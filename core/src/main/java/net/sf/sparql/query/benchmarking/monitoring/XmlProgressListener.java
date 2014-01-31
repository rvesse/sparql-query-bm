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

package net.sf.sparql.query.benchmarking.monitoring;

import java.io.File;
import java.io.PrintWriter;
import java.util.Iterator;

import net.sf.sparql.query.benchmarking.BenchmarkerUtils;
import net.sf.sparql.query.benchmarking.operations.BenchmarkOperation;
import net.sf.sparql.query.benchmarking.operations.BenchmarkOperationMix;
import net.sf.sparql.query.benchmarking.options.BenchmarkOptions;
import net.sf.sparql.query.benchmarking.options.Options;
import net.sf.sparql.query.benchmarking.runners.Runner;
import net.sf.sparql.query.benchmarking.stats.OperationMixRun;
import net.sf.sparql.query.benchmarking.stats.OperationRun;

/**
 * A Progress Listener that generates a XML output file
 * 
 * @author rvesse
 */
public class XmlProgressListener implements ProgressListener {
    private File file;
    private PrintWriter writer;
    private int indent = 0;
    private boolean allowOverwrite = false;

    //@formatter:off
	/**
	 * Constants for Tag and Attribute Names
	 */
	@SuppressWarnings("javadoc")
    public static final String TAG_SPARQL_BENCHMARK = "sparqlBenchmark",
							   TAG_CONFIGURATION = "configuration",
							   TAG_CONFIG_PROPERTY = "property",
							   ATTR_ID = "id",
							   ATTR_NAME = "name",
							   ATTR_VALUE = "value",
							   ATTR_TYPE = "type",
							   TAG_OPERATIONS = "operations",
							   TAG_OPERATION = "operation",
							   TAG_MIX_RUNS = "operationMixRuns",
							   TAG_MIX_RUN = "operationMixRun",
							   TAG_STATS = "statistics",
							   TAG_SUMMARY = "summary",
							   ATTR_RESPONSE_TIME = "responseTime",
							   ATTR_TOTAL_RESPONSE_TIME = "totalResponseTime",
							   ATTR_RUNTIME = "runtime",
							   ATTR_RESULT_COUNT = "resultCount",
							   ATTR_TOTAL_RUNTIME = "totalRuntime",
							   ATTR_ACTUAL_RUNTIME = "actualRuntime",
							   ATTR_ACTUAL_AVG_RUNTIME = "averageActualRuntime",
							   ATTR_MIN_OPERATION_RUNTIME = "minOperationRuntime",
							   ATTR_MAX_OPERATION_RUNTIME = "maxOperationRuntime",
							   ATTR_MIN_MIX_RUNTIME = "minMixRuntime",
							   ATTR_MAX_MIX_RUNTIME = "maxMixRuntime",
							   ATTR_AVG_RUNTIME = "averageRuntime",
							   ATTR_AVG_RUNTIME_GEOM = "averageRuntimeGeometric",
							   ATTR_AVG_RESPONSE_TIME = "averageResponseTime",
							   ATTR_AVG_RESPONSE_TIME_GEOM = "averageResponseTimeGeometric",
							   ATTR_VARIANCE = "variance",
							   ATTR_STD_DEV = "standardDeviation",
							   ATTR_OPS = "operationsPerSecond",
							   ATTR_ACTUAL_OPS = "actualOperationsPerSecond",
							   ATTR_OPH = "operationsPerHour",
							   ATTR_ACTUAL_OPH = "actualOperationsPerHour",
							   ATTR_OMPH = "operationMixesPerHour",
							   ATTR_ACTUAL_OMPH = "actualOperationMixesPerHour",
							   ATTR_FASTEST_OPERATION = "fastestOperation",
							   ATTR_SLOWEST_OPERATION = "slowestOperation",
							   ATTR_RUN_ORDER = "runOrder";
	//@formatter:on

    /**
     * Creates a new XML progress listener which writes to the given path unless
     * it already exists
     * 
     * @param outputPath
     *            Output File Path
     */
    public XmlProgressListener(String outputPath) {
        this(outputPath, false);
    }

    /**
     * Constructor to be called when the file to write to is known in advance of
     * benchmarking
     * 
     * @param outputPath
     *            Output File Path
     * @param allowOverwrite
     *            Whether overwriting an existing file is permitted
     */
    public XmlProgressListener(String outputPath, boolean allowOverwrite) {
        this.setup(outputPath, allowOverwrite);
    }

