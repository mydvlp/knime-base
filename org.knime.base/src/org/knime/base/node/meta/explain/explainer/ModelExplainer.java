/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME AG, Zurich, Switzerland
 *  Website: http://www.knime.com; Email: contact@knime.com
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ---------------------------------------------------------------------
 *
 * History
 *   Apr 15, 2019 (Adrian Nembach, KNIME GmbH, Konstanz, Germany): created
 */
package org.knime.base.node.meta.explain.explainer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.knime.base.node.meta.explain.explainer.node.ModelExplainerSettings;
import org.knime.base.node.meta.explain.feature.FeatureManager;
import org.knime.base.node.meta.explain.feature.KnimeFeatureVectorIterator;
import org.knime.base.node.meta.explain.feature.PerturberFactory;
import org.knime.base.node.meta.explain.feature.SimpleReplacingPerturberFactory;
import org.knime.base.node.meta.explain.feature.VectorEnabledPerturberFactory;
import org.knime.base.node.meta.explain.util.DefaultRowSampler;
import org.knime.base.node.meta.explain.util.RowSampler;
import org.knime.base.node.meta.explain.util.TablePreparer;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTable;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataTableSpecCreator;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.vector.bitvector.DenseBitVectorCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.util.CheckUtils;

/**
 *
 * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
 */
public class ModelExplainer {

    private final TablePreparer m_featureTablePreparer;

    private final FeatureManager m_featureManager;

    private ModelExplainerSettings m_settings;

    private ExplanationSetCreator m_explanationSetCreator;

    private int m_numIterations = -1;

    private int m_currentIteration = -1;

    public ModelExplainer(final ModelExplainerSettings settings) {
        m_settings = settings;
        m_featureTablePreparer = new TablePreparer(settings.getFeatureCols(), "feature");
        // for now we don't care about feature names (the loop end will be a generic one at first)
        m_featureManager = new FeatureManager(m_settings.isTreatAllColumnsAsSingleFeature(), false);
    }

    private static DataTableSpec createExplanationTableSpec(final DataTableSpec featureTableSpec) {
        final DataTableSpecCreator specCreator = new DataTableSpecCreator(featureTableSpec);
        // TODO make sure the column name is unique
        final DataColumnSpec weightCol = new DataColumnSpecCreator("weight", DoubleCell.TYPE).createSpec();
        final DataColumnSpec labelCol = new DataColumnSpecCreator("label", DenseBitVectorCell.TYPE).createSpec();
        specCreator.addColumns(weightCol, labelCol);
        return specCreator.createSpec();
    }

    /**
     * Configures the loop start node
     *
     * @param roiSpec {@link DataTableSpec} of the table containing the rows to explain
     * @param samplingSpec {@link DataTableSpec} of the table containing the rows for sampling
     * @param settings the current node settings
     * @return the {@link DataTableSpec} of the table that has to be predicted by the model
     * @throws InvalidSettingsException if some of the selected feature columns are not contained in the sampling table
     */
    public DataTableSpec configureLoopStart(final DataTableSpec roiSpec, final DataTableSpec samplingSpec,
        final ModelExplainerSettings settings) throws InvalidSettingsException {
        m_settings = settings;
        m_featureTablePreparer.updateSpecs(roiSpec, settings.getFeatureCols());
        m_featureTablePreparer.checkSpec(samplingSpec);
        final DataTableSpec featureTableSpec = m_featureTablePreparer.getTableSpec();
        m_featureManager.updateWithSpec(featureTableSpec);
        return createExplanationTableSpec(featureTableSpec);
    }

    public BufferedDataTable executeLoopStart(final BufferedDataTable roiTable, final BufferedDataTable samplingTable,
        final ExecutionContext exec) throws Exception {
        checkForEmptyTables(roiTable, samplingTable);
        m_featureTablePreparer.checkSpec(samplingTable.getDataTableSpec());
        boolean isFirstIteration = false;
        if (m_explanationSetCreator == null) {
            isFirstIteration = true;
            // since each incoming row corresponds to one iteration this would take literally forever
            m_numIterations = getAsInt(roiTable.size(), "The first input table has more than Integer.MAX_VALUE rows, "
                + "tables of this size are currently not supported.");
            initializeExplanationSetCreator(roiTable, samplingTable, exec.createSubExecutionContext(0.1));
        }
        m_currentIteration++;

        return m_explanationSetCreator.next(exec.createSubExecutionContext(isFirstIteration ? 0.9 : 1.0));

    }

    /**
     * @return true if there will be another iteration
     */
    public boolean hasNextIteration() {
        return m_explanationSetCreator.hasNext();
    }

