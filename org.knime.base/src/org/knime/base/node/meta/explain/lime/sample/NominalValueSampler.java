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

import java.util.Collection;
import java.util.Collections;
import java.util.stream.IntStream;

import org.apache.commons.math3.distribution.EnumeratedIntegerDistribution;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.knime.base.node.meta.explain.lime.colstats.NominalFeatureStatistic;
import org.knime.base.node.meta.explain.util.Caster;
import org.knime.base.node.meta.explain.util.iter.DoubleIterator;
import org.knime.base.node.meta.explain.util.iter.SingletonDoubleIterator;
import org.knime.core.data.DataCell;
import org.knime.core.data.NominalValue;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.node.util.CheckUtils;

/**
 *
 * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
 */
public class NominalValueSampler implements CellSampler {

    private final NominalFeatureStatistic m_stat;

    private final EnumeratedIntegerDistribution m_idxSampler;

    private final Caster<NominalValue> m_caster = new Caster<>(NominalValue.class, false);

    private NominalValue m_reference = null;

    NominalValueSampler(final NominalFeatureStatistic stat, final long seed) {
        m_stat = stat;
        RandomGenerator random = new JDKRandomGenerator();
        random.setSeed(seed);
        m_idxSampler = new EnumeratedIntegerDistribution(random, IntStream.range(0, stat.getNumValues()).toArray(),
            stat.getDistribution());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LimeSample sample() {
        CheckUtils.checkState(m_reference != null, "setReference has to be called before sample can be called.");
        final int valueIdx = m_idxSampler.sample();
        final DataCell value = m_stat.getValue(valueIdx);
        return new NominalLimeSample(value.equals(m_reference), value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setReference(final DataCell reference) {
        m_reference = m_caster.getAsT(reference);
    }

    private static class NominalLimeSample implements LimeSample {

        private static final Collection<DataCell> ONE = Collections.singleton(new DoubleCell(1.0));

        private static final Collection<DataCell> ZERO = Collections.singleton(new DoubleCell(0.0));

        private final double m_dv;

        private final Collection<DataCell> m_data;

        private final DataCell m_inverse;

        NominalLimeSample(final boolean matchesReference, final DataCell value) {
            m_inverse = value;
            m_data = matchesReference ? ONE : ZERO;
            m_dv = matchesReference ? 1.0 : 0.0;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public DoubleIterator iterator() {
            return new SingletonDoubleIterator(m_dv);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Collection<DataCell> getData() {
            return m_data;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public DataCell getInverse() {
            return m_inverse;
        }

    }

}