    /**
     * Sets up the file for output throwing an error if the file cannot be
     * written to
     * 
     * @param outputPath
     *            Output File Path
     */
    private void setup(String outputPath, boolean allowOverwrite) {
        this.file = new File(outputPath);
        this.allowOverwrite = allowOverwrite;
    }

    /**
     * Handles the started event by printing run configuration to the XML file
     */
    @Override
    public <T extends Options> void handleStarted(Runner<T> runner, T options) {
        if (!BenchmarkerUtils.checkFile(this.file, allowOverwrite)) {
            throw new RuntimeException("XML Output File is not a file, already exists or is not writable");
        }

        BenchmarkOptions bOps = null;
        if (options instanceof BenchmarkOptions) {
            bOps = (BenchmarkOptions) options;
        }

        try {
            // Open Print Writer
            writer = new PrintWriter(file);

            // Generate XML Header
            writer.println("<?xml version=\"1.0\"?>");
            openTag(TAG_SPARQL_BENCHMARK);

            // Generate an <configuration> element detailing configuration
            openTag(TAG_CONFIGURATION);
            printProperty("endpoint", options.getQueryEndpoint());
            if (bOps != null) {
                printProperty("sanityChecking", bOps.getSanityCheckLevel());
            }
            printProperty("warmups", options.getWarmups());
            printProperty("runs", options.getRuns());
            printProperty("randomOrder", options.getRandomizeOrder());
            if (bOps != null) {
                printProperty("outliers", bOps.getOutliers());
            }
            printProperty("timeout", options.getTimeout());
            printProperty("maxDelay", options.getMaxDelay());
            printProperty("askFormat", options.getResultsAskFormat());
            printProperty("graphFormat", options.getResultsGraphFormat());
            printProperty("selectFormat", options.getResultsSelectFormat());
            printProperty("threads", options.getParallelThreads());
            if (bOps != null) {
                printProperty("counting", !bOps.getNoCount());
                printProperty("limit", bOps.getLimit());
            }
            printProperty("gzip", options.getAllowGZipEncoding());
            printProperty("deflate", options.getAllowDeflateEncoding());

            // Print Queries
            openTag(TAG_OPERATIONS);
            BenchmarkOperationMix mix = options.getOperationMix();
            Iterator<BenchmarkOperation> ops = mix.getOperations();
            int id = 0;
            while (ops.hasNext()) {
                BenchmarkOperation op = ops.next();
                openTag(TAG_OPERATION, true);
                addAttribute(ATTR_ID, id);
                addAttribute(ATTR_NAME, op.getName());
                addAttribute(ATTR_TYPE, op.getType());
                id++;
                finishAttributes();

                openCData();
                writer.print(op.getContentString());
                closeCData();
                closeTag(TAG_OPERATION);
            }
            closeTag(TAG_OPERATIONS);

            closeTag(TAG_CONFIGURATION);

            // Open Tag for Mix Run stats
            openTag(TAG_MIX_RUNS);

            writer.flush();
        } catch (Exception e) {
            System.err.println("Unexpected error writing XML stats");
            runner.halt(options, e.getMessage());
        }
    }

    protected void printProperty(String name, int value) {
        printProperty(name, Integer.toString(value));
    }

    protected void printProperty(String name, long value) {
        printProperty(name, Long.toString(value));
    }

    protected void printProperty(String name, boolean value) {
        printProperty(name, Boolean.toString(value));
    }

    protected void printProperty(String name, String value) {
        openTag(TAG_CONFIG_PROPERTY, true);
        addAttribute(ATTR_NAME, name);
        if (value == null)
            value = "";
        addAttribute(ATTR_VALUE, value);
        finishAttributes(true);
    }

