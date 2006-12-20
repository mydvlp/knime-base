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
 */
package org.knime.base.node.io.csvwriter;

import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;


/**
 * Dialog to choose a file for csv output.
 * 
 * @author Bernd Wiswedel, University of Konstanz
 */
public class CSVWriterNodeDialog extends NodeDialogPane {

    /** textfield to enter file name. */
    private final CSVFilesHistoryPanel m_textBox;

    /** Checkbox for writing column header. */
    private final JCheckBox m_colHeaderChecker;

    /** Checkbox for writing column header even if file exits. */
    private final JCheckBox m_colHeaderWriteSkipOnAppend;
    
    /** Checkbox for writing column header. */
    private final JCheckBox m_rowHeaderChecker;

    /** Checkbox if append to output file (if exists). */
    private final JCheckBox m_appendChecker;

    /** text field for missing pattern. */
    private final JTextField m_missingField;

    /**
     * Creates a new CSV writer dialog.
     */
    public CSVWriterNodeDialog() {
        super();

        final JPanel filePanel = new JPanel();
        filePanel.setLayout(new BoxLayout(filePanel, BoxLayout.X_AXIS));
        filePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory
                .createEtchedBorder(), "Output file location:"));
        m_textBox = new CSVFilesHistoryPanel();
        filePanel.add(m_textBox);
        filePanel.add(Box.createHorizontalGlue());

        final JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        optionsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory
                .createEtchedBorder(), "Writer options:"));

        ItemListener l = new ItemListener() {
            public void itemStateChanged(final ItemEvent e) {
                checkCheckerState();
            }
        };
        m_colHeaderChecker = new JCheckBox("Write column header");
        m_colHeaderChecker.addItemListener(l);
        m_colHeaderWriteSkipOnAppend = new JCheckBox(
                "Skip column header when appended");
        m_rowHeaderChecker = new JCheckBox("Write row header");
        m_appendChecker = new JCheckBox("Append to output file");
        m_appendChecker.addItemListener(l);
        m_missingField = new JTextField(3);
        m_missingField.setMaximumSize(new Dimension(40, 20));
        m_missingField.setToolTipText(
                "Pattern for missing values. If unsure, simply leave empty");
        final JPanel missingPanel = new JPanel();
        missingPanel.setLayout(new BoxLayout(missingPanel, BoxLayout.X_AXIS));
        missingPanel.add(m_missingField);
        missingPanel.add(new JLabel(" Missing Pattern"));
        missingPanel.add(Box.createHorizontalGlue());

        final JPanel colHeaderPane = new JPanel();
        colHeaderPane.setLayout(new BoxLayout(colHeaderPane, BoxLayout.X_AXIS));
        colHeaderPane.add(m_colHeaderChecker);
        colHeaderPane.add(Box.createHorizontalGlue());
        final JPanel colHeaderPane2 = new JPanel();
        colHeaderPane2.setLayout(
                new BoxLayout(colHeaderPane2, BoxLayout.X_AXIS));
        colHeaderPane2.add(m_colHeaderWriteSkipOnAppend);
        colHeaderPane2.add(Box.createHorizontalGlue());
        final JPanel rowHeaderPane = new JPanel();
        rowHeaderPane.setLayout(new BoxLayout(rowHeaderPane, BoxLayout.X_AXIS));
        rowHeaderPane.add(m_rowHeaderChecker);
        rowHeaderPane.add(Box.createHorizontalGlue());
        final JPanel appendPane = new JPanel();
        appendPane.setLayout(new BoxLayout(appendPane, BoxLayout.X_AXIS));
        appendPane.add(m_appendChecker);
        appendPane.add(Box.createHorizontalGlue());

        optionsPanel.add(missingPanel);
        optionsPanel.add(Box.createVerticalStrut(5));
        optionsPanel.add(colHeaderPane);
        optionsPanel.add(Box.createVerticalStrut(5));
        optionsPanel.add(colHeaderPane2);
        optionsPanel.add(Box.createVerticalStrut(5));
        optionsPanel.add(rowHeaderPane);
        optionsPanel.add(Box.createVerticalStrut(5));
        optionsPanel.add(appendPane);
        optionsPanel.add(Box.createVerticalStrut(5));
        
        final JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(filePanel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(optionsPanel);
        panel.add(Box.createVerticalGlue());
        addTab("Settings", panel);
    }
    
    /** Checks whether or not the "on file exists" check should be enabled. */
    private void checkCheckerState() {
        m_colHeaderWriteSkipOnAppend.setEnabled(
                m_colHeaderChecker.isSelected() 
                && m_appendChecker.isSelected());
    }

    /**
     * @see NodeDialogPane#loadSettingsFrom(NodeSettingsRO, DataTableSpec[])
     */
    @Override
    protected void loadSettingsFrom(final NodeSettingsRO settings,
            final DataTableSpec[] specs) throws NotConfigurableException {
        String fileName = settings.getString(CSVWriterNodeModel.CFGKEY_FILE,
                null);
        boolean writeColHeader = settings.getBoolean(
                CSVWriterNodeModel.CFGKEY_COLHEADER, true);
        boolean colHeaderWriteSkipOnAppend = settings.getBoolean(
                CSVWriterNodeModel.CFGKEY_COLHEADER_SKIP_ON_APPEND, true);
        boolean writeRowHeader = settings.getBoolean(
                CSVWriterNodeModel.CFGKEY_ROWHEADER, true);
        boolean isAppend = settings.getBoolean(
                CSVWriterNodeModel.CFGKEY_APPEND, true);
        String missing = settings.getString(CSVWriterNodeModel.CFGKEY_MISSING,
                "");
        m_textBox.updateHistory();
        m_textBox.setSelectedFile(fileName);
        m_missingField.setText(missing);
        m_colHeaderChecker.setSelected(writeColHeader);
        m_colHeaderWriteSkipOnAppend.setSelected(colHeaderWriteSkipOnAppend);
        m_rowHeaderChecker.setSelected(writeRowHeader);
        m_appendChecker.setSelected(isAppend);
        checkCheckerState();
    }

    /**
     * @see NodeDialogPane#saveSettingsTo(NodeSettingsWO)
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings)
            throws InvalidSettingsException {
        String fileName = m_textBox.getSelectedFile();
        if (!fileName.equals("")) {
            File file = CSVFilesHistoryPanel.getFile(fileName);
            settings.addString(CSVWriterNodeModel.CFGKEY_FILE, file
                    .getAbsolutePath());
        }
        boolean writeColHeader = m_colHeaderChecker.isSelected();
        boolean writeColHeaderOnFileExists = 
            m_colHeaderWriteSkipOnAppend.isSelected();
        boolean writeRowHeader = m_rowHeaderChecker.isSelected();
        boolean isAppend = m_appendChecker.isSelected();
        String missing = m_missingField.getText();
        settings.addString(CSVWriterNodeModel.CFGKEY_MISSING, missing);
        settings.addBoolean(
                CSVWriterNodeModel.CFGKEY_COLHEADER, writeColHeader);
        settings.addBoolean(CSVWriterNodeModel.CFGKEY_COLHEADER_SKIP_ON_APPEND, 
                writeColHeaderOnFileExists);
        settings.addBoolean(
                CSVWriterNodeModel.CFGKEY_ROWHEADER, writeRowHeader);
        settings.addBoolean(
                CSVWriterNodeModel.CFGKEY_APPEND, isAppend);
    }
}
