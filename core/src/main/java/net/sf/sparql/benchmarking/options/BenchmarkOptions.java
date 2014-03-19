/*
Copyright 2011-2014 Cray Inc. All Rights Reserved

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:

 * Redistributions of source code must retain the above copyright
  notice, this list of conditions and the following disclaimer.

 * Redistributions in binary form must reproduce the above copyright
  notice, this list of conditions and the following disclaimer in the
  documentation and/or other materials provided with the distribution.

 * Neither the name Cray Inc. nor the names of its contributors may be
  used to endorse or promote products derived from this software
  without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
 */

package net.sf.sparql.benchmarking.options;

import net.sf.sparql.benchmarking.monitoring.CsvProgressListener;
import net.sf.sparql.benchmarking.monitoring.ProgressListener;
import net.sf.sparql.benchmarking.monitoring.XmlProgressListener;
import net.sf.sparql.benchmarking.runners.mix.DefaultOperationMixRunner;
import net.sf.sparql.benchmarking.runners.operations.DefaultOperationRunner;

/**
 * Options for benchmarks
 * 
 * @author rvesse
 * 
 */
public class BenchmarkOptions extends OptionsImpl {

    /**
     * Default Runs
     */
    public static final int DEFAULT_RUNS = 25;
    /**
     * Default Warmup Runs
     */
    public static final int DEFAULT_WARMUPS = 5;
    /**
     * Default Outliers
     */
    public static final int DEFAULT_OUTLIERS = 1;
    private String csvResultsFile = null;
    private String xmlResultsFile = null;
    private int outliers = DEFAULT_OUTLIERS;
    private boolean allowOverwite = false;
    private ProgressListener csvListener = null;
    private ProgressListener xmlListener = null;
    private int runs = DEFAULT_RUNS;
    private int warmups = DEFAULT_WARMUPS;

    /**
     * Creates new benchmark options
     */
    public BenchmarkOptions() {
        super.setMixRunner(new DefaultOperationMixRunner());
        super.setOperationRunner(new DefaultOperationRunner());
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
        if (file != null) {
            // Remove existing progress listener if changing the results file
            if (!file.equals(this.csvResultsFile) && this.csvListener != null) {
                this.removeListener(this.csvListener);
                this.csvListener = null;
            }

            // Add new progress listener for the results file
            this.csvListener = new CsvProgressListener(file, this.getAllowOverwrite());
            this.addListener(this.csvListener);
        } else if (this.csvListener != null) {
            // Remove existing progress listener
            this.removeListener(this.csvListener);
            this.csvListener = null;
        }
        this.csvResultsFile = file;
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
        if (xmlFile != null) {
            // Remove existing progress listener if changing the results xmlFile
            if (!xmlFile.equals(this.xmlResultsFile) && this.xmlListener != null) {
                this.removeListener(this.xmlListener);
                this.xmlListener = null;
            }

            // Add new progress listener for the results xmlFile
            this.xmlListener = new XmlProgressListener(xmlFile, this.getAllowOverwrite());
            this.addListener(this.xmlListener);
        } else if (this.xmlListener != null) {
            // Remove existing progress listener
            this.removeListener(this.xmlListener);
            this.xmlListener = null;
        }
        this.xmlResultsFile = xmlFile;
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

    /**
     * Sets the number of times the Query Mix will be run
     * 
     * @param runs
     *            Number of Runs
     */
    public void setRuns(int runs) {
        if (runs < 0)
            runs = 1;
        this.runs = runs;
    }

    /**
     * Gets the number of times the operation mix will be run
     * 
     * @return Number of Runs
     */
    public int getRuns() {
        return runs;
    }

    /**
     * Sets the number of times the Query Mix will be run as a warm up prior to
     * actual runs
     * 
     * @param runs
     *            Number of Warmup Runs
     */
    public void setWarmups(int runs) {
        if (runs <= 0)
            runs = 0;
        warmups = runs;
    }

    /**
     * Gets the number of times the Query Mix will be run as a warm up prior to
     * actual runs
     * 
     * @return Number of Warmup Runs
     */
    public int getWarmups() {
        return warmups;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Options> T copy() {
        BenchmarkOptions copy = new BenchmarkOptions();
        this.copyStandardOptions(copy);
        copy.setAllowOverwrite(this.getAllowOverwrite());
        copy.setCsvResultsFile(this.getCsvResultsFile());
        copy.setOutliers(this.getOutliers());
        copy.setRuns(this.getRuns());
        copy.setWarmups(this.getWarmups());
        copy.setXmlResultsFile(this.getXmlResultsFile());

        return (T) copy;
    }
}