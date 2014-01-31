/** 
 * Copyright 2011-2012 Cray Inc. All Rights Reserved
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name Cray Inc. nor the names of its contributors may be
 *   used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 **/

package net.sf.sparql.query.benchmarking.options;

import net.sf.sparql.query.benchmarking.monitoring.CsvProgressListener;
import net.sf.sparql.query.benchmarking.monitoring.ProgressListener;
import net.sf.sparql.query.benchmarking.monitoring.XmlProgressListener;

/**
 * Benchmarker class that is used to create and run a Benchmark
 * 
 * @author rvesse
 * 
 */
public class BenchmarkOptions extends OptionsImpl {

    /**
     * Default Outliers
     */
    public static final int DEFAULT_OUTLIERS = 1;
    /**
     * Default CSV Results File
     */
    public static final String DEFAULT_CSV_RESULTS_FILE = "results.csv";
    /**
     * Default XML Results File
     */
    public static final String DEFAULT_XML_RESULTS_FILE = "results.xml";
    /**
     * Default Sanity Checks
     */
    public static final int DEFAULT_SANITY_CHECKS = 2;
    /**
     * Default Limit, values <= 0 are considered to mean leave existing LIMIT
     * as-is and don't impose a limit on unlimited queries
     */
    public static final long DEFAULT_LIMIT = 0;

    private String csvResultsFile = DEFAULT_CSV_RESULTS_FILE;
    private String xmlResultsFile = DEFAULT_XML_RESULTS_FILE;
    private int outliers = DEFAULT_OUTLIERS;
    private int sanity = DEFAULT_SANITY_CHECKS;
    private long limit = DEFAULT_LIMIT;
    private boolean noCount = false;
    private boolean allowOverwite = false;
    private ProgressListener csvListener = null;
    private ProgressListener xmlListener = null;

    /**
     * Creates a new Benchmarker
     * <p>
     * A Benchmarker will always at least generate CSV and XML output unless you
     * disable this by removing the {@link CsvProgressListener} and
     * {@link XmlProgressListener} using the
     * {@link #removeListener(ProgressListener)} method
     * </p>
     */
    public BenchmarkOptions() {
        // By default a Benchmarker is always configured to generate CSV and XML
        // output
        this.addListener(this.csvListener);
        this.addListener(this.xmlListener);
    }

    /**
     * Sets the number of outliers to be discarded
     * 
     * @param outliers
     *            Number of outliers
     */
    public void setOutliers(int outliers) {
        if (outliers < 0)
            outliers = 0;
        if (outliers > this.getRuns() / 2)
            throw new IllegalArgumentException("Cannot set outliers to be more than half the number of runs");
        this.outliers = outliers;
    }

    /**
     * Gets the number of outliers to be discarded
     * 
     * @return Number of outliers
     */
    public int getOutliers() {
        return outliers;
    }

    /**
     * Sets the CSV Results File
     * 
     * @param file
     *            Filename for CSV Results, null disables CSV results
     */
    public void setCsvResultsFile(String file) {
        if (csvResultsFile == null && file != null) {
            // Add CsvProgressListener if not already present
            this.csvListener = new CsvProgressListener(file, this.getAllowOverwrite());
            this.addListener(this.csvListener);
        }
        csvResultsFile = file;
        if (file == null && this.csvListener != null) {
            // Remove CsvProgressListener if present
            this.removeListener(this.csvListener);
            this.csvListener = null;
        }
    }

    /**
     * Gets the XML Result File
     * 
     * @return Filename for XML Results
     */
    public String getXmlResultsFile() {
        return xmlResultsFile;
    }

    /**
     * Sets the XML Results File
     * 
     * @param xmlFile
     *            Filename for XML Results, null disables XML results
     */
    public void setXmlResultsFile(String xmlFile) {
        if (xmlResultsFile == null && xmlFile != null) {
            // Add XmlProgressListener if not already present
            this.xmlListener = new XmlProgressListener(xmlFile, this.getAllowOverwrite());
            this.addListener(this.xmlListener);
        }
        xmlResultsFile = xmlFile;
        if (xmlFile == null && this.xmlListener != null) {
            // Remove XmlProgressListener if present
            this.removeListener(this.xmlListener);
            this.xmlListener = null;
        }
    }

    /**
     * Gets the CSV Result File
     * 
     * @return Filename for CSV Results
     */
    public String getCsvResultsFile() {
        return csvResultsFile;
    }

    /**
     * Sets the Sanity Checking level
     * 
     * @param level
     *            Sanity Check Level
     */
    public void setSanityCheckLevel(int level) {
        sanity = level;
    }

    /**
     * Gets the Sanity Checking Level
     * 
     * @return Sanity Check Level
     */
    public int getSanityCheckLevel() {
        return sanity;
    }

    /**
     * Sets the LIMIT to impose on queries
     * <p>
     * Values less than or equal to zero mean existing limits are left
     * unchanged, non-zero values will be imposed iff existing limit is greater
     * than the set limit
     * </p>
     * 
     * @param limit
     *            Limit to impose
     */
    public void setLimit(long limit) {
        this.limit = limit;
    }

    /**
     * Gets the LIMIT to impose on queries
     * 
     * @return Limit to impose
     */
    public long getLimit() {
        return limit;
    }

    /**
     * Sets whether query results are counted or just thrown away
     * <p>
     * Currently enabling this only applies to SELECT queries as only SELECT
     * queries stream the results currently, future versions of this tool will
     * also stream CONSTRUCT/DESCRIBE results but this is yet to be implemented
     * </p>
     * 
     * @param noCount
     *            Whether query results are counted
     */
    public void setNoCount(boolean noCount) {
        this.noCount = noCount;
    }

    /**
     * Gets whether query results are counted or just thrown away
     * 
     * @return True if results will not be counted
     */
    public boolean getNoCount() {
        return noCount;
    }

    /**
     * Sets whether {@link ProgressListener} which write to files are allowed to
     * overwrite existing files (default false)
     * 
     * @param allowOverwrite
     *            Whether overwriting existing files is allowed
     */
    public void setAllowOverwrite(boolean allowOverwrite) {
        this.allowOverwite = allowOverwrite;
    }

    /**
     * Gets whether overwriting existing files is allowed
     * 
     * @return Whether overwriting existing files is allowed
     */
    public boolean getAllowOverwrite() {
        return allowOverwite;
    }
}