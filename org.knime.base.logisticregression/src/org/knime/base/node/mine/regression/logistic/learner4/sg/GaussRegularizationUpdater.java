/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME GmbH, Konstanz, Germany
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
 * ---------------------------------------------------------------------
 *
 * History
 *   24.03.2017 (Adrian): created
 */
package org.knime.base.node.mine.regression.logistic.learner4.sg;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.knime.base.node.mine.regression.logistic.learner4.data.TrainingRow;

/**
 * Efficient implementation for Gauss (L2) regularization that scales the entire matrix instead of applying
 * the updates per coefficient.
 * Note that it requires the use of a WeightVector implementation that allows to efficiently scale the weights.
 *
 * @author Adrian Nembach, KNIME.com
 */
class GaussRegularizationUpdater implements LazyRegularizationUpdater {

    private final double m_lambda;

    GaussRegularizationUpdater(final double variance, final int rowCount) {
        m_lambda = 1.0 / (variance * rowCount);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(final WeightVector<?> beta, final double stepSize, final int iteration) {
        beta.scale(1 - stepSize * m_lambda);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void lazyUpdate(final WeightVector<?> beta, final TrainingRow x, final int[] lastVisited, final int iteration) {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resetJITSystem(final WeightVector<?> beta, final int[] lastVisited) {
        // nothing to reset
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RealMatrix hessian(final WeightVector<?> beta) {
        int nVar = beta.getNVariables();
        int dim = nVar * beta.getNVectors();
        double[] diag = new double[dim];
        for (int i = 0; i < dim; i++) {
            if (i % nVar == 0) {
                diag[i] = 0;
            } else {
                diag[i] = m_lambda;
            }
        }
        return MatrixUtils.createRealDiagonalMatrix(diag);
    }

}
