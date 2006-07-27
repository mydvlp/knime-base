/*
 * ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2006
 * University of Konstanz, Germany.
 * Chair for Bioinformatics and Information Mining
 * Prof. Dr. Michael R. Berthold
 *
 * You may not modify, publish, transmit, transfer or sell, reproduce,
 * create derivative works from, distribute, perform, display, or in
 * any way exploit any of the content, in whole or in part, except as
 * otherwise expressly permitted in writing by the copyright owner or
 * as specified in the license file distributed with this product.
 *
 * If you have any quesions please contact the copyright holder:
 * website: www.knime.org
 * email: contact@knime.org
 * -------------------------------------------------------------------
 * 
 * History
 *   27.04.2005 (ohl): created
 */
package de.unikn.knime.base.node.io.filereader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import junit.framework.TestCase;
import de.unikn.knime.base.node.io.filetokenizer.Quote;

/**
 * 
 * @author Peter Ohl, University of Konstanz
 */
public class FileAnalyzerTest extends TestCase {

    /**
     * Tests the analyze TODO: test all features!
     */
    public void testAnalyze() {
        colHeaderTest();
        rowHeaderTest();
        quoteTest();
        bug107Test();
    }

    /*
     * tests if column headers are correctly detected. The file has col headers,
     * if the first line contains a non-empty string in each column that has an
     * increasing number at its end (increasing over the different cols).
     */
    private void colHeaderTest() {

        URL url;
        FileReaderNodeSettings settings;
        FileReaderNodeSettings analSettings;

        try {
            /*
             * give it some nice column headers in the first line
             */
            url = initTempFile("column1,column2,column3,column4\n"
                    + "foo,poo,moo,zoo\n" + "oof,oop,oom,ooz");
            settings = new FileReaderNodeSettings();
            settings.setDataFileLocationAndUpdateTableName(url);
            analSettings = FileAnalyzer.analyze(settings);
            assertTrue(analSettings.getFileHasColumnHeaders());
            // just to be on the safe side
            assertEquals(analSettings.getNumberOfColumns(), 4);
            /*
             * just numbers will be considered col headers
             */
            url = initTempFile("1,2,3,4\n" + "foo,poo,moo,zoo\n"
                    + "oof,oop,oom,ooz");
            settings = new FileReaderNodeSettings();
            settings.setDataFileLocationAndUpdateTableName(url);
            analSettings = FileAnalyzer.analyze(settings);
            assertTrue(analSettings.getFileHasColumnHeaders());
            // just to be on the safe side
            assertEquals(analSettings.getNumberOfColumns(), 4);
            /*
             * numbers not increasing: no headers
             */
            url = initTempFile("1,2,5,4\n" + "foo,poo,moo,zoo\n"
                    + "oof,oop,oom,ooz");
            settings = new FileReaderNodeSettings();
            settings.setDataFileLocationAndUpdateTableName(url);
            analSettings = FileAnalyzer.analyze(settings);
            // just to be on the safe side
            assertEquals(analSettings.getNumberOfColumns(), 4);
            assertFalse(analSettings.getFileHasColumnHeaders());
            /*
             * different prefixes: no headers
             */
            url = initTempFile("col1,col2,foo3,col4\n" + "foo,poo,moo,zoo\n"
                    + "oof,oop,oom,ooz");
            settings = new FileReaderNodeSettings();
            settings.setDataFileLocationAndUpdateTableName(url);
            analSettings = FileAnalyzer.analyze(settings);
            // just to be on the safe side
            assertEquals(analSettings.getNumberOfColumns(), 4);
            assertFalse(analSettings.getFileHasColumnHeaders());
            /*
             * different prefixes: no headers
             */
            url = initTempFile("col1,2,foo3,4\n" + "foo,poo,moo,zoo\n"
                    + "oof,oop,oom,ooz");
            settings = new FileReaderNodeSettings();
            settings.setDataFileLocationAndUpdateTableName(url);
            analSettings = FileAnalyzer.analyze(settings);
            // just to be on the safe side
            assertEquals(analSettings.getNumberOfColumns(), 4);
            assertFalse(analSettings.getFileHasColumnHeaders());
            /*
             * different prefixes: no headers
             */
            url = initTempFile("1,col2,3,c4\n" + "foo,poo,moo,zoo\n"
                    + "oof,oop,oom,ooz");
            settings = new FileReaderNodeSettings();
            settings.setDataFileLocationAndUpdateTableName(url);
            analSettings = FileAnalyzer.analyze(settings);
            // just to be on the safe side
            assertEquals(analSettings.getNumberOfColumns(), 4);
            assertFalse(analSettings.getFileHasColumnHeaders());
            /*
             * empty cell: no headers
             */
            url = initTempFile("col1,,col3,col4\n" + "foo,poo,moo,zoo\n"
                    + "oof,oop,oom,ooz");
            settings = new FileReaderNodeSettings();
            settings.setDataFileLocationAndUpdateTableName(url);
            analSettings = FileAnalyzer.analyze(settings);
            // just to be on the safe side
            assertEquals(analSettings.getNumberOfColumns(), 4);
            assertFalse(analSettings.getFileHasColumnHeaders());
            /*
             * empty cell: no headers
             */
            url = initTempFile(",col1,col3,col4\n" + "foo,poo,moo,zoo\n"
                    + "oof,oop,oom,ooz");
            settings = new FileReaderNodeSettings();
            settings.setDataFileLocationAndUpdateTableName(url);
            analSettings = FileAnalyzer.analyze(settings);
            // just to be on the safe side
            assertEquals(analSettings.getNumberOfColumns(), 4);
            assertFalse(analSettings.getFileHasColumnHeaders());
            /*
             * don't choke on a huge index
             */
            url = initTempFile("col9999999999999999999999999999999999999999999,"
                    + "col9999999999999999999999999999999999999999999999999998,"
                    + "col3,"
                    + "col999999999999999999999999999999999999999999999999997\n"
                    + "foo,poo,moo,zoo\n" + "oof,oop,oom,ooz");
            settings = new FileReaderNodeSettings();
            settings.setDataFileLocationAndUpdateTableName(url);
            analSettings = FileAnalyzer.analyze(settings);
            // just to be on the safe side
            assertEquals(analSettings.getNumberOfColumns(), 4);
            assertFalse(analSettings.getFileHasColumnHeaders());

        } catch (IOException ioe) {
            // if this goes off the temp file couldn't be created.
            assertTrue(false);
        }
    }

