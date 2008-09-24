/*
 * -------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2008
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
 */
package org.knime.base.node.io.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.knime.base.node.io.database.DBConnectionDialogPanel.DBTableOptions;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettings;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.database.DatabaseQueryConnectionSettings;
import org.knime.core.node.port.database.DatabasePortObjectSpec;

/**
 * 
 * @author Thomas Gabriel, University of Konstanz
 */
class DBNodeModel extends NodeModel {
    
    /** Config ID for temporary table. */
    private static final String CFG_TABLE_ID = "tableID.xml";
    
    private final SettingsModelString m_tableOption =
        DBConnectionDialogPanel.createTableModel();

    private final SettingsModelIntegerBounded m_cachedRows =
        DBConnectionDialogPanel.createCachedRowsModel();
    
    private DatabaseQueryConnectionSettings m_conn;

    private String m_tableId;
    
    /**
     * Creates a new database reader.
     * @param inPorts array of input port types
     * @param outPorts array of output port types
     */
    DBNodeModel(final PortType[] inPorts, final PortType[] outPorts) {
        super(inPorts, outPorts);
        m_tableId = "table_" + System.identityHashCode(this);
    }
    
    /**
     * @return ID for the temp table as <code>table_</code>hashCode()
     */
    final String getTableID() {
        return m_tableId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_tableOption.saveSettingsTo(settings);
        m_cachedRows.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_tableOption.validateSettings(settings);
        m_cachedRows.validateSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_tableOption.loadSettingsFrom(settings);
        m_cachedRows.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        if (m_conn != null) {
            if (DBTableOptions.CREATE_TABLE.getActionCommand().equals(
                    m_tableOption.getStringValue())) {
                try {
                    m_conn.execute("DROP TABLE " + m_tableId);
                } catch (Exception e) {
                    super.setWarningMessage("Can't drop table with id \"" 
                            + m_tableId + "\", reason: " + e.getMessage());
                }
            }
            m_conn = null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir,
            final ExecutionMonitor exec) throws IOException {
        NodeSettingsRO sett = NodeSettings.loadFromXML(new FileInputStream(
                new File(nodeInternDir, CFG_TABLE_ID)));
        m_tableId = sett.getString(CFG_TABLE_ID, m_tableId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir,
            final ExecutionMonitor exec) throws IOException {
        NodeSettings sett = new NodeSettings(CFG_TABLE_ID);
        sett.addString(CFG_TABLE_ID, m_tableId);
        sett.saveToXML(new FileOutputStream(
                new File(nodeInternDir, CFG_TABLE_ID)));
    }
    
    /**
     * Creates a new query connection based on the connection settings, that 
     * is, either create a new table or wrap the SQL statement.
     * @param spec the database spec
     * @param newQuery the new query to execute
     * @return a database connection object
     * @throws InvalidSettingsException if the query to create the new table 
     *         inside the database could not be executed
     */
    final DatabaseQueryConnectionSettings createDBQueryConnection(
            final DatabasePortObjectSpec spec, final String newQuery) 
            throws InvalidSettingsException {
        DatabaseQueryConnectionSettings conn = 
            new DatabaseQueryConnectionSettings(
                spec.getConnectionModel(), getNumCachedRows());
        if (DBTableOptions.CREATE_TABLE.getActionCommand().equals(
                m_tableOption.getStringValue())) {
            try {
                conn.execute("CREATE TABLE " + m_tableId + " AS " + newQuery);
            } catch (Throwable t) {
                throw new InvalidSettingsException("Could not execute query \""
                        + newQuery + "\" to create new table, reason: "
                        + t.getMessage(), t);
            }
            return new DatabaseQueryConnectionSettings(
                    conn, "SELECT * FROM " + m_tableId, 
                    m_cachedRows.getIntValue());
        } else {
            return new DatabaseQueryConnectionSettings(conn, newQuery,
                    m_cachedRows.getIntValue());
        }
    }
    
    /**
     * @return number of rows to cache for review table
     */
    final int getNumCachedRows() {
        return m_cachedRows.getIntValue();
    }
    
        
}
