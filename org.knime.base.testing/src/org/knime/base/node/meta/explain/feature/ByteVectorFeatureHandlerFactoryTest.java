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
 *   Apr 10, 2019 (Adrian Nembach, KNIME GmbH, Konstanz, Germany): created
 */
package org.knime.base.node.meta.explain.feature;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.knime.base.node.meta.explain.feature.ByteVectorFeatureHandlerFactory;
import org.knime.base.node.meta.explain.feature.FeatureHandler;
import org.knime.core.data.DataCell;
import org.knime.core.data.vector.bytevector.DenseByteVectorCell;
import org.knime.core.data.vector.bytevector.DenseByteVectorCellFactory;
import org.knime.core.data.vector.bytevector.SparseByteVectorCellFactory;

/**
 *
 * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
 */
public class ByteVectorFeatureHandlerFactoryTest {

    @Test
    public void testDenseByteVector() throws Exception {
        final DenseByteVectorCellFactory original = new DenseByteVectorCellFactory(10);
        final DenseByteVectorCellFactory sampled = new DenseByteVectorCellFactory(10);

        original.setValue(0, 1);
        original.setValue(2, 2);
        original.setValue(4, 3);

        sampled.setValue(1, 4);
        sampled.setValue(3, 5);
        sampled.setValue(5, 6);

        final FeatureHandler fh = new ByteVectorFeatureHandlerFactory().createFeatureHandler();
        fh.setOriginal(original.createDataCell());
        fh.setSampled(sampled.createDataCell());

        fh.markForReplacement(1);
        fh.markForReplacement(4);
        fh.markForReplacement(5);

        int[] expected = new int[] {1, 4, 2, 0, 0, 6, 0, 0, 0, 0};

        final DataCell replaced = fh.createReplaced();

        assertTrue(replaced instanceof DenseByteVectorCell);
        final DenseByteVectorCell bv = (DenseByteVectorCell)replaced;

        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], bv.get(i));
        }
    }

    @Test
    public void testSparseByteVector() throws Exception {
        final SparseByteVectorCellFactory original = new SparseByteVectorCellFactory(10);
        final SparseByteVectorCellFactory sampled = new SparseByteVectorCellFactory(10);

        original.set(0, (byte)1);
        original.set(2, (byte)2);
        original.set(4, (byte)3);

        sampled.set(1, (byte)4);
        sampled.set(3, (byte)5);
        sampled.set(5, (byte)6);

        final FeatureHandler fh = new ByteVectorFeatureHandlerFactory().createFeatureHandler();
        fh.setOriginal(original.createDataCell());
        fh.setSampled(sampled.createDataCell());

        fh.markForReplacement(1);
        fh.markForReplacement(4);
        fh.markForReplacement(5);

        int[] expected = new int[] {1, 4, 2, 0, 0, 6, 0, 0, 0, 0};

        final DataCell replaced = fh.createReplaced();

        assertTrue(replaced instanceof DenseByteVectorCell);
        final DenseByteVectorCell bv = (DenseByteVectorCell)replaced;

        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], bv.get(i));
        }
    }

}