    /*
     * makes sure double quotes and single quotes are only supported when they
     * appear in even numbers.
     */
    private void quoteTest() {

        URL url;
        FileReaderNodeSettings settings;
        FileReaderNodeSettings analSettings;

        try {
            /*
             * nice quoting
             */
            url = initTempFile("\"col1\",'col2',col3,col4\n"
                    + "\"foo\",poo,\"moo\",zoo\n" + "oof,'oo p',oom,' ooz '");
            settings = new FileReaderNodeSettings();
            settings.setDataFileLocationAndUpdateTableName(url);
            analSettings = FileAnalyzer.analyze(settings);
            assertTrue(analSettings.getFileHasColumnHeaders());
            assertEquals(analSettings.getNumberOfColumns(), 4);
            Vector quotes = analSettings.getAllQuotes();
            // we support '"' and '
            assertEquals(quotes.size(), 2);
            assertEquals(((Quote)quotes.get(0)).getLeft(), "\"");
            assertEquals(((Quote)quotes.get(0)).getRight(), "\"");
            assertTrue(((Quote)quotes.get(0)).hasEscapeChar());
            assertEquals(((Quote)quotes.get(0)).getEscape(), '\\');
            assertEquals(((Quote)quotes.get(1)).getLeft(), "'");
            assertEquals(((Quote)quotes.get(1)).getRight(), "'");
            assertTrue(((Quote)quotes.get(1)).hasEscapeChar());
            assertEquals(((Quote)quotes.get(1)).getEscape(), '\\');

            /*
             * the tick (') is part of the data - don't consider it a quote (it
             * must show up an odd number of times...)
             */
            url = initTempFile("\"col1\",col2,col3,col4\n"
                    + "\"foo\",poo,\"moo\",zoo\n" + "oo'f,o'op,o'om,ooz");
            settings = new FileReaderNodeSettings();
            settings.setDataFileLocationAndUpdateTableName(url);
            analSettings = FileAnalyzer.analyze(settings);
            assertTrue(analSettings.getFileHasColumnHeaders());
            assertEquals(analSettings.getNumberOfColumns(), 4);
            quotes = analSettings.getAllQuotes();
            // we support '"' still
            assertEquals(quotes.size(), 1);
            assertEquals(((Quote)quotes.get(0)).getLeft(), "\"");
            assertEquals(((Quote)quotes.get(0)).getRight(), "\"");
            assertTrue(((Quote)quotes.get(0)).hasEscapeChar());
            assertEquals(((Quote)quotes.get(0)).getEscape(), '\\');
            /*
             * there is also a single double quote in the data
             */
            url = initTempFile("\"col1,col2,col3,col4\n"
                    + "fo\"o,poo,moo,zoo\n" + "oo'f,o'op,o'om,ooz");
            settings = new FileReaderNodeSettings();
            settings.setDataFileLocationAndUpdateTableName(url);
            analSettings = FileAnalyzer.analyze(settings);
            assertEquals(analSettings.getNumberOfColumns(), 4);
            assertEquals(analSettings.getAllQuotes().size(), 0);

            /*
             * don't stumble over escaped quotes
             */
            url = initTempFile("col1,col2,col3,col4\n"
                    + "\"foo\",\"po\\\"o\",moo,zoo\n" + "oo'f,o'op,o'om,ooz");
            settings = new FileReaderNodeSettings();
            settings.setDataFileLocationAndUpdateTableName(url);
            analSettings = FileAnalyzer.analyze(settings);
            assertEquals(analSettings.getNumberOfColumns(), 4);
            // we must support the double quotes with the escape char
            quotes = analSettings.getAllQuotes();
            assertEquals(quotes.size(), 1);
            assertEquals(((Quote)quotes.get(0)).getLeft(), "\"");
            assertEquals(((Quote)quotes.get(0)).getRight(), "\"");
            assertTrue(((Quote)quotes.get(0)).hasEscapeChar());
            assertEquals(((Quote)quotes.get(0)).getEscape(), '\\');

        } catch (IOException ioe) {
            // if this goes off the temp file couldn't be created.
            assertTrue(false);
        }

    }

