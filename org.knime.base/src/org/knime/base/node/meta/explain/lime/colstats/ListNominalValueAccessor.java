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
 *   Apr 29, 2019 (Adrian Nembach, KNIME GmbH, Konstanz, Germany): created
 */
package org.knime.base.node.meta.explain.lime.colstats;

import org.knime.base.node.meta.explain.lime.colstats.valueaccess.NominalValueAccessor;
import org.knime.base.node.meta.explain.util.Caster;
import org.knime.core.data.DataCell;
import org.knime.core.data.NominalValue;
import org.knime.core.data.collection.ListDataValue;
import org.knime.core.node.util.CheckUtils;

/**
 *
 * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
 */
final class ListNominalValueAccessor implements NominalValueAccessor {

    private final int m_idx;

    private final Caster<ListDataValue> m_caster = new Caster<>(ListDataValue.class, false);

    private NominalValue m_value = null;

    ListNominalValueAccessor(final int idx) {
        m_idx = idx;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void accept(final DataCell cell) {
        final ListDataValue list = m_caster.getAsT(cell);
        final DataCell element = list.get(m_idx);
        CheckUtils.checkArgument(element instanceof NominalValue,
            "Expected nominal value at list index %s but received object of type %s instead.", m_idx,
            element.getType().getName());
        m_value = (NominalValue)element;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NominalValue getValue() {
        CheckUtils.checkState(m_value != null,
            "ListNominalValueAccessor#accept has to be called at least once before calling ListNominalValueAccessor#getValue.");
        return m_value;
    }

}
