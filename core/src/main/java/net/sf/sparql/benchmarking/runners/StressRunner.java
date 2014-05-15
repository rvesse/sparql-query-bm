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
import net.sf.sparql.benchmarking.options.Options;
import net.sf.sparql.benchmarking.options.StressOptions;
import net.sf.sparql.benchmarking.parallel.ParallelClientManagerTask;
import net.sf.sparql.benchmarking.parallel.impl.StressTestParallelClientManager;
import net.sf.sparql.benchmarking.stats.OperationRun;
import net.sf.sparql.benchmarking.util.ConvertUtils;
import net.sf.sparql.benchmarking.util.FormatUtils;

/**
 * A stress test runner
 * <p>
 * Stress tests are continuous tests that run for some period of time that
 * gradually increase the load upon the system. This differs from the
 * {@link SoakRunner} which provides continuous tests that put a stable load on
 * the system.
 * </p>
 * <p>
 * The testing methodology is to start with only the number of user defined
 * parallel threads ( {@link Options#getParallelThreads()} and have each thread
 * complete a single run. The number of parallel threads are then increased by
 * the configured ramp up factor ({@link StressOptions#getRampUpFactor()}) and
 * the runs repeated. This process continues until such time as the configured
 * maximum runtime or maximum number of threads is reached.
 * </p>
 * 
 * @author rvesse
 * 
 */
public class StressRunner extends AbstractRunner<StressOptions> {

	static final Logger logger = LoggerFactory.getLogger(StressRunner.class);

	@Override
	public void run(StressOptions options) {
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

		Iterator<Operation> ops;
		checkOperations(options);

		// Print Options for User Reference
		reportGeneralOptions(options);
		reportStressOptions(options);

		// Sanity Checking
		if (options.getSanityCheckLevel() > 0) {
			if (checkSanity(options)) {
				reportProgress(options,
						"Sanity Checks passed required sanity level...");
				reportProgress(options);
			} else {
				reportProgress(
						options,
						"Sanity Checks failed to meet required sanity level, please ensure that the endpoint specified is actually available and working.  If this is the case try setting the sanity checking level to zero and retrying");
				System.exit(1);
			}
		} else {
			reportProgress(options, "Sanity Check skipped by user...");
		}

		// Summarize operations to be used
		reportProgress(options, "Starting soak testing...");
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

		// Actual Runs
		reportProgress(options, "Running stress tests...");
		Instant startInstant = Instant.now();
		Instant endInstant = startInstant;
		reportProgress(options,
				"Start Time: " + FormatUtils.formatInstant(startInstant));
		reportProgress(options);

		long startTime = System.nanoTime();
		long endTime = startTime;
		// Stress tests are always multi-threaded
		StressTestParallelClientManager stressClientManager = new StressTestParallelClientManager(
				this, options);
		ParallelClientManagerTask<StressOptions> task = new ParallelClientManagerTask<StressOptions>(
				stressClientManager);
		options.getExecutor().submit(task);
		try {
			task.get();
		} catch (InterruptedException e) {
			logger.error("Stress testing was interrupted - " + e.getMessage());
			if (options.getHaltAny())
				halt(options, e);
		} catch (ExecutionException e) {
			logger.error("Stress testing encountered an error - "
					+ e.getMessage());

			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			logger.error(sw.toString());

			if (options.getHaltOnError() || options.getHaltAny())
				halt(options, e);
		}

		// Get end time
		endTime = System.nanoTime();
		endInstant = Instant.now();

		// Teardown
		runTeardown(options);

		reportProgress(options, "Finished stress testing");
		reportProgress(options);

		// Summarize Operations
		reportProgress(options, "Operation Summary");
		reportProgress(options, "-----------------");
		reportProgress(options);

		ops = options.getOperationMix().getOperations();
		while (ops.hasNext()) {
			Operation op = ops.next();

			// Print Summary
			reportOperationSummary(options, op);
		}

		reportProgress(options, "Stress Test Summary");
		reportProgress(options, "-----------------");
		reportProgress(options);
		reportProgress(options, "Total Mix Runs: "
				+ options.getOperationMix().getStats().getRunCount());
		reportProgress(options, "Total Operations Run: "
				+ options.getOperationMix().getStats().getTotalOperations());
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
		reportProgress(options,
				"Start Time: " + FormatUtils.formatInstant(startInstant));
		reportProgress(options,
				"End Time: " + FormatUtils.formatInstant(endInstant));
		reportProgress(options,
				"Total Runtime: " + ConvertUtils.toMinutes(endTime - startTime)
						+ " minutes");
		reportProgress(options, "Maximum Parallel Threads: "
				+ stressClientManager.getCurrentClientCount());
		reportProgress(options);

		// Finally inform listeners that running finished OK
		finished(options);
	}

	private void reportStressOptions(StressOptions options) {
		reportProgress(options, "Stress Options");
		reportProgress(options, "------------");
		reportProgress(options);
		reportProgress(options, "Maximum Threads = "
				+ (options.getMaxThreads() > 0 ? options.getMaxThreads()
						: "Unlimited"));
		reportProgress(options, "Maximum Runtime = "
				+ (options.getMaxRuntime() > 0 ? options.getMaxRuntime()
						+ " minutes" : "Unlimited"));
		reportProgress(options, "Ramp Up Factor = " + options.getRampUpFactor());
		reportProgress(options);
	}
}
