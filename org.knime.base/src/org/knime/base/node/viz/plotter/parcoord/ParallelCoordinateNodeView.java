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
 *   27.09.2006 (Fabian Dill): created
 */
package org.knime.base.node.viz.plotter.parcoord;

import org.knime.base.node.viz.plotter.node.DefaultVisualizationNodeView;
import org.knime.core.node.NodeModel;

/**
 * Extends the 
 * {@link org.knime.base.node.viz.plotter.node.DefaultVisualizationNodeView} 
 * since it provides an additional menu to show, fade or hide unhilited lines 
 * (rows).
 * 
 * @author Fabian Dill, University of Konstanz
 */
public class ParallelCoordinateNodeView extends DefaultVisualizationNodeView {

    /**
     * Adds  a show/hide menu to the menu bar.
     * 
     * @param model the node model
     * @param plotter the plotter
     */
    public ParallelCoordinateNodeView(final NodeModel model, 
            final ParallelCoordinatesPlotter plotter) {
        super(model, plotter);
        getJMenuBar().add(plotter.getShowHideMenu());
    }
}
