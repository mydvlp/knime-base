/* 
 * -------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2007
 * University of Konstanz, Germany
 * Chair for Bioinformatics and Information Mining (Prof. M. Berthold)
 * and KNIME GmbH, Konstanz, Germany
 *
 * You may not modify, publish, transmit, transfer or sell, reproduce,
 * create derivative works from, distribute, perform, display, or in
 * any way exploit any of the content, in whole or in part, except as
 * otherwise expressly permitted in writing by the copyright owner or
 * as specified in the license file distributed with this product.
 *
 * If you have any questions please contact the copyright holder:
 * website: www.knime.org
 * email: contact@knime.org
 * -------------------------------------------------------------------
 * 
 * History
 *   30.10.2005 (mb): created
 */
package org.knime.base.node.io.predictor;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.ModelContent;
import org.knime.core.node.ModelContentRO;
import org.knime.core.node.ModelContentWO;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * Read ModelContent object from file.
 * 
 * @author M. Berthold, University of Konstanz
 */
public class PredictorReaderNodeModel extends NodeModel {

    /** key for filename entry in config object. */
    static final String FILENAME = "filename";

    private final SettingsModelString m_fileName =
            new SettingsModelString(FILENAME, null);

    private ModelContentRO m_predParams;

    /**
     * Constructor: Create new NodeModel with only one Model Input Port.
     */
    public PredictorReaderNodeModel() {
        super(0, 0, 0, 1);
    }

    /**
     * @see NodeModel#saveSettingsTo(NodeSettingsWO)
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_fileName.saveSettingsTo(settings);
    }

    /**
     * @see NodeModel#validateSettings(NodeSettingsRO)
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        SettingsModelString filename =
                m_fileName.createCloneWithValidatedValue(settings);
        checkFileAccess(filename.getStringValue());
    }

    /**
     * @see NodeModel#loadValidatedSettingsFrom(NodeSettingsRO)
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        SettingsModelString filename =
            m_fileName.createCloneWithValidatedValue(settings);
        checkFileAccess(filename.getStringValue());
        m_fileName.setStringValue(filename.getStringValue());
    }

    /**
     * Save model into ModelContent for a specific output port.
     * 
     * @param index of the ModelContent's ouput port.
     * @param predParam The object to write the model into.
     * @throws InvalidSettingsException If the model could not be written to
     *             file.
     */
    @Override
    protected void saveModelContent(final int index,
            final ModelContentWO predParam) throws InvalidSettingsException {
        assert index == 0 : index;
        if (predParam != null && m_predParams != null) {
            m_predParams.copyTo(predParam);
        }
    }

    /**
     * Execute does nothing - the reading of the file and writing to the
     * NodeSettings object has already happened during savePredictorParams.
     * 
     * @see NodeModel#execute(BufferedDataTable[],ExecutionContext)
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] data,
            final ExecutionContext exec) throws CanceledExecutionException,
            IOException {
        m_predParams = null;
        InputStream is =
                new BufferedInputStream(new FileInputStream(
                        new File(m_fileName.getStringValue())));
        if (m_fileName.getStringValue().endsWith(".gz")) {
            is = new GZIPInputStream(is);
        }
        exec.setProgress(-1, "Reading from file: " 
                + m_fileName.getStringValue());
        m_predParams = ModelContent.loadFromXML(is);
        return new BufferedDataTable[0];
    }

    /**
     * Ignored.
     * 
     * @see org.knime.core.node.NodeModel#reset()
     */
    @Override
    protected void reset() {
    }

    /**
     * @see NodeModel#configure(DataTableSpec[])
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
        checkFileAccess(m_fileName.getStringValue());
        return new DataTableSpec[0];
    }

    /*
     * Helper that checks some properties for the file argument.
     * 
     * @param fileName The file to check @throws InvalidSettingsException If
     * that fails.
     */
    private String checkFileAccess(final String fileName)
            throws InvalidSettingsException {
        if (fileName == null) {
            throw new InvalidSettingsException("No file set.");
        }
        String newFileName = fileName;
        if (!fileName.endsWith(".pmml") && !fileName.endsWith(".pmml.gz")) {
            newFileName += ".pmml.gz";
        }
        File file = new File(newFileName);
        if (file.isDirectory()) {
            throw new InvalidSettingsException("\"" + file.getAbsolutePath()
                    + "\" is a directory.");
        }
        if (!file.exists()) {
            // dunno how to check the write access to the directory. If we can't
            // create the file the execute of the node will fail. Well, too bad.
            throw new InvalidSettingsException("File does not exist: "
                    + newFileName);
        }
        if (!file.canRead()) {
            throw new InvalidSettingsException("Cannot write to file \""
                    + file.getAbsolutePath() + "\".");
        }
        return newFileName;
    }

    /**
     * @see org.knime.core.node.NodeModel#loadInternals(java.io.File,
     *      org.knime.core.node.ExecutionMonitor)
     */
    @Override
    protected void loadInternals(final File nodeInternDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // nothing to do here
    }

    /**
     * @see org.knime.core.node.NodeModel#saveInternals(java.io.File,
     *      org.knime.core.node.ExecutionMonitor)
     */
    @Override
    protected void saveInternals(final File nodeInternDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // nothing to do here
    }

}