    /**
     * Handles the finished event by printing statistics to the XML file
     * 
     * @param ok
     *            Whether benchmarking finished OK
     */
    @Override
    public <T extends Options> void handleFinished(Runner<T> runner, T options, boolean ok) {
        if (writer == null)
            throw new RuntimeException(
                    "handleFinished() on XmlProgressListener was called but it appears handleStarted() was never called, another listener may have caused handleStarted() to be bypassed for this listener");

        if (!BenchmarkerUtils.checkFile(this.file, allowOverwrite)) {
            throw new RuntimeException("XML Output File is not a file, already exists or is not writable");
        }

        try {
            closeTag(TAG_MIX_RUNS);

            openTag(TAG_STATS);

            boolean wasMultithreaded = options.getParallelThreads() > 1;

            // Query Summary
            openTag(TAG_OPERATIONS);
            BenchmarkOperationMix mix = options.getOperationMix();
            Iterator<BenchmarkOperation> ops = mix.getOperations();
            int id = 0;
            while (ops.hasNext()) {
                BenchmarkOperation op = ops.next();
                openTag(TAG_OPERATION, true);

                // Per-operation summary
                addAttribute(ATTR_ID, id);
                addAttribute(ATTR_NAME, op.getName());
                addAttribute(ATTR_TYPE, op.getType());
                addAttribute(ATTR_TOTAL_RESPONSE_TIME, op.getTotalResponseTime());
                addAttribute(ATTR_AVG_RESPONSE_TIME, op.getAverageResponseTime());
                addAttribute(ATTR_TOTAL_RUNTIME, op.getTotalRuntime());
                if (wasMultithreaded)
                    addAttribute(ATTR_ACTUAL_RUNTIME, op.getActualRuntime());
                addAttribute(ATTR_AVG_RUNTIME, op.getAverageRuntime());
                if (wasMultithreaded)
                    addAttribute(ATTR_ACTUAL_AVG_RUNTIME, op.getActualAverageRuntime());
                addAttribute(ATTR_AVG_RUNTIME_GEOM, op.getGeometricAverageRuntime());
                addAttribute(ATTR_MIN_OPERATION_RUNTIME, op.getMinimumRuntime());
                addAttribute(ATTR_MAX_OPERATION_RUNTIME, op.getMaximumRuntime());
                addAttribute(ATTR_VARIANCE, op.getVariance());
                addAttribute(ATTR_STD_DEV, op.getStandardDeviation());
                addAttribute(ATTR_OPS, op.getOperationsPerSecond());
                if (wasMultithreaded)
                    addAttribute(ATTR_ACTUAL_OPS, op.getActualOperationsPerSecond());
                addAttribute(ATTR_OPH, op.getOperationsPerHour());
                if (wasMultithreaded)
                    addAttribute(ATTR_ACTUAL_OPH, op.getActualOperationsPerHour());
                finishAttributes(true);

                id++;
            }
            closeTag(TAG_OPERATIONS);

            // Overall Summary
            openTag(TAG_SUMMARY, true);
            addAttribute(ATTR_TOTAL_RESPONSE_TIME, mix.getTotalResponseTime());
            addAttribute(ATTR_AVG_RESPONSE_TIME, mix.getAverageResponseTime());
            addAttribute(ATTR_TOTAL_RUNTIME, mix.getTotalRuntime());
            if (wasMultithreaded)
                addAttribute(ATTR_ACTUAL_RUNTIME, mix.getActualRuntime());
            addAttribute(ATTR_AVG_RUNTIME, mix.getAverageRuntime());
            if (wasMultithreaded)
                addAttribute(ATTR_ACTUAL_AVG_RUNTIME, mix.getActualAverageRuntime());
            addAttribute(ATTR_AVG_RUNTIME_GEOM, mix.getGeometricAverageRuntime());
            addAttribute(ATTR_MIN_MIX_RUNTIME, mix.getMinimumRuntime());
            addAttribute(ATTR_MAX_MIX_RUNTIME, mix.getMaximumRuntime());
            addAttribute(ATTR_VARIANCE, mix.getVariance());
            addAttribute(ATTR_STD_DEV, mix.getStandardDeviation());
            addAttribute(ATTR_OMPH, mix.getOperationMixesPerHour());
            if (wasMultithreaded)
                addAttribute(ATTR_ACTUAL_OMPH, mix.getActualOperationMixesPerHour());
            finishAttributes(true);

            closeTag(TAG_STATS);
            closeTag(TAG_SPARQL_BENCHMARK);

            writer.flush();
            writer.close();
        } catch (Exception e) {
            System.err.println("Unexpected error writing XML stats " + e);
            e.printStackTrace();
            if (options.getHaltAny() || options.getHaltOnError())
                runner.halt(options, e.getMessage());
        }
    }

    @Override
    public <T extends Options> void handleProgress(Runner<T> runner, T options, String message) {
        // Not relevant for XML output
    }

    @Override
    public <T extends Options> void handleProgress(Runner<T> runner, T options, BenchmarkOperation operation, OperationRun run) {
        // We don't handle individual operation run stats, we only handle mix
        // and
        // aggregate stats
    }

