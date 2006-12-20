/*
 * ------------------------------------------------------------------
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
 *   04.07.2005 (ohl): created
 */
package org.knime.base.node.preproc.filter.row.rowfilter;

import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

/**
 * Row filter that doesn't match any row. Not really usefull - but used if the
 * user absolutly wants it.
 * 
 * @author Peter Ohl, University of Konstanz
 */
public class FalseRowFilter extends RowFilter {
    /**
     * @see RowFilter#matches(DataRow, int)
     */
    @Override
    public boolean matches(final DataRow row, final int rowIndex)
            throws EndOfTableException {
        // we can immediately tell that the end of the table is reached.
        throw new EndOfTableException();
    }

    /**
     * @see RowFilter#loadSettingsFrom(NodeSettingsRO)
     */
    @Override
    public void loadSettingsFrom(final NodeSettingsRO cfg)
            throws InvalidSettingsException {
        // no settings to load.
    }

    /**
     * @see RowFilter#saveSettings(NodeSettingsWO)
     */
    @Override
    protected void saveSettings(final NodeSettingsWO cfg) {
        // no settings to save.
    }

    /**
     * @see RowFilter
     *      #configure(org.knime.core.data.DataTableSpec)
     */
    @Override
    public DataTableSpec configure(final DataTableSpec inSpec)
            throws InvalidSettingsException {
        return inSpec;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "FALSE-Filter";
    }
}
