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
import java.util.concurrent.TimeUnit;

import org.joda.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.sparql.benchmarking.monitoring.ProgressListener;
import net.sf.sparql.benchmarking.operations.Operation;
import net.sf.sparql.benchmarking.options.SoakOptions;
import net.sf.sparql.benchmarking.parallel.ParallelClientManagerTask;
import net.sf.sparql.benchmarking.parallel.SoakTestParallelClientManager;
import net.sf.sparql.benchmarking.stats.OperationMixRun;
import net.sf.sparql.benchmarking.stats.OperationRun;
import net.sf.sparql.benchmarking.util.ConvertUtils;
import net.sf.sparql.benchmarking.util.ErrorCategories;
import net.sf.sparql.benchmarking.util.FormatUtils;

/**
 * A soak test runner
 * 
 * @author rvesse
 * 
 */
public class SoakRunner extends AbstractRunner<SoakOptions> {

    static final Logger logger = LoggerFactory.getLogger(SoakRunner.class);

    @Override
    public void run(SoakOptions options) {
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
        if (options.getMaxRuns() <= 0 && options.getMaxRuntime() <= 0) {
            System.err
                    .println("One/both of the maximum runs (use setRuns() method) or the maximum runtime (use setSoakRuntime() method) must be set");
            halt(options, "No maximum runs/runtime set");
        }
        if (options.getOperationMix() == null) {
            System.err.println("Operation Mix has not been set");
            halt(options, "No Operation Mix was set");
        }

        Iterator<Operation> ops;
        checkOperations(options);

        // Print Options for User Reference
        reportProgress(options, "Soak Options");
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
        if (options.getMaxRuns() > 0)
            reportProgress(options, "Maximum Runs = " + options.getMaxRuns());
        if (options.getMaxRuntime() > 0)
            reportProgress(options, "Maximum Runtime = " + options.getMaxRuntime() + " minutes");
        reportProgress(options, "Random Operation Order = " + (options.getRandomizeOrder() ? "On" : "Off"));
        reportProgress(options, "Timeout = " + (options.getTimeout() > 0 ? options.getTimeout() + " seconds" : "disabled"));
        reportProgress(options, "Max Delay between Operations = " + options.getMaxDelay() + " milliseconds");
        // reportProgress(options, "Result Limit = " + (options.getLimit() <= 0
        // ? "Query Specified" : options.getLimit()));
        reportProgress(options, "Halt on Timeout = " + options.getHaltOnTimeout());
        reportProgress(options, "Halt on Error = " + options.getHaltOnError());
        reportProgress(options, "Halt Any = " + options.getHaltAny());
        reportProgress(options, "ASK Results Format = " + options.getResultsAskFormat());
        reportProgress(options, "Graph Results Format = " + options.getResultsGraphFormat());
        reportProgress(options, "SELECT Results Format = " + options.getResultsSelectFormat());
        reportProgress(options, "Compression = " + (options.getAllowCompression() ? "enabled" : "disabled"));
        reportProgress(options, "Parallel Threads = " + options.getParallelThreads());
        // reportProgress(options, "Result Counting = " + (options.getNoCount()
        // ? "disabled" : "enabled"));
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

        // Summarize operations to be used
        reportProgress(options, "Starting soak testing...");
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

        // Actual Runs
        reportProgress(options, "Running soak tests...");
        Instant startInstant = Instant.now();
        Instant endInstant = startInstant;
        reportProgress(options, "Start Time: " + FormatUtils.formatInstant(startInstant));
        reportProgress(options);

        long startTime = System.nanoTime();
        long endTime = startTime;
        if (options.getParallelThreads() == 1) {
            // Single Threaded Benchmark
            i = 0;
            while (true) {
                if (options.getMaxRuns() > 0) {
                    reportProgress(options, "Operation Mix Run " + (i + 1) + " of " + options.getMaxRuns());
                } else {
                    reportProgress(options, "Operation Mix Run " + (i + 1));
                }
                reportProgress(options, "Current Time: " + FormatUtils.formatInstant(Instant.now()));
                if (options.getMaxRuntime() > 0) {
                    reportProgress(
                            options,
                            "Running for "
                                    + String.format("%,.3f minutes", ConvertUtils.toMinutes(System.nanoTime() - startTime))
                                    + " minutes of " + options.getMaxRuntime() + " minutes");
                }
                i++;
                OperationMixRun r = options.getOperationMix().run(this, options);
                reportProgress(options, r);
                reportProgress(options);

                if (options.getMaxRuns() > 0 && i >= options.getMaxRuns()) {
                    // Reached max runs
                    reportProgress(options, "Reached the maximum number of runs");
                    break;
                }
                if (options.getMaxRuntime() > 0) {
                    endTime = System.nanoTime();
                    if (TimeUnit.NANOSECONDS.toMinutes(endTime - startTime) >= options.getMaxRuntime()) {
                        // Reached maximum runtime
                        reportProgress(options, "Reached the maximum runtime");
                        break;
                    }
                }
            }
        } else {
            // Multi Threaded Benchmark
            options.getOperationMix().setRunAsThread(true);
            ParallelClientManagerTask<SoakOptions> task = new ParallelClientManagerTask<SoakOptions>(
                    new SoakTestParallelClientManager(this, options));
            options.getExecutor().submit(task);
            try {
                task.get();
            } catch (InterruptedException e) {
                logger.error("Multi Threaded soak testing was interrupted - " + e.getMessage());
                if (options.getHaltAny())
                    halt(options, e);
            } catch (ExecutionException e) {
                logger.error("Multi Threaded soak testing encountered an error - " + e.getMessage());

                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                logger.error(sw.toString());

                if (options.getHaltOnError() || options.getHaltAny())
                    halt(options, e);
            }
        }

        // Get end time
        endTime = System.nanoTime();
        endInstant = Instant.now();

        reportProgress(options, "Finished soak testing");
        reportProgress(options);

        // Summarize Operations
        reportProgress(options, "Operation Summary");
        reportProgress(options, "-----------------");
        reportProgress(options);

        ops = options.getOperationMix().getOperations();
        i = 0;
        while (ops.hasNext()) {
            Operation op = ops.next();

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
            reportProgress(options, "Average Runtime (Geometric): " + FormatUtils.formatSeconds(op.getGeometricAverageRuntime()));
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

        reportProgress(options, "Soak Test Summary");
        reportProgress(options, "-----------------");
        reportProgress(options);
        reportProgress(options, "Number of Runs: " + i);
        reportProgress(options, "Total Operations Run: " + (i * options.getOperationMix().size()));
        reportProgress(options);
        reportProgress(options, "Total Errors: " + options.getOperationMix().getTotalErrors());
        if (options.getOperationMix().getTotalErrors() > 0) {
            // Show errors by category
            reportProgress(options, "Errors by Category: ");
            Map<Integer, List<OperationRun>> categorizedErrors = options.getOperationMix().getCategorizedErrors();
            for (Integer category : categorizedErrors.keySet()) {
                String description = ErrorCategories.getDescription(category);
                if (description == null)
                    description = String.format("Unknown Category %d", category);
                reportProgress(options, description + ": " + categorizedErrors.get(category).size() + " error(s)");
            }
        }
        reportProgress(options);
        reportProgress(options, "Start Time: " + FormatUtils.formatInstant(startInstant));
        reportProgress(options, "End Time: " + FormatUtils.formatInstant(endInstant));
        reportProgress(options, "Total Runtime: " + ConvertUtils.toMinutes(endTime - startTime) + " minutes");

        // Finally inform listeners that running finished OK
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
