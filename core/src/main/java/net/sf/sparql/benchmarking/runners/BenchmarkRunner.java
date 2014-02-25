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

package net.sf.sparql.benchmarking.runners;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.joda.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.sparql.benchmarking.monitoring.ProgressListener;
import net.sf.sparql.benchmarking.operations.Operation;
import net.sf.sparql.benchmarking.operations.OperationMix;
import net.sf.sparql.benchmarking.options.BenchmarkOptions;
import net.sf.sparql.benchmarking.parallel.ParallelClientManagerTask;
import net.sf.sparql.benchmarking.parallel.impl.BenchmarkParallelClientManager;
import net.sf.sparql.benchmarking.stats.OperationMixRun;
import net.sf.sparql.benchmarking.stats.OperationRun;
import net.sf.sparql.benchmarking.util.FormatUtils;

/**
 * A benchmark runner
 * 
 * @author rvesse
 * 
 */
public class BenchmarkRunner extends AbstractRunner<BenchmarkOptions> {

    private static final Logger logger = LoggerFactory.getLogger(BenchmarkRunner.class);

    @Override
    public void run(BenchmarkOptions options) {
        // Inform Listeners that we are starting benchmarking
        for (ProgressListener l : options.getListeners()) {
            try {
                l.start(this, options);
            } catch (Exception e) {
                System.err.println(l.getClass().getName() + " encountered an error during handleStarted() - " + e.getMessage());
                e.printStackTrace(System.err);
                // IMPORTANT - A startup error always halts benchmarking
                // regardless of halting options
                halt(options, l.getClass().getName() + " encountered an error in startup");
            }
        }

        // Validate Options
        if (options.getQueryEndpoint() == null && options.getUpdateEndpoint() == null && options.getGraphStoreEndpoint() == null
                && options.getCustomEndpoints().size() == 0) {
            System.err.println("At least one endpoint must be set");
            halt(options, "No endpoint was set");
        }
        if (options.getOperationMix() == null) {
            System.err.println("Operation Mix has not been set");
            halt(options, "No Operation Mix was set");
        }
        if (options.getOutliers() * 2 >= options.getRuns()) {
            System.err
                    .println("Specified number of outliers would mean all run results would be discarded, please specify a lower number of outliers");
            halt(options, "Number of Outliers too high");
        }
        if (options.getTimeout() <= 0) {
            System.err.println("Benchmarking requires that an operation timeout be set, cannot run with timeout disabled");
            halt(options, "No timeout was set");
        }

        Iterator<Operation> ops = options.getOperationMix().getOperations();
        checkOperations(options);

        // Print Options for User Reference
        reportProgress(options, "Benchmark Options");
        reportProgress(options, "-----------------");
        reportProgress(options);
        reportProgress(options,
                "Query Endpoint = " + (options.getQueryEndpoint() == null ? "not specified" : options.getQueryEndpoint()));
        reportProgress(options,
                "Update Endpoint = " + (options.getUpdateEndpoint() == null ? "not specified" : options.getUpdateEndpoint()));
        reportProgress(options, "Graph Store Protocol Endpoint = "
                + (options.getGraphStoreEndpoint() == null ? "not specified" : options.getGraphStoreEndpoint()));
        if (options.getCustomEndpoints().size() > 0) {
            for (String key : options.getCustomEndpoints().keySet()) {
                String value = options.getCustomEndpoint(key);
                reportProgress(options, "Custom Endpoint (" + key + ") = " + (value == null ? "not specified" : value));
            }
        }
        reportProgress(options, "Sanity Checking Level = " + options.getSanityCheckLevel());
        reportProgress(options, "Warmups = " + options.getWarmups());
        reportProgress(options, "Runs = " + options.getRuns());
        reportProgress(options, "Random Operation Order = " + (options.getRandomizeOrder() ? "On" : "Off"));
        reportProgress(options, "Outliers = " + options.getOutliers());
        reportProgress(options, "Timeout = " + (options.getTimeout() > 0 ? options.getTimeout() + " seconds" : "disabled"));
        reportProgress(options, "Max Delay between Operations = " + options.getMaxDelay() + " milliseconds");
        reportProgress(options, "Result Limit = " + (options.getLimit() <= 0 ? "Query Specified" : options.getLimit()));
        reportProgress(options,
                "CSV Results File = " + (options.getCsvResultsFile() == null ? "disabled" : options.getCsvResultsFile()));
        reportProgress(options,
                "XML Results File = " + (options.getXmlResultsFile() == null ? "disabled" : options.getXmlResultsFile()));
        reportProgress(options, "Halt on Timeout = " + options.getHaltOnTimeout());
        reportProgress(options, "Halt on Error = " + options.getHaltOnError());
        reportProgress(options, "Halt Any = " + options.getHaltAny());
        reportProgress(options, "ASK Results Format = " + options.getResultsAskFormat());
        reportProgress(options, "Graph Results Format = " + options.getResultsGraphFormat());
        reportProgress(options, "SELECT Results Format = " + options.getResultsSelectFormat());
        reportProgress(options, "Compression = " + (options.getAllowCompression() ? "enabled" : "disabled"));
        reportProgress(options, "Parallel Threads = " + options.getParallelThreads());
        reportProgress(options, "Result Counting = " + (options.getNoCount() ? "disabled" : "enabled"));
        reportProgress(options, "Authentication = " + (options.getAuthenticator() != null ? "enabled" : "disabled"));
        reportProgress(options);

        // Sanity Checking
        if (options.getSanityCheckLevel() > 0) {
            if (checkSanity(options)) {
                reportProgress(options, "Sanity Checks passed required sanity level...");
                reportProgress(options);
            } else {
                halt(options,
                        "Sanity Checks failed to meet required sanity level, please ensure that the endpoint specified is actually available and working.  If this is the case try calling setSanityCheckLevel(0) and retrying");
            }
        } else {
            reportProgress(options, "Sanity Check skipped by user...");
        }

        // Summarise Operations to be used
        reportProgress(options, "Starting Benchmarking...");
        reportProgress(options, options.getOperationMix().size() + " operations were loaded:");

        int i = 0;
        ops = options.getOperationMix().getOperations();
        while (ops.hasNext()) {
            Operation op = ops.next();
            reportProgress(options, "Operation ID " + i + " of type " + op.getType() + " (" + op.getName() + ")");
            reportProgress(options, op.getContentString());
            reportProgress(options);
            i++;
        }

        // Setup
        runSetup(options);

        // Warmups
        reportProgress(options, "Running Warmups...");
        reportProgress(options);
        for (i = 0; i < options.getWarmups(); i++) {
            reportProgress(options, "Warmup Run " + (i + 1) + " of " + options.getWarmups());
            OperationMixRun r = options.getOperationMix().run(this, options);
            reportProgress(options);
            reportProgress(options, "Total Response Time: " + FormatUtils.formatSeconds(r.getTotalResponseTime()));
            reportProgress(options, "Total Runtime: " + FormatUtils.formatSeconds(r.getTotalRuntime()));
            int minOperationId = r.getMinimumRuntimeOperationID();
            int maxOperationId = r.getMaximumRuntimeOperationID();
            reportProgress(options, "Minimum Operation Runtime: " + FormatUtils.formatSeconds(r.getMinimumRuntime())
                    + " (Operation " + options.getOperationMix().getOperation(minOperationId).getName() + ")");
            reportProgress(options, "Maximum Operation Runtime: " + FormatUtils.formatSeconds(r.getMaximumRuntime())
                    + " (Operation " + options.getOperationMix().getOperation(maxOperationId).getName() + ")");
            reportProgress(options);
        }
        options.getOperationMix().getStats().clear();

        // Actual Runs
        reportProgress(options, "Running Benchmarks...");
        reportProgress(options);

        // Reset Order because warm up runs/prior runs may have altered this
        options.resetGlobalOrder();

        // Record start time
        Instant startInstant = Instant.now();
        reportProgress(options, "Start Time: " + FormatUtils.formatInstant(startInstant));

        if (options.getParallelThreads() == 1) {
            // Single Threaded Benchmark
            for (i = 0; i < options.getRuns(); i++) {
                reportProgress(options, "Operation Mix Run " + (i + 1) + " of " + options.getRuns());
                reportProgress(options, "Current Time: " + FormatUtils.formatInstant(Instant.now()));
                OperationMixRun r = options.getOperationMix().run(this, options);
                reportProgress(options, r);
                reportProgress(options);
                reportProgress(options, "Total Response Time: " + FormatUtils.formatSeconds(r.getTotalResponseTime()));
                reportProgress(options, "Total Runtime: " + FormatUtils.formatSeconds(r.getTotalRuntime()));
                int minOperationId = r.getMinimumRuntimeOperationID();
                int maxOperationId = r.getMaximumRuntimeOperationID();
                reportProgress(options, "Minimum Operation Runtime: " + FormatUtils.formatSeconds(r.getMinimumRuntime())
                        + " (Query " + options.getOperationMix().getOperation(minOperationId).getName() + ")");
                reportProgress(options, "Maximum Operation Runtime: " + FormatUtils.formatSeconds(r.getMaximumRuntime())
                        + " (Query " + options.getOperationMix().getOperation(maxOperationId).getName() + ")");
                reportProgress(options);
            }
        } else {
            // Multi-Threaded Benchmark
            options.getOperationMix().setRunAsThread(true);
            ParallelClientManagerTask<BenchmarkOptions> task = new ParallelClientManagerTask<BenchmarkOptions>(
                    new BenchmarkParallelClientManager<BenchmarkOptions>(this, options));
            options.getExecutor().submit(task);
            try {
                task.get();
            } catch (InterruptedException e) {
                logger.error("Multi Threaded Benchmarking was interrupted - " + e.getMessage());
                if (options.getHaltAny())
                    halt(options, e);
            } catch (ExecutionException e) {
                logger.error("Multi Threaded Benchmarking encountered an error - " + e.getMessage());

                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                logger.error(sw.toString());

                if (options.getHaltOnError() || options.getHaltAny())
                    halt(options, e);
            }
        }
        Instant endInstant = Instant.now();

        // Teardown
        runTeardown(options);

        reportProgress(options, "Finished Benchmarking...");
        reportProgress(options);

        // Operation Summary
        reportProgress(options, "Operation Summary");
        reportProgress(options, "-----------------");
        reportProgress(options);
        ops = options.getOperationMix().getOperations();
        i = 0;
        while (ops.hasNext()) {
            Operation op = ops.next();
            // Trim outliers
            op.getStats().trim(options.getOutliers());

            // Print Summary
            reportProgress(options, "Operation ID " + i + " of type " + op.getType() + " (" + op.getName() + ")");
            reportProgress(options, "Total Errors: " + op.getStats().getTotalErrors());
            if (op.getStats().getTotalErrors() > 0) {
                // Show errors by category
                Map<Integer, List<OperationRun>> categorizedErrors = op.getStats().getCategorizedErrors();
                this.reportCategorizedErrors(options, categorizedErrors);
            }
            reportProgress(options, "Average Results: " + op.getStats().getAverageResults());
            reportProgress(options, "Total Response Time: " + FormatUtils.formatSeconds(op.getStats().getTotalResponseTime()));
            reportProgress(options,
                    "Average Response Time (Arithmetic): " + FormatUtils.formatSeconds(op.getStats().getAverageResponseTime()));
            reportProgress(options, "Total Runtime: " + FormatUtils.formatSeconds(op.getStats().getTotalRuntime()));
            if (options.getParallelThreads() > 1)
                reportProgress(options, "Actual Runtime: " + FormatUtils.formatSeconds(op.getStats().getActualRuntime()));
            reportProgress(options, "Average Runtime (Arithmetic): " + FormatUtils.formatSeconds(op.getStats().getAverageRuntime()));
            if (options.getParallelThreads() > 1)
                reportProgress(options,
                        "Actual Average Runtime (Arithmetic): " + FormatUtils.formatSeconds(op.getStats().getActualAverageRuntime()));
            reportProgress(options, "Average Runtime (Geometric): " + FormatUtils.formatSeconds(op.getStats().getGeometricAverageRuntime()));
            reportProgress(options, "Minimum Runtime: " + FormatUtils.formatSeconds(op.getStats().getMinimumRuntime()));
            reportProgress(options, "Maximum Runtime: " + FormatUtils.formatSeconds(op.getStats().getMaximumRuntime()));
            reportProgress(options, "Runtime Variance: " + FormatUtils.formatSeconds(op.getStats().getVariance()));
            reportProgress(options, "Runtime Standard Deviation: " + FormatUtils.formatSeconds(op.getStats().getStandardDeviation()));
            reportProgress(options, "Operations per Second: " + op.getStats().getOperationsPerSecond());
            if (options.getParallelThreads() > 1)
                reportProgress(options, "Actual Operations per Second: " + op.getStats().getActualOperationsPerSecond());
            reportProgress(options, "Operations per Hour: " + op.getStats().getOperationsPerHour());
            if (options.getParallelThreads() > 1)
                reportProgress(options, "Actual Operations per Hour: " + op.getStats().getActualOperationsPerHour());
            reportProgress(options);
            i++;
        }

        // Benchmark Summary
        OperationMix operationMix = options.getOperationMix();
        operationMix.getStats().trim(options.getOutliers());
        reportProgress(options, "Operation Mix Summary");
        reportProgress(options, "---------------------");
        reportProgress(options);
        reportProgress(options,
                "Ran Operation Mix containing " + operationMix.size() + " operations a total of " + options.getRuns() + " times");
        reportProgress(options, "Start Time: " + FormatUtils.formatInstant(startInstant));
        reportProgress(options, "End Time: " + FormatUtils.formatInstant(endInstant));
        reportProgress(options);
        reportProgress(options, "Total Errors: " + options.getOperationMix().getStats().getTotalErrors());
        if (options.getOperationMix().getStats().getTotalErrors() > 0) {
            // Show errors by category
            Map<Integer, List<OperationRun>> categorizedErrors = options.getOperationMix().getStats().getCategorizedErrors();
            reportCategorizedErrors(options, categorizedErrors);
        }
        reportProgress(options);
        reportProgress(options, "Total Response Time: " + FormatUtils.formatSeconds(operationMix.getStats().getTotalResponseTime()));
        reportProgress(options,
                "Average Response Time (Arithmetic): " + FormatUtils.formatSeconds(operationMix.getStats().getAverageResponseTime()));
        reportProgress(options, "Total Runtime: " + FormatUtils.formatSeconds(operationMix.getStats().getTotalRuntime()));
        if (options.getParallelThreads() > 1)
            reportProgress(options, "Actual Runtime: " + FormatUtils.formatSeconds(operationMix.getStats().getActualRuntime()));
        reportProgress(options, "Average Runtime (Arithmetic): " + FormatUtils.formatSeconds(operationMix.getStats().getAverageRuntime()));
        if (options.getParallelThreads() > 1)
            reportProgress(options,
                    "Actual Average Runtime (Arithmetic): " + FormatUtils.formatSeconds(operationMix.getStats().getActualAverageRuntime()));
        reportProgress(options, "Average Runtime (Geometric): " + operationMix.getStats().getGeometricAverageRuntime() + "s");
        reportProgress(options, "Minimum Mix Runtime: " + FormatUtils.formatSeconds(operationMix.getStats().getMinimumRuntime()));
        reportProgress(options, "Maximum Mix Runtime: " + FormatUtils.formatSeconds(operationMix.getStats().getMaximumRuntime()));
        reportProgress(options, "Mix Runtime Variance: " + FormatUtils.formatSeconds(operationMix.getStats().getVariance()));
        reportProgress(options,
                "Mix Runtime Standard Deviation: " + FormatUtils.formatSeconds(operationMix.getStats().getStandardDeviation()));
        reportProgress(options, "Operation Mixes per Hour: " + operationMix.getStats().getOperationMixesPerHour());
        if (options.getParallelThreads() > 1)
            reportProgress(options, "Actual Operation Mixes per Hour: " + operationMix.getStats().getActualOperationMixesPerHour());
        reportProgress(options);

        // Finally inform listeners that benchmarking finished OK
        for (ProgressListener l : options.getListeners()) {
            try {
                l.finish(this, options, true);
            } catch (Exception e) {
                System.err.println(l.getClass().getName() + " encountered an error during handleFinish() - " + e.getMessage());
                e.printStackTrace(System.err);
                if (options.getHaltOnError() || options.getHaltAny()) {
                    halt(options, l.getClass().getName() + " encountering an error during finish");
                }
            }
        }
    }
}
