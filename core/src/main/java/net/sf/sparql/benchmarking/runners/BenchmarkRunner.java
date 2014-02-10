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
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.sparql.benchmarking.monitoring.ProgressListener;
import net.sf.sparql.benchmarking.operations.Operation;
import net.sf.sparql.benchmarking.operations.OperationMix;
import net.sf.sparql.benchmarking.options.BenchmarkOptions;
import net.sf.sparql.benchmarking.parallel.BenchmarkParallelClientManager;
import net.sf.sparql.benchmarking.parallel.ParallelClientManagerTask;
import net.sf.sparql.benchmarking.stats.OperationMixRun;
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
                l.handleStarted(this, options);
            } catch (Exception e) {
                System.err.println(l.getClass().getName() + " encountered an error during handleStarted() - " + e.getMessage());
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
        reportProgress(options, "Timeout = " + options.getTimeout() + " seconds");
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
                reportProgress(
                        options,
                        "Sanity Checks failed to meet required sanity level, please ensure that the endpoint specified is actually available and working.  If this is the case try setting -s 0 and retrying");
                System.exit(1);
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
        options.getOperationMix().clear();

        // Actual Runs
        reportProgress(options, "Running Benchmarks...");
        reportProgress(options);

        // Reset Order because warm up runs/prior runs may have altered this
        options.resetGlobalOrder();

        if (options.getParallelThreads() == 1) {
            // Single Threaded Benchmark
            for (i = 0; i < options.getRuns(); i++) {
                reportProgress(options, "Operation Mix Run " + (i + 1) + " of " + options.getRuns());
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
            // Multi Threaded Benchmark
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
        reportProgress(options, "Finished Benchmarking, calculating statistics...");
        reportProgress(options);

        // Operation Summary
        reportProgress(options, "Operation Summary");
        reportProgress(options, "-------------");
        reportProgress(options);
        ops = options.getOperationMix().getOperations();
        i = 0;
        while (ops.hasNext()) {
            Operation op = ops.next();
            // Trim outliers
            op.trim(options.getOutliers());

            // Print Summary
            reportProgress(options, "Operation ID " + i + " of type " + op.getType() + " (" + op.getName() + ")");
            reportProgress(options, "Total Errors: " + op.getTotalErrors());
            reportProgress(options, "Average Results: " + op.getAverageResults());
            reportProgress(options, "Total Response Time: " + FormatUtils.formatSeconds(op.getTotalResponseTime()));
            reportProgress(options,
                    "Average Response Time (Arithmetic): " + FormatUtils.formatSeconds(op.getAverageResponseTime()));
            reportProgress(options, "Total Runtime: " + FormatUtils.formatSeconds(op.getTotalRuntime()));
            if (options.getParallelThreads() > 1)
                reportProgress(options, "Actual Runtime: " + FormatUtils.formatSeconds(op.getActualRuntime()));
            reportProgress(options, "Average Runtime (Arithmetic): " + FormatUtils.formatSeconds(op.getAverageRuntime()));
            if (options.getParallelThreads() > 1)
                reportProgress(options,
                        "Actual Average Runtime (Arithmetic): " + FormatUtils.formatSeconds(op.getActualAverageRuntime()));
            reportProgress(options,
                    "Average Runtime (Geometric): " + FormatUtils.formatSeconds(op.getGeometricAverageRuntime()));
            reportProgress(options, "Minimum Runtime: " + FormatUtils.formatSeconds(op.getMinimumRuntime()));
            reportProgress(options, "Maximum Runtime: " + FormatUtils.formatSeconds(op.getMaximumRuntime()));
            reportProgress(options, "Runtime Variance: " + FormatUtils.formatSeconds(op.getVariance()));
            reportProgress(options, "Runtime Standard Deviation: " + FormatUtils.formatSeconds(op.getStandardDeviation()));
            reportProgress(options, "Operations per Second: " + op.getOperationsPerSecond());
            if (options.getParallelThreads() > 1)
                reportProgress(options, "Actual Operations per Second: " + op.getActualOperationsPerSecond());
            reportProgress(options, "Operations per Hour: " + op.getOperationsPerHour());
            if (options.getParallelThreads() > 1)
                reportProgress(options, "Actual Operations per Hour: " + op.getActualOperationsPerHour());
            reportProgress(options);
            i++;
        }

        // Benchmark Summary
        OperationMix operationMix = options.getOperationMix();
        operationMix.trim(options.getOutliers());
        reportProgress(options, "Operation Mix Summary");
        reportProgress(options, "-----------------");
        reportProgress(options);
        reportProgress(options,
                "Ran Operation Mix containing " + operationMix.size() + " operations a total of " + options.getRuns() + " times");
        reportProgress(options, "Total Response Time: " + FormatUtils.formatSeconds(operationMix.getTotalResponseTime()));
        reportProgress(options,
                "Average Response Time (Arithmetic): " + FormatUtils.formatSeconds(operationMix.getAverageResponseTime()));
        reportProgress(options, "Total Runtime: " + FormatUtils.formatSeconds(operationMix.getTotalRuntime()));
        if (options.getParallelThreads() > 1)
            reportProgress(options, "Actual Runtime: " + FormatUtils.formatSeconds(operationMix.getActualRuntime()));
        reportProgress(options,
                "Average Runtime (Arithmetic): " + FormatUtils.formatSeconds(operationMix.getAverageRuntime()));
        if (options.getParallelThreads() > 1)
            reportProgress(
                    options,
                    "Actual Average Runtime (Arithmetic): "
                            + FormatUtils.formatSeconds(operationMix.getActualAverageRuntime()));
        reportProgress(options, "Average Runtime (Geometric): " + operationMix.getGeometricAverageRuntime() + "s");
        reportProgress(options, "Minimum Mix Runtime: " + FormatUtils.formatSeconds(operationMix.getMinimumRuntime()));
        reportProgress(options, "Maximum Mix Runtime: " + FormatUtils.formatSeconds(operationMix.getMaximumRuntime()));
        reportProgress(options, "Mix Runtime Variance: " + FormatUtils.formatSeconds(operationMix.getVariance()));
        reportProgress(options,
                "Mix Runtime Standard Deviation: " + FormatUtils.formatSeconds(operationMix.getStandardDeviation()));
        reportProgress(options, "Operation Mixes per Hour: " + operationMix.getOperationMixesPerHour());
        if (options.getParallelThreads() > 1)
            reportProgress(options, "Actual Operation Mixes per Hour: " + operationMix.getActualOperationMixesPerHour());
        reportProgress(options);

        // Finally inform listeners that benchmarking finished OK
        for (ProgressListener l : options.getListeners()) {
            try {
                l.handleFinished(this, options, true);
            } catch (Exception e) {
                System.err.println(l.getClass().getName() + " encountered an error during handleFinish() - " + e.getMessage());
                if (options.getHaltOnError() || options.getHaltAny()) {
                    halt(options, l.getClass().getName() + " encountering an error during finish");
                }
            }
        }
    }
}
