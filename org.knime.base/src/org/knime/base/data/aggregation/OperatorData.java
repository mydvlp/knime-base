/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2011
 *  University of Konstanz, Germany and
 *  KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
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
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
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
 * -------------------------------------------------------------------
 */

package org.knime.base.data.aggregation;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataValue;


/**
 *
 * @author Tobias Koetter, University of Konstanz
 */
public class OperatorData {
    /**Flag that indicates if the aggregator uses limits.*/
    private final boolean m_usesLimit;
    /**The DataValues this method supports.*/
    private final Class<? extends DataValue> m_supportedType;
    /**If the method supports the setting of the missing value flag
     * by the user.*/
    private final boolean m_supportsMissingVals;
    /**The unique label that is displayed to the user and that
     * is used for registration.*/
    private final String m_label;
    /**The label of the aggregation method which is
    * used in the column name.*/
    private final String m_colName;
    /**Flag that indicates if the original column specification should be
     * used.*/
    private final boolean m_keepColSpec;

    /**Constructor for class OperatorData.
     * @param label unique user readable label. The label need to be unique
     * since it is used for registering and for the column name. It is also
     * displayed to the user.
     * @param usesLimit <code>true</code> if the method checks the number of
     * unique values limit.
     * @param keepColSpec <code>true</code> if the original column specification
     * should be kept if possible
     * @param supportedClass the {@link DataValue} class supported by
     * this method
     * @param supportMissingValsOption <code>true</code> if the operator
     * supports the alternation of the missing value flag
     */
    public OperatorData(final String label,
            final boolean usesLimit,
            final boolean keepColSpec,
            final Class<? extends DataValue> supportedClass,
            final boolean supportMissingValsOption) {
        this(label, label, usesLimit, keepColSpec, supportedClass,
                supportMissingValsOption);
    }

    /**Constructor for class OperatorData.
     *@param label unique user readable label. The label need to be unique
     * since it is used for registering and for the column name. It is also
     * displayed to the user.
     * @param colName the name of the result column
     * @param usesLimit <code>true</code> if the method checks the number of
     * unique values limit.
     * @param keepColSpec <code>true</code> if the original column specification
     * should be kept if possible
     * @param supportedClass the {@link DataValue} class supported by
     * this method
     * @param supportMissingValsOption <code>true</code> if the operator supports
     * the alternation of the missing value flag
     */
    public OperatorData(final String label, final String colName,
            final boolean usesLimit,
            final boolean keepColSpec,
            final Class<? extends DataValue> supportedClass,
            final boolean supportMissingValsOption) {
        m_label = label;
        m_colName = colName;
        m_usesLimit = usesLimit;
        m_keepColSpec = keepColSpec;
        m_supportedType = supportedClass;
        m_supportsMissingVals = supportMissingValsOption;
    }

    /**
     * @return the supported {@link DataValue} class
     */
    public Class<? extends DataValue> getSupportedType() {
        return m_supportedType;
    }

    /**
     * @return <code>true</code> if the operator supports the alteration of
     * the missing cell option
     */
    public boolean supportsMissingValueOption() {
        return m_supportsMissingVals;
    }

    /**
     * @return <code>true</code> if the original {@link DataColumnSpec} should
     * be kept.
     */
    public boolean keepColumnSpec() {
        return m_keepColSpec;
    }

    /**
     * @return the unique label that is displayed to the user and that
     * is used for registration
     */
    public String getLabel() {
        return m_label;
    }

    /**
     * @return the label of the aggregation method which is
     * used in the column name
     */
    public String getColumnLabel() {
        return m_colName;
    }

    /**
     * @return <code>true</code> if this method checks the maximum unique
     * values limit.
     */
    public boolean usesLimit() {
        return m_usesLimit;
    }
}