    /**
     * @return the current iteration
     */
    public int getCurrentIteration() {
        CheckUtils.checkState(m_currentIteration >= 0, "The iterations have not been initialized, yet.");
        return m_currentIteration;
    }

    /**
     * @return the total number of iterations
     */
    public int getNumberOfIterations() {
        CheckUtils.checkState(m_numIterations >= 0, "The iterations have not been initialized, yet.");
        return m_numIterations;
    }

    /**
     * Makes sure that all resources are released
     */
    public void reset() {
        if (m_explanationSetCreator != null) {
            m_explanationSetCreator.close();
        }
        m_explanationSetCreator = null;
        m_currentIteration = -1;
        m_currentIteration = -1;
    }

    private static int getAsInt(final long value, final String message) {
        CheckUtils.checkArgument(value <= Integer.MAX_VALUE, message);
        return (int)value;
    }

    // TODO could be performed by a factory
    private void initializeExplanationSetCreator(final BufferedDataTable roiTable, final BufferedDataTable samplingTable,
        final ExecutionContext exec) throws InvalidSettingsException, CanceledExecutionException, Exception {
        final BufferedDataTable roiFeatureTable = m_featureTablePreparer.createTable(roiTable, exec);
        final BufferedDataTable samplingFeatureTable = m_featureTablePreparer.createTable(samplingTable, exec);
        updateFeatureManager(roiFeatureTable, samplingFeatureTable);
        final PerturberFactory<DataRow, Set<Integer>, DataCell[]> perturberFactory =
            createPerturberFactory(samplingFeatureTable, samplingTable.size(), exec.createSubProgress(0.1));
        final KnimeFeatureVectorIterator featureVectorIterator =
            new KnimeFeatureVectorIterator(roiFeatureTable.iterator(), perturberFactory,
                m_featureManager.getNumFeatures().orElseThrow(() -> new IllegalStateException(
                    "At this point the number of features should be known. This is a coding error.")));
        final DataTableSpec explanationTableSpec = createExplanationTableSpec(m_featureTablePreparer.getTableSpec());
        m_explanationSetCreator = new ExplanationSetCreator(featureVectorIterator, new ShapWeightingKernel(),
            explanationTableSpec, m_settings.getExplanationSetSize(), m_settings.getSeed());
    }

    // TODO PerturberFactoryFactory ?
    private PerturberFactory<DataRow, Set<Integer>, DataCell[]> createPerturberFactory(final DataTable samplingTable,
        final long numRows, final ExecutionMonitor prog) throws Exception {
        final DataRow[] samplingSet = createSamplingSet(samplingTable, numRows, prog);
        final RowSampler rowSampler = new DefaultRowSampler(samplingSet);
        if (m_featureManager.containsCollection()) {
            final int[] numFeaturesPerCol = m_featureManager.getNumberOfFeaturesPerColumn();
            return new VectorEnabledPerturberFactory(rowSampler, m_featureManager.getFactories(), numFeaturesPerCol);
        } else {
            return new SimpleReplacingPerturberFactory(rowSampler);
        }

    }

    /**
     * @param samplingTable
     * @return
     * @throws InvalidSettingsException
     */
    private static DataRow[] createSamplingSet(final DataTable samplingFeatureTable, final long numRows,
        final ExecutionMonitor prog) throws Exception {
        // TODO check if we can implement Sobol (quasi random) sampling
        // in an efficient way using BufferedDataTables or caching
        prog.setProgress("Create sampling dataset");
        final List<DataRow> samplingData = new ArrayList<>();
        long current = 0;
        final double total = numRows;
        for (DataRow row : samplingFeatureTable) {
            prog.checkCanceled();
            prog.setProgress(current / total, "Reading row " + row.getKey());
            current++;
            samplingData.add(row);
        }
        return samplingData.toArray(new DataRow[samplingData.size()]);
    }

    private void updateFeatureManager(final DataTable roiFeatureTable, final DataTable samplingFeatureTable) {
        final DataRow roiRow = roiFeatureTable.iterator().next();
        // TODO figure out how to handle the case where configure and execution state don't match
        m_featureManager.updateWithRow(roiRow);
        final DataRow samplingRow = samplingFeatureTable.iterator().next();
        CheckUtils.checkState(m_featureManager.hasSameNumberOfFeatures(samplingRow),
            "The rows in the sampling table contain a different number of features than the rows in the ROI table.");
    }

    private static void checkForEmptyTables(final BufferedDataTable roiTable, final BufferedDataTable samplingTable) {
        CheckUtils.checkArgument(roiTable.size() > 0, "The ROI table must contain at least one row.");
        CheckUtils.checkArgument(samplingTable.size() > 0, "The sampling table must contain at least one row.");
    }

}
