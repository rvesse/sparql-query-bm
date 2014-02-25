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

package net.sf.sparql.benchmarking.monitoring;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import net.sf.sparql.benchmarking.operations.Operation;
import net.sf.sparql.benchmarking.operations.OperationMix;
import net.sf.sparql.benchmarking.options.BenchmarkOptions;
import net.sf.sparql.benchmarking.options.Options;
import net.sf.sparql.benchmarking.runners.Runner;
import net.sf.sparql.benchmarking.stats.OperationMixRun;
import net.sf.sparql.benchmarking.stats.OperationRun;
import net.sf.sparql.benchmarking.util.ConvertUtils;
import net.sf.sparql.benchmarking.util.FileUtils;
import net.sf.sparql.benchmarking.util.FormatUtils;

import org.apache.log4j.Logger;

/**
 * A Progress Listener that generates a CSV output file
 * 
 * @author rvesse
 * 
 */
public class CsvProgressListener implements ProgressListener {
    private static final Logger logger = Logger.getLogger(CsvProgressListener.class);

    private File f;
    private boolean allowOverwrite = false;
    private StringBuffer buffer;
    private int run = 1;
    private boolean ready = false;

    /**
     * Creates a new CSV progress listener which writes to the given file
     * provided it does not already exist
     * 
     * @param file
     *            File
     */
    public CsvProgressListener(String file) {
        this(file, false);
    }

    /**
     * Creates a new CSV progress listener which writes to the given file
     * optionally overwriting it if it exists
     * 
     * @param file
     *            File
     * @param allowOverwrite
     *            Whether to allow overwrites
     */
    public CsvProgressListener(String file, boolean allowOverwrite) {
        this.f = new File(file);
        this.allowOverwrite = allowOverwrite;
    }

    /**
     * Handles the started event by preparing a record of the run configuration
     * which will eventually be printed to the CSV file
     * 
     * @param runner
     *            Runner
     * @param options
     *            Options
     */
    @Override
    public <T extends Options> void start(Runner<T> runner, T options) {
        if (!FileUtils.checkFile(this.f, this.allowOverwrite)) {
            throw new RuntimeException("CSV Output File is not a file, already exists or is not writable");
        }

        this.buffer = new StringBuffer();
        this.run = 1;

        BenchmarkOptions bOps = null;
        if (options instanceof BenchmarkOptions) {
            bOps = (BenchmarkOptions) options;
        }

        // Information on Benchmark Options
        this.buffer.append("Options Summary,\n");
        this.buffer.append("Query Endpoint," + options.getQueryEndpoint() + "\n");
        this.buffer.append("Update Endpoint," + options.getUpdateEndpoint() + "\n");
        this.buffer.append("Graph Store Endpoint," + options.getGraphStoreEndpoint() + "\n");
        Map<String, String> customEndpoints = options.getCustomEndpoints();
        if (customEndpoints.size() > 0) {
            for (String key : customEndpoints.keySet()) {
                this.buffer.append("Custom Endpoint (" + key + ")," + customEndpoints.get(key) + "\n");
            }
        }
        this.buffer.append("Sanity Checking Level," + bOps.getSanityCheckLevel() + "\n");
        if (bOps != null) {
            this.buffer.append("Warmups," + bOps.getWarmups() + "\n");
            this.buffer.append("Runs," + bOps.getRuns() + "\n");
        }
        this.buffer.append("Random Operation Order," + options.getRandomizeOrder() + "\n");
        this.buffer.append("Outliers," + bOps.getOutliers() + "\n");
        this.buffer.append("Timeout," + (options.getTimeout() > 0 ? Integer.toString(options.getTimeout()) : "disabled") + "s\n");
        this.buffer.append("Max Delay between Operations," + options.getMaxDelay() + "s\n");
        this.buffer.append("Result Limit," + (bOps.getLimit() <= 0 ? "Query Specified" : bOps.getLimit()) + "\n");
        this.buffer.append("ASK Results Format," + options.getResultsAskFormat() + "\n");
        this.buffer.append("Graph Results Format," + options.getResultsGraphFormat() + "\n");
        this.buffer.append("SELECT Results Format," + options.getResultsSelectFormat() + "\n");
        this.buffer.append("Parallel Threads," + options.getParallelThreads() + "\n");
        this.buffer.append("Result Counting," + bOps.getNoCount() + "\n");
        this.buffer.append(",\n");

        // Header for Run Summary
        this.buffer.append("Run Summary,\n");
        this.buffer.append("Run,Total Response Time,Total Runtime,Min Query Runtime,Max Query Runtime\n");

        // Actual run summaries are printed by handleProgess(QuerySetRun run)
        // during benchmarking
        this.ready = true;
    }