    /*
     * tests if row headers are correctly detected. The file has row headers, if
     * the first column of each row contains a non-empty string in each column
     * that consists of the same prefix with an increasing number following
     * (increasing over the different rows).
     */
    private void rowHeaderTest() {

        URL url;
        FileReaderNodeSettings settings;
        FileReaderNodeSettings analSettings;

        try {
            /*
             * test nice row header
             */
            url = initTempFile("\"\",col0,col1,col2\n" + "row1,poo,moo,zoo\n"
                    + "row2,oop,oom,ooz");
            settings = new FileReaderNodeSettings();
            settings.setDataFileLocationAndUpdateTableName(url);
            analSettings = FileAnalyzer.analyze(settings);
            // just to be on the safe side
            assertEquals(analSettings.getNumberOfColumns(), 3);
            assertTrue(analSettings.getFileHasRowHeaders());

            /*
             * again nice row headers.
             */
            url = initTempFile(",col0,col1,col2\n" + "row1,poo,moo,zoo\n"
                    + "row2,poo,moo,zoo\n" + "row3,poo,moo,zoo\n"
                    + "row4,oop,oom,ooz");
            settings = new FileReaderNodeSettings();
            settings.setDataFileLocationAndUpdateTableName(url);
            analSettings = FileAnalyzer.analyze(settings);
            // just to be on the safe side
            assertEquals(analSettings.getNumberOfColumns(), 3);
            assertTrue(analSettings.getFileHasRowHeaders());

            /*
             * these are not row headers.
             */
            url = initTempFile(",col0,col1,col2\n" + "row1,poo,moo,zoo\n"
                    + "row2,poo,moo,zoo\n" + "row2.5,poo,moo,zoo\n"
                    + "row3,oop,oom,ooz");
            settings = new FileReaderNodeSettings();
            settings.setDataFileLocationAndUpdateTableName(url);
            analSettings = FileAnalyzer.analyze(settings);
            // just to be on the safe side
            assertEquals(analSettings.getNumberOfColumns(), 4);
            assertFalse(analSettings.getFileHasRowHeaders());

            /*
             * don't choke on a huge indecies
             */
            url = initTempFile("\"\",col0,col1,col2\n"
                    + "row9999999999999999999999999999999999999999999999999998,"
                    + "poo,moo,zoo\n"
                    + "row9999999999999999999999999999999999999999999999999999,"
                    + "oop,oom,ooz");
            settings = new FileReaderNodeSettings();
            settings.setDataFileLocationAndUpdateTableName(url);
            analSettings = FileAnalyzer.analyze(settings);
            // just to be on the safe side
            assertEquals(analSettings.getNumberOfColumns(), 4);
            assertFalse(analSettings.getFileHasRowHeaders());

        } catch (IOException ioe) {
            // if this goes off the temp file couldn't be created.
            assertTrue(false);
        }
    }

