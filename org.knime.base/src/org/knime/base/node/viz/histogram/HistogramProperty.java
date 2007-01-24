/* 
 * -------------------------------------------------------------------
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
 *    10.01.2007 (Tobias Koetter): created
 */

package org.knime.base.node.viz.histogram;

import javax.swing.ButtonGroup;

/**
 * Used to create a {@link ButtonGroup} in the 
 * {@link AbstractHistogramProperties} class.
 * @author Tobias Koetter, University of Konstanz
 */
public interface HistogramProperty {

    /**
     * @return the label to display
     */
    public String getLabel();
    
    /**
     * @return the id of this option
     */
    public String getID();
 
    /**
     * @return <code>true</code> if this is the default option
     */
    public boolean isDefault();
}