    @Override
    public synchronized <T extends Options> void handleProgress(Runner<T> runner, T options, OperationMixRun run) {
        // Print run information
        openTag(TAG_MIX_RUN, true);

        this.addAttribute(ATTR_RUN_ORDER, run.getRunOrder());
        this.addAttribute(ATTR_TOTAL_RESPONSE_TIME, run.getTotalResponseTime());
        this.addAttribute(ATTR_TOTAL_RUNTIME, run.getTotalRuntime());
        this.addAttribute(ATTR_MIN_OPERATION_RUNTIME, run.getMinimumRuntime());
        this.addAttribute(ATTR_MAX_OPERATION_RUNTIME, run.getMaximumRuntime());
        this.addAttribute(ATTR_FASTEST_OPERATION, run.getMinimumRuntimeOperationID());
        this.addAttribute(ATTR_SLOWEST_OPERATION, run.getMaximumRuntimeOperationID());

        finishAttributes();

        Iterator<OperationRun> rs = run.getRuns();
        int id = 0;
        while (rs.hasNext()) {
            OperationRun r = rs.next();
            openTag(TAG_OPERATION, true);
            addAttribute(ATTR_ID, id);
            id++;
            addAttribute(ATTR_RUN_ORDER, r.getRunOrder());
            addAttribute(ATTR_RESPONSE_TIME, r.getResponseTime());
            addAttribute(ATTR_RUNTIME, r.getRuntime());
            addAttribute(ATTR_RESULT_COUNT, r.getResultCount());

            if (r.getErrorMessage() != null) {
                finishAttributes();
                writer.print(escape(r.getErrorMessage()));
                closeTag(TAG_OPERATION);
            } else {
                finishAttributes(true);
            }
        }

        closeTag(TAG_MIX_RUN);

        writer.flush();
    }

    private void openTag(String tagname) {
        openTag(tagname, false);
    }

    private void openTag(String tagname, boolean allowAttributes) {
        if (allowAttributes) {
            writer.print(indent() + "<" + tagname);
        } else {
            writer.println(indent() + "<" + tagname + ">");
            indent++;
        }
    }

    private void closeTag(String tagname) {
        indent--;
        writer.println(indent() + "</" + tagname + ">");
    }

    private void openCData() {
        indent++;
        writer.println(indent() + "<![CDATA[");
    }

    private void closeCData() {
        writer.println("]]>");
        indent--;
    }

    private void addAttribute(String attr, int value) {
        addAttribute(attr, Integer.toString(value));
    }

    private void addAttribute(String attr, long value) {
        addAttribute(attr, Long.toString(value));
    }

    private void addAttribute(String attr, double value) {
        addAttribute(attr, Double.toString(value));
    }

    private void addAttribute(String attr, String value) {
        writer.print(' ');
        writer.print(attr);
        writer.print('=');
        writer.print('"');
        writer.print(escapeForAttribute(value));
        writer.print('"');
    }

    private void finishAttributes() {
        finishAttributes(false);
    }

    private void finishAttributes(boolean closeTag) {
        if (closeTag)
            writer.print(" /");
        writer.println(">");
        if (!closeTag)
            indent++;
    }

    @SuppressWarnings("unused")
    private void printTag(String tagname, String value) {
        writer.println(indent() + "<" + tagname + ">" + escape(value) + "</" + tagname + ">");
    }

    private String indent() {
        if (indent == 0)
            return "";
        StringBuffer buffer = new StringBuffer();
        int i = 0;
        while (i < indent) {
            buffer.append(' ');
            i++;
        }
        return buffer.toString();
    }

    private String escape(String string) {
        final StringBuilder sb = new StringBuilder(string);

        int offset = 0;
        String replacement;
        char found;
        for (int i = 0; i < string.length(); i++) {
            found = string.charAt(i);

            switch (found) {
            case '&':
                replacement = "&amp;";
                break;
            case '<':
                replacement = "&lt;";
                break;
            case '>':
                replacement = "&gt;";
                break;
            case '\r':
                replacement = "&#x0D;";
                break;
            case '\n':
                replacement = "&#x0A;";
                break;
            default:
                replacement = null;
            }

            if (replacement != null) {
                sb.replace(offset + i, offset + i + 1, replacement);
                offset += replacement.length() - 1; // account for added chars
            }
        }

        return sb.toString();
    }

    private String escapeForAttribute(String string) {
        string = escape(string);
        return string.replace("\"", "&quot;");
    }
}
