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

	private static final Logger logger = LoggerFactory
			.getLogger(BenchmarkRunner.class);

	@Override
	public void run(BenchmarkOptions options) {
		// Inform Listeners that we are starting benchmarking
		started(options);

		// Validate Options
		if (options.getQueryEndpoint() == null
				&& options.getUpdateEndpoint() == null
				&& options.getGraphStoreEndpoint() == null
				&& options.getCustomEndpoints().size() == 0
				&& options.getDataset() == null) {
			System.err
					.println("At least one remote endpoint or an in-memory dataset must be set");
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
			System.err
					.println("Benchmarking requires that an operation timeout be set, cannot run with timeout disabled");
			halt(options, "No timeout was set");
		}

		Iterator<Operation> ops = options.getOperationMix().getOperations();
		checkOperations(options);

		// Print Options for User Reference
		reportGeneralOptions(options);
		reportBenchmarkOptions(options);
		reportProgress(options);

		// Sanity Checking
		runSanityChecks(options);

		// Summarise Operations to be used
		reportProgress(options, "Starting Benchmarking...");
		reportProgress(options, options.getOperationMix().size()
				+ " operations were loaded:");

		int i = 0;
		ops = options.getOperationMix().getOperations();
		while (ops.hasNext()) {
			Operation op = ops.next();
			reportProgress(options,
					"Operation ID " + i + " of type " + op.getType() + " ("
							+ op.getName() + ")");
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
			reportProgress(options,
					"Warmup Run " + (i + 1) + " of " + options.getWarmups());
			OperationMixRun r = this.runMix(options);
			reportProgress(options);
			reportProgress(
					options,
					"Total Response Time: "
							+ FormatUtils.formatSeconds(r
									.getTotalResponseTime()));
			reportProgress(
					options,
					"Total Runtime: "
							+ FormatUtils.formatSeconds(r.getTotalRuntime()));
			int minOperationId = r.getMinimumRuntimeOperationID();
			int maxOperationId = r.getMaximumRuntimeOperationID();
			reportProgress(
					options,
					"Minimum Operation Runtime: "
							+ FormatUtils.formatSeconds(r.getMinimumRuntime())
							+ " (Operation "
							+ options.getOperationMix()
									.getOperation(minOperationId).getName()
							+ ")");
			reportProgress(
					options,
					"Maximum Operation Runtime: "
							+ FormatUtils.formatSeconds(r.getMaximumRuntime())
							+ " (Operation "
							+ options.getOperationMix()
									.getOperation(maxOperationId).getName()
							+ ")");
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
		reportProgress(options,
				"Start Time: " + FormatUtils.formatInstant(startInstant));

		if (options.getParallelThreads() == 1) {
			// Single Threaded Benchmark
			for (i = 0; i < options.getRuns(); i++) {
				reportProgress(options, "Operation Mix Run " + (i + 1) + " of "
						+ options.getRuns());
				reportProgress(
						options,
						"Current Time: "
								+ FormatUtils.formatInstant(Instant.now()));
				reportBeforeOperationMix(options, options.getOperationMix());
				OperationMixRun r = this.runMix(options);
				reportAfterOperationMix(options, options.getOperationMix(), r);
				reportProgress(options);
				reportProgress(
						options,
						"Total Response Time: "
								+ FormatUtils.formatSeconds(r
										.getTotalResponseTime()));
				reportProgress(
						options,
						"Total Runtime: "
								+ FormatUtils.formatSeconds(r.getTotalRuntime()));
				int minOperationId = r.getMinimumRuntimeOperationID();
				int maxOperationId = r.getMaximumRuntimeOperationID();
				reportProgress(
						options,
						"Minimum Operation Runtime: "
								+ FormatUtils.formatSeconds(r
										.getMinimumRuntime())
								+ " (Query "
								+ options.getOperationMix()
										.getOperation(minOperationId).getName()
								+ ")");
				reportProgress(
						options,
						"Maximum Operation Runtime: "
								+ FormatUtils.formatSeconds(r
										.getMaximumRuntime())
								+ " (Query "
								+ options.getOperationMix()
										.getOperation(maxOperationId).getName()
								+ ")");
				reportProgress(options);
			}
		} else {
			// Multi-Threaded Benchmark
			ParallelClientManagerTask<BenchmarkOptions> task = new ParallelClientManagerTask<BenchmarkOptions>(
					new BenchmarkParallelClientManager<BenchmarkOptions>(this,
							options));
			options.getExecutor().submit(task);
			try {
				task.get();
			} catch (InterruptedException e) {
				logger.error("Multi Threaded Benchmarking was interrupted - "
						+ e.getMessage());
				if (options.getHaltAny())
					halt(options, e);
			} catch (ExecutionException e) {
				logger.error("Multi Threaded Benchmarking encountered an error - "
						+ e.getMessage());

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
		while (ops.hasNext()) {
			Operation op = ops.next();
			// Trim outliers
			op.getStats().trim(options.getOutliers());

			// Print Summary
			reportOperationSummary(options, op);
		}

		// Benchmark Summary
		OperationMix operationMix = options.getOperationMix();
		operationMix.getStats().trim(options.getOutliers());
		reportProgress(options, "Operation Mix Summary");
		reportProgress(options, "---------------------");
		reportProgress(options);
		reportProgress(options, "Total Mix Runs: " + options.getRuns());
		reportProgress(options, "Total Operation Runs: "
				+ options.getOperationMix().getStats().getTotalOperations());
		reportProgress(options,
				"Start Time: " + FormatUtils.formatInstant(startInstant));
		reportProgress(options,
				"End Time: " + FormatUtils.formatInstant(endInstant));
		reportProgress(options);
		reportProgress(options, "Total Errors: "
				+ options.getOperationMix().getStats().getTotalErrors());
		if (options.getOperationMix().getStats().getTotalErrors() > 0) {
			// Show errors by category
			Map<Integer, List<OperationRun>> categorizedErrors = options
					.getOperationMix().getStats().getCategorizedErrors();
			reportCategorizedErrors(options, categorizedErrors);
		}
		reportProgress(options);
		reportProgress(
				options,
				"Total Response Time: "
						+ FormatUtils.formatSeconds(operationMix.getStats()
								.getTotalResponseTime()));
		reportProgress(
				options,
				"Average Response Time (Arithmetic): "
						+ FormatUtils.formatSeconds(operationMix.getStats()
								.getAverageResponseTime()));
		reportProgress(
				options,
				"Total Runtime: "
						+ FormatUtils.formatSeconds(operationMix.getStats()
								.getTotalRuntime()));
		if (options.getParallelThreads() > 1)
			reportProgress(
					options,
					"Actual Runtime: "
							+ FormatUtils.formatSeconds(operationMix.getStats()
									.getActualRuntime()));
		reportProgress(
				options,
				"Average Runtime (Arithmetic): "
						+ FormatUtils.formatSeconds(operationMix.getStats()
								.getAverageRuntime()));
		if (options.getParallelThreads() > 1)
			reportProgress(
					options,
					"Actual Average Runtime (Arithmetic): "
							+ FormatUtils.formatSeconds(operationMix.getStats()
									.getActualAverageRuntime()));
		reportProgress(options, "Average Runtime (Geometric): "
				+ operationMix.getStats().getGeometricAverageRuntime() + "s");
		reportProgress(
				options,
				"Minimum Mix Runtime: "
						+ FormatUtils.formatSeconds(operationMix.getStats()
								.getMinimumRuntime()));
		reportProgress(
				options,
				"Maximum Mix Runtime: "
						+ FormatUtils.formatSeconds(operationMix.getStats()
								.getMaximumRuntime()));
		reportProgress(
				options,
				"Mix Runtime Variance: "
						+ FormatUtils.formatSeconds(operationMix.getStats()
								.getVariance()));
		reportProgress(
				options,
				"Mix Runtime Standard Deviation: "
						+ FormatUtils.formatSeconds(operationMix.getStats()
								.getStandardDeviation()));
		reportProgress(options, "Operation Mixes per Hour: "
				+ operationMix.getStats().getOperationMixesPerHour());
		if (options.getParallelThreads() > 1)
			reportProgress(options, "Actual Operation Mixes per Hour: "
					+ operationMix.getStats().getActualOperationMixesPerHour());
		reportProgress(options);

		// Finally inform listeners that benchmarking finished OK
		finished(options);
	}

	private void reportBenchmarkOptions(BenchmarkOptions options) {
		reportProgress(options, "Benchmark Options");
		reportProgress(options, "---------------");
		reportProgress(options);
		reportProgress(options, "Warmups = " + options.getWarmups());
		reportProgress(options, "Runs = " + options.getRuns());
		reportProgress(options, "Outliers = " + options.getOutliers());
		reportProgress(options,
				"CSV Results File = "
						+ (options.getCsvResultsFile() == null ? "disabled"
								: options.getCsvResultsFile()));
		reportProgress(options,
				"XML Results File = "
						+ (options.getXmlResultsFile() == null ? "disabled"
								: options.getXmlResultsFile()));
		reportProgress(options);
	}
}