    /**
     * Handles the finished event by printing relevant statistics to the CSV
     * file
     * 
     * @param ok
     *            Whether benchmarking finished OK
     */
    @Override
    public <T extends Options> void finish(Runner<T> runner, T options, boolean ok) {
        if (!this.ready)
            throw new RuntimeException(
                    "handleFinished() was called on CsvProgressListener but it appears handleStarted() was not called or encountered an error, another listener may be the cause of this issue");

        if (!FileUtils.checkFile(this.f, this.allowOverwrite)) {
            throw new RuntimeException("CSV Output File is not a file, already exists or is not writable");
        }

        boolean wasMultithreaded = options.getParallelThreads() > 1;

        // Operation Summary Header
        this.buffer.append(",\nOperation Summary,\n");
        if (wasMultithreaded) {
            this.buffer
                    .append("Operation,Type,Total Response Time,Average Response Time (Arithmetic),Total Runtime,Actual Runtime,Average Runtime (Arithmetic),Actual Average Runtime (Arithmetic),Average Runtime (Geometric),Min Runtime,Max Runtime,Variance,Standard Deviation,Operations per Second,Actual Operations per Second,Operations per Hour,Actual Operations per Hour\n");
        } else {
            this.buffer
                    .append("Operation,Type,Total Response Time,Average Response Time (Arithmetic),Total Runtime,Average Runtime (Arithmetic),Average Runtime (Geometric),Min Runtime,Max Runtime,Variance,Standard Deviation,Queries per Second,Queries per Hour\n");
        }

        OperationMix operationMix = options.getOperationMix();
        Iterator<Operation> ops = operationMix.getOperations();
        while (ops.hasNext()) {
            Operation op = ops.next();
            // Operation Summary
            this.buffer.append(FormatUtils.toCsv(op.getName()) + ",");
            this.buffer.append(FormatUtils.toCsv(op.getType()) + ",");
            this.buffer.append(ConvertUtils.toSeconds(op.getStats().getTotalResponseTime()) + ",");
            this.buffer.append(ConvertUtils.toSeconds(op.getStats().getAverageResponseTime()) + ",");
            this.buffer.append(ConvertUtils.toSeconds(op.getStats().getTotalRuntime()) + ",");
            if (wasMultithreaded)
                this.buffer.append(ConvertUtils.toSeconds(op.getStats().getActualRuntime()) + ",");
            this.buffer.append(ConvertUtils.toSeconds(op.getStats().getAverageRuntime()) + ",");
            if (wasMultithreaded)
                this.buffer.append(ConvertUtils.toSeconds(op.getStats().getActualAverageRuntime()) + ",");
            this.buffer.append(op.getStats().getGeometricAverageRuntime() + ",");
            this.buffer.append(ConvertUtils.toSeconds(op.getStats().getMinimumRuntime()) + ",");
            this.buffer.append(ConvertUtils.toSeconds(op.getStats().getMaximumRuntime()) + ",");
            this.buffer.append(ConvertUtils.toSeconds(op.getStats().getVariance()) + ",");
            this.buffer.append(ConvertUtils.toSeconds(op.getStats().getStandardDeviation()) + ",");
            this.buffer.append(op.getStats().getOperationsPerSecond() + ",");
            if (wasMultithreaded)
                this.buffer.append(op.getStats().getActualOperationsPerSecond() + ",");
            this.buffer.append(op.getStats().getOperationsPerHour());
            if (wasMultithreaded)
                this.buffer.append("," + op.getStats().getActualOperationsPerHour());
            this.buffer.append("\n");
        }

        try {
            // Benchmark Summary
            FileWriter results = new FileWriter(this.f);

            if (wasMultithreaded) {
                results.append("Total Response Time,Average Response Time (Arithmetic),Total Runtime,Actual Runtime,Average Runtime (Arithmetic),Actual Average Runtime (Arithmetic),Average Runtime (Geometric),Minimum Mix Runtime,Maximum Mix Runtime,Variance,Standard Deviation,Operation Mixes per Hour,Actual Operation Mixes per Hour\n");
            } else {
                results.append("Total Response Time,Average Response Time (Arithmetic),Total Runtime,Average Runtime (Arithmetic),Average Runtime (Geometric),Minimum Mix Runtime,Maximum Mix Runtime,Variance,Standard Deviation,Operation Mixes per Hour\n");
            }
            results.append(ConvertUtils.toSeconds(operationMix.getStats().getTotalResponseTime()) + ",");
            results.append(ConvertUtils.toSeconds(operationMix.getStats().getAverageResponseTime()) + ",");
            results.append(ConvertUtils.toSeconds(operationMix.getStats().getTotalRuntime()) + ",");
            if (wasMultithreaded)
                results.append(ConvertUtils.toSeconds(operationMix.getStats().getActualRuntime()) + ",");
            results.append(ConvertUtils.toSeconds(operationMix.getStats().getAverageRuntime()) + ",");
            if (wasMultithreaded)
                results.append(ConvertUtils.toSeconds(operationMix.getStats().getActualAverageRuntime()) + ",");
            results.append(ConvertUtils.toSeconds(operationMix.getStats().getGeometricAverageRuntime()) + ",");
            results.append(ConvertUtils.toSeconds(operationMix.getStats().getMinimumRuntime()) + ",");
            results.append(ConvertUtils.toSeconds(operationMix.getStats().getMaximumRuntime()) + ",");
            results.append(ConvertUtils.toSeconds(operationMix.getStats().getVariance()) + ",");
            results.append(ConvertUtils.toSeconds(operationMix.getStats().getStandardDeviation()) + ",");
            results.append(Double.toString(operationMix.getStats().getOperationMixesPerHour()));
            if (wasMultithreaded)
                results.append("," + operationMix.getStats().getActualOperationMixesPerHour());
            results.append("\n");
            results.append(buffer.toString());
            results.close();
        } catch (IOException e) {
            System.err.println("Error created CSV results file " + this.f.getAbsolutePath());
            logger.error("Error creating CSV results file" + this.f.getAbsolutePath());
            if (options.getHaltOnError() || options.getHaltAny())
                runner.halt(options, e);
        }
    }

