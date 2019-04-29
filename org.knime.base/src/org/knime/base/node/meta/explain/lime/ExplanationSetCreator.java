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
package org.knime.base.node.meta.explain.lime;

import java.util.NoSuchElementException;

import org.apache.commons.math3.random.RandomDataGenerator;
import org.knime.base.node.meta.explain.feature.FeatureVector;
import org.knime.base.node.meta.explain.feature.KnimeFeatureVectorIterator;
import org.knime.base.node.meta.explain.feature.PerturbableFeatureVector;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.append.AppendedColumnRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.vector.bitvector.DenseBitVectorCell;
import org.knime.core.data.vector.bitvector.DenseBitVectorCellFactory;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;

/**
 *
 * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
 */
class ExplanationSetCreator {

    private final KnimeFeatureVectorIterator m_iter;

    private final int m_explanationSetSize;

    private final DataTableSpec m_explanationTableSpec;

    private final RandomDataGenerator m_random;

    private final WeightingKernel m_weightingKernel;

    public ExplanationSetCreator(final KnimeFeatureVectorIterator iter, final WeightingKernel weightingKernel,
        final DataTableSpec explanationTableSpec, final int explanationSetSize, final long seed) {
        m_iter = iter;
        m_explanationTableSpec = explanationTableSpec;
        m_explanationSetSize = explanationSetSize;
        m_random = new RandomDataGenerator();
        m_random.getRandomGenerator().setSeed(seed);
        m_weightingKernel = weightingKernel;
    }



    public boolean hasNext() {
        return m_iter.hasNext();
    }

    public BufferedDataTable next(final ExecutionContext exec) throws CanceledExecutionException {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        final double total = m_explanationSetSize;
        final BufferedDataContainer container = exec.createDataContainer(m_explanationTableSpec);
        final FeatureVector x = m_iter.next();
        for (int i = 0; i < m_explanationSetSize; i++) {
            exec.checkCanceled();
            final String keySuffix = "_" + i;
            final DataRow explanationRow = createExplanationRow(x, keySuffix);
            container.addRowToTable(explanationRow);
            exec.setProgress(i / total);
        }
        container.close();
        return container.getTable();
    }

    public void close() {
        m_iter.close();
    }

    /**
     * @param x
     * @param keySuffix
     * @return
     */
    private DataRow createExplanationRow(final FeatureVector x, final String keySuffix) {
        final PerturbableFeatureVector pertubable = x.getPerturbable(keySuffix);
        // TODO figure out what to do if there is only a single feature
        // (although there is no need for an explanation since all changes are due to this lone feature)
        final int nPerturb = m_random.nextInt(1, x.size() - 1);
        final DenseBitVectorCellFactory bvFac = new DenseBitVectorCellFactory(x.size());
        bvFac.set(0, x.size());
        for (int j = 0; j < nPerturb; j++) {
            final int toPerturb = m_random.nextInt(0, x.size() - 1);
            bvFac.set(toPerturb, false);
            pertubable.perturb(toPerturb);
        }
        final DataRow perturbedRow = pertubable.get();
        final DenseBitVectorCell label = bvFac.createDataCell();
        final double weight = m_weightingKernel.calculate(x.get(), perturbedRow, label);
        final DataRow explanationRow = new AppendedColumnRow(perturbedRow, new DoubleCell(weight), label);
        return explanationRow;
    }

}
