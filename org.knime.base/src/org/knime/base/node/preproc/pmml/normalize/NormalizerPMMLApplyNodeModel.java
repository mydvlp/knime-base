/* ------------------------------------------------------------------
  * This source code, its documentation and all appendant files
  * are protected by copyright law. All rights reserved.
  *
  * Copyright, 2008 - 2011
  * KNIME.com, Zurich, Switzerland
  *
  * You may not modify, publish, transmit, transfer or sell, reproduce,
  * create derivative works from, distribute, perform, display, or in
  * any way exploit any of the content, in whole or in part, except as
  * otherwise expressly permitted in writing by the copyright owner or
  * as specified in the license file distributed with this product.
  *
  * If you have any questions please contact the copyright holder:
  * website: www.knime.com
  * email: contact@knime.com
  * ---------------------------------------------------------------------
  *
  * History
  *   Apr 6, 2011 (morent): created
  */

package org.knime.base.node.preproc.pmml.normalize;

import java.util.ArrayList;
import java.util.List;

import org.knime.base.data.normalize.AffineTransConfiguration;
import org.knime.base.data.normalize.AffineTransTable;
import org.knime.base.data.normalize.Normalizer;
import org.knime.base.data.normalize.PMMLNormalizeTranslator;
import org.knime.base.node.preproc.normalize.NormalizerApplyNodeModel;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.pmml.PMMLPortObject;
import org.knime.core.node.port.pmml.PMMLPortObjectSpec;

/**
 * @author Dominik Morent, KNIME.com, Zurich, Switzerland
 *
 */
public class NormalizerPMMLApplyNodeModel extends NormalizerApplyNodeModel {
    /**
    *
    */
   public NormalizerPMMLApplyNodeModel() {
       super(PMMLPortObject.TYPE, true);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
           throws InvalidSettingsException {
       PMMLPortObjectSpec modelSpec
               = (PMMLPortObjectSpec)inSpecs[0];
       DataTableSpec dataSpec = (DataTableSpec)inSpecs[1];
       List<String> unknownCols = new ArrayList<String>();
       List<String> knownCols = new ArrayList<String>();
       for (String column : modelSpec.getPreprocessingFields()) {
           DataColumnSpec inDataCol = dataSpec.getColumnSpec(column);
           if (inDataCol == null) {
               unknownCols.add(column);
           } else {
               knownCols.add(column);
           }
       }
       if (!unknownCols.isEmpty()) {
           setWarningMessage("Some column(s) as specified by the model is not "
                   + "present in the data: " + unknownCols);
       }
       String[] ar = knownCols.toArray(new String[knownCols.size()]);
       DataTableSpec s = Normalizer.generateNewSpec(dataSpec, ar);
       return new PortObjectSpec[]{modelSpec, s};
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected PortObject[] execute(final PortObject[] inData,
           final ExecutionContext exec) throws Exception {
       PMMLPortObject model = (PMMLPortObject)inData[0];
       BufferedDataTable table = (BufferedDataTable)inData[1];

       PMMLNormalizeTranslator translator = new PMMLNormalizeTranslator();
       translator.initializeFrom(model.getDerivedFields());
       AffineTransConfiguration config = translator.getAffineTransConfig();
       if (config.getNames().length == 0) {
           throw new IllegalArgumentException("No normalization configuration "
                   + "found.");
       }
       AffineTransTable t = new AffineTransTable(table, config);
       BufferedDataTable bdt = exec.createBufferedDataTable(t, exec);
       if (t.getErrorMessage() != null) {
           setWarningMessage(t.getErrorMessage());
       }
       return new PortObject[]{model, bdt};
   }

}