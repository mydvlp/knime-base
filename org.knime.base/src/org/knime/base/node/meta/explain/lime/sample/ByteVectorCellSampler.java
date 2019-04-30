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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.apache.commons.math3.random.RandomDataGenerator;
import org.knime.base.node.meta.explain.lime.colstats.NumericFeatureStatistic;
import org.knime.base.node.meta.explain.util.Caster;
import org.knime.base.node.meta.explain.util.iter.DoubleIterator;
import org.knime.core.data.DataCell;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.vector.bytevector.ByteVectorValue;
import org.knime.core.data.vector.bytevector.DenseByteVectorCellFactory;
import org.knime.core.node.util.CheckUtils;

/**
 *
 * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
 */
public class ByteVectorCellSampler implements CellSampler {

    private final List<NumericFeatureStatistic> m_stats;

    private final Caster<ByteVectorValue> m_caster = new Caster<>(ByteVectorValue.class, false);

    private final boolean m_sampleAroundReference;

    private final RandomDataGenerator m_random;

    private ByteVectorValue m_reference = null;

    ByteVectorCellSampler(final List<NumericFeatureStatistic> stats, final boolean sampleAroundReference,
        final long seed) {
        m_stats = new ArrayList<>(stats);
        m_sampleAroundReference = sampleAroundReference;
        m_random = new RandomDataGenerator();
        m_random.getRandomGenerator().setSeed(seed);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LimeSample sample() {
        final double[] data = createData();
        return new ByteVectorLimeSample(data);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setReference(final DataCell reference) {
        m_reference = m_caster.getAsT(reference);
    }

    private double[] createData() {
        final double[] data = new double[m_stats.size()];
        for (int i = 0; i < m_stats.size(); i++) {
            double sample = m_random.nextGaussian(0.0, 1.0);
            final NumericFeatureStatistic stat = m_stats.get(i);
            sample *= stat.getStd();
            if (m_sampleAroundReference) {
                CheckUtils.checkState(m_reference != null, "No reference set.");
                sample += m_reference.get(i);
            } else {
                sample += stat.getMean();
            }
            data[i] = sample;
        }
        return data;
    }

    private class ByteVectorLimeSample implements LimeSample {

        private final double[] m_data;

        ByteVectorLimeSample(final double[] data) {
            m_data = data;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Collection<DataCell> getData() {
            return Arrays.stream(m_data).mapToObj(DoubleCell::new).collect(Collectors.toCollection(ArrayList::new));
        }

        private int size() {
            return m_data.length;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public DataCell getInverse() {
            final DenseByteVectorCellFactory factory = new DenseByteVectorCellFactory(size());
            for (int i = 0; i < size(); i++) {
                factory.setValue(i, toByte(m_data[i]));
            }
            return factory.createDataCell();
        }



        /**
         * {@inheritDoc}
         */
        @Override
        public DoubleIterator iterator() {
            return new DoubleIterator() {
                int m_idx = -1;

                @Override
                public boolean hasNext() {
                    return m_idx < m_data.length;
                }

                @Override
                public double next() {
                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    }
                    m_idx++;
                    return m_data[m_idx];
                }

            };
        }


    }

    private static int toByte(final double value) {
        final long rounded = Math.round(value);
        if (rounded > 255) {
            return 255;
        } else if (rounded < 0) {
            return 0;
        } else {
            return (int)rounded;
        }
    }

}