    /**
     * Does nothing as this listener discards informational messages
     * 
     * @param message
     *            Informational Message
     */
    @Override
    public <T extends Options> void progress(Runner<T> runner, T options, String message) {
        // We don't handle informational messages
    }
    
    /**
     * Does nothing as this listener discards individual operation run
     * statistics
     * 
     * @param operation
     *            Benchmark Operation
     */
    @Override
    public <T extends Options> void beforeOperation(Runner<T> runner, T options, Operation operation) {
        // We don't handle before operation events
    }

    /**
     * Does nothing as this listener discards individual operation run
     * statistics
     * 
     * @param operation
     *            Benchmark Operation
     * @param run
     *            Operation Run statistics
     */
    @Override
    public <T extends Options> void afterOperation(Runner<T> runner, T options, Operation operation, OperationRun run) {
        // We don't handle query run stats as they are produced, we're only
        // interested in aggregate stats at the end
    }
    
    @Override
    public <T extends Options> void beforeOperationMix(Runner<T> runner, T options, OperationMix mix) {
        // We don't handle before operation mix events
    }

    /**
     * Handles the Mix progress event by recording the run statistics for later
     * printing to the CSV file
     */
    @Override
    public synchronized <T extends Options> void afterOperationMix(Runner<T> runner, T options, OperationMix mix, OperationMixRun run) {
        this.buffer.append(this.run + ",");
        this.buffer.append(ConvertUtils.toSeconds(run.getTotalResponseTime()) + ",");
        this.buffer.append(ConvertUtils.toSeconds(run.getTotalRuntime()) + ",");
        this.buffer.append(ConvertUtils.toSeconds(run.getMinimumRuntime()) + ",");
        this.buffer.append(ConvertUtils.toSeconds(run.getMaximumRuntime()) + "\n");
        this.run++;
    }

}
