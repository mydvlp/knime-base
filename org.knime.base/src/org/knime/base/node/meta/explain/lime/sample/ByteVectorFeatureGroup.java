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
 *   Apr 30, 2019 (Adrian Nembach, KNIME GmbH, Konstanz, Germany): created
 */
package org.knime.base.node.meta.explain.lime.sample;

import java.util.List;
import java.util.NoSuchElementException;

import org.knime.base.node.meta.explain.lime.colstats.NumericFeatureStatistic;
import org.knime.base.node.meta.explain.util.Caster;
import org.knime.base.node.meta.explain.util.iter.DoubleIterable;
import org.knime.base.node.meta.explain.util.iter.DoubleIterator;
import org.knime.core.data.DataCell;
import org.knime.core.data.vector.bytevector.ByteVectorValue;

/**
 *
 * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
 */
final class ByteVectorFeatureGroup implements FeatureGroup {

    private final List<NumericFeatureStatistic> m_stats;

    private static final Caster<ByteVectorValue> CASTER = new Caster<>(ByteVectorValue.class, false);

    private final boolean m_sampleAroundReference;

    /**
     *
     */
    ByteVectorFeatureGroup(final List<NumericFeatureStatistic> stats, final boolean sampleAroundReference) {
        m_stats = stats;
        m_sampleAroundReference = sampleAroundReference;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DoubleIterable mapOriginalCell(final DataCell cell) {
        final ByteVectorValue bv = CASTER.getAsT(cell);
        return new DoubleIterable() {

            @Override
            public DoubleIterator iterator() {
                return new DoubleIterator() {
                    int m_idx = -1;

                    @Override
                    public boolean hasNext() {
                        return m_idx < bv.length();
                    }

                    @Override
                    public double next() {
                        if (!hasNext()) {
                            throw new NoSuchElementException();
                        }
                        m_idx++;
                        final double value = bv.get(m_idx);
                        final NumericFeatureStatistic stat = m_stats.get(m_idx);
                        return (value - stat.getMean()) / stat.getStd();
                    }

                };
            }

        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CellSampler createSampler(final long seed) {
        return new ByteVectorCellSampler(m_stats, m_sampleAroundReference, seed);
    }

}