    /*
     * that's a file causing the analyze to puke and die. Lets make sure it
     * doesn't cause any harme in the future.
     */
    private void bug107Test() {

        URL url;
        FileReaderNodeSettings settings;
        FileReaderNodeSettings analSettings;

        try {
            /*
             * test nice row header
             */
            url = initTempFile("Covariance: =\n"
            + " 0.6856935123 -0.0424340045 1.2743154362 0.5162706935\n"
            + " -0.0424340045 0.1899794183 -0.3296563758 -0.1216393736\n"
            + " 1.2743154362 -0.3296563758 3.1162778523 1.2956093960\n"
            + " 0.5162706935 -0.1216393736 1.2956093960 0.5810062640\n"
            + "\n"
            + "\n"
            + "V:  =\n"
            + " -0.3154871929 -0.5820298513 -0.6565887713 0.3613865918\n"
            + " 0.3197231037 0.5979108301 -0.7301614348 -0.0845225141\n"
            + " 0.4798389870 0.0762360758 0.1733726628 0.8566706059\n"
            + " -0.7536574253 0.5458314320 0.0754810199 0.3582891972\n"
            + "\n"
            + "\n"
            + "sorted V:  =\n"
            + " 0.3613865918 -0.6565887713 -0.5820298513 -0.3154871929\n"
            + " -0.0845225141 -0.7301614348 0.5979108301 0.3197231037\n"
            + " 0.8566706059 0.1733726628 0.0762360758 0.4798389870\n"
            + " 0.3582891972 0.0754810199 0.5458314320 -0.7536574253");
            settings = new FileReaderNodeSettings();
            settings.setDataFileLocationAndUpdateTableName(url);
            analSettings = FileAnalyzer.analyze(settings);
            assertNotNull(analSettings);
        } catch (IOException ioe) {
            // if this goes off the temp file couldn't be created.
            assertTrue(false);
        }

    }
    
    /**
     * Creates a temp file, writes the string into it and closes it again. It
     * will return an URL to that file then.
     * 
     * @param contents the string written into the newly created temp file.
     * @return url to the created file
     * @throws IOException If the file be instantiated or written.
     */
    private URL initTempFile(final String contents) throws IOException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmssSSS");
        String date = sdf.format(new Date());
        String fileName = "knime_fileanalyzer_test_" + date + "_";
        String suffix = ".dat";

        File tempFile = File.createTempFile(fileName, suffix);
        tempFile.deleteOnExit();

        FileWriter out = new FileWriter(tempFile);
        out.write(contents);
        out.close();

        return tempFile.toURL();

    } // initTempFile()

}
