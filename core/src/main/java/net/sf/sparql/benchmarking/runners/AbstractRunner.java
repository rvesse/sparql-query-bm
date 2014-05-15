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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;

import net.sf.sparql.benchmarking.monitoring.ProgressListener;
import net.sf.sparql.benchmarking.operations.Operation;
import net.sf.sparql.benchmarking.operations.OperationMix;
import net.sf.sparql.benchmarking.operations.query.callables.InMemoryQueryCallable;
import net.sf.sparql.benchmarking.operations.query.callables.RemoteQueryCallable;
import net.sf.sparql.benchmarking.options.Options;
import net.sf.sparql.benchmarking.runners.mix.DefaultOperationMixRunner;
import net.sf.sparql.benchmarking.runners.mix.InOrderOperationMixRunner;
import net.sf.sparql.benchmarking.runners.mix.OperationMixRunner;
import net.sf.sparql.benchmarking.runners.operations.DefaultOperationRunner;
import net.sf.sparql.benchmarking.runners.operations.OperationRunner;
import net.sf.sparql.benchmarking.stats.OperationMixRun;
import net.sf.sparql.benchmarking.stats.OperationRun;
import net.sf.sparql.benchmarking.util.ErrorCategories;
import net.sf.sparql.benchmarking.util.FormatUtils;

/**
 * Abstract implementation of a runner providing common halting and progress
 * reporting functionality
 * 
 * @author rvesse
 * 
 * @param <T>
 */
public abstract class AbstractRunner<T extends Options> implements Runner<T> {

	private static final Logger logger = LoggerFactory
			.getLogger(AbstractRunner.class);

	private OperationMixRunner inOrderRunner = new InOrderOperationMixRunner();
	private OperationMixRunner defaultRunner = new DefaultOperationMixRunner();
	private OperationRunner defaultOpRunner = new DefaultOperationRunner();
	private boolean halted = false;

	@Override
	public void halt(T options, String message) {
		System.err.println("Benchmarking Aborted - Halting due to " + message);
		if (!halted) {
			// Make sure we only reallyHalt once, otherwise, we infinite loop
			// with bad behavior from a listener.
			halted = true;
			reallyHalt(options, message);
		}
	}

	/**
	 * Helper method that ensures we really halt without going into an infinite
	 * loop
	 * 
	 * @param options
	 *            Options
	 * @param message
	 *            Message
	 */
	private void reallyHalt(T options, String message) {
		// Inform Listeners that Benchmarking Finished with a halt condition
		for (ProgressListener l : options.getListeners()) {
			try {
				l.finish(this, options, false);
			} catch (Exception e) {
				System.err.println(l.getClass().getName()
						+ " encountered an error during handleFinish() - "
						+ e.getMessage());
				if (options.getHaltOnError() || options.getHaltAny()) {
					halt(options, l.getClass().getName()
							+ " encountering an error during finish");
				}
			}
		}

		// Then perform actual halting depending on configured behaviour
		switch (options.getHaltBehaviour()) {
		case EXIT:
			System.exit(2);
		case THROW_EXCEPTION:
			throw new RuntimeException("Benchmarking Aborted - Halting due to "
					+ message);
		}
	}

	@Override
	public void halt(T options, Exception e) {
		halt(options, e.getMessage());
	}

	@Override
	public void reportProgress(T options) {
		this.reportPartialProgress(options, "\n");
	}

	@Override
	public void reportPartialProgress(T options, String message) {
		for (ProgressListener l : options.getListeners()) {
			try {
				l.progress(this, options, message);
			} catch (Exception e) {
				System.err.println(l.getClass().getName()
						+ " encountered an error during handleProgress() - "
						+ e.getMessage());
				if (options.getHaltAny() || options.getHaltOnError()) {
					halt(options, l.getClass().getName()
							+ " encountering an error in progress reporting");
				}
			}
		}
	}

	@Override
	public void reportProgress(T options, String message) {
		this.reportPartialProgress(options, message + '\n');
	}

	@Override
	public void reportBeforeOperation(T options, Operation operation) {
		for (ProgressListener l : options.getListeners()) {
			try {
				l.beforeOperation(this, options, operation);
			} catch (Exception e) {
				System.err.println(l.getClass().getName()
						+ " encountered an error during beforeOperation() - "
						+ e.getMessage());
				if (options.getHaltAny() || options.getHaltOnTimeout()) {
					halt(options, l.getClass().getName()
							+ " encountering an error in progress reporting");
				}
			}
		}
	}

	@Override
	public void reportAfterOperation(T options, Operation operation,
			OperationRun run) {
		for (ProgressListener l : options.getListeners()) {
			try {
				l.afterOperation(this, options, operation, run);
			} catch (Exception e) {
				System.err.println(l.getClass().getName()
						+ " encountered an error during afterOperation() - "
						+ e.getMessage());
				if (options.getHaltAny() || options.getHaltOnTimeout()) {
					halt(options, l.getClass().getName()
							+ " encountering an error in progress reporting");
				}
			}
		}
	}

	@Override
	public void reportBeforeOperationMix(T options, OperationMix mix) {
		for (ProgressListener l : options.getListeners()) {
			try {
				l.beforeOperationMix(this, options, mix);
			} catch (Exception e) {
				System.err
						.println(l.getClass().getName()
								+ " encountered an error during beforeOperationMix() - "
								+ e.getMessage());
				if (options.getHaltAny() || options.getHaltOnError()) {
					halt(options, l.getClass().getName()
							+ " encountering an error in progress reporting");
				}
			}
		}
	}

	@Override
	public void reportAfterOperationMix(T options, OperationMix mix,
			OperationMixRun run) {
		for (ProgressListener l : options.getListeners()) {
			try {
				l.afterOperationMix(this, options, mix, run);
			} catch (Exception e) {
				System.err.println(l.getClass().getName()
						+ " encountered an error during afterOperationMix() - "
						+ e.getMessage());
				if (options.getHaltAny() || options.getHaltOnError()) {
					halt(options, l.getClass().getName()
							+ " encountering an error in progress reporting");
				}
			}
		}
	}

	/**
	 * Checks that the query endpoint/in-memory dataset being used passes some
	 * basic queries to see if it is up and running
	 * <p>
	 * May be overridden by runner implementations to change the sanity checking
	 * constraints
	 * </p>
	 * 
	 * @param options
	 *            Options
	 * 
	 * @return Whether the endpoint/in-memory dataset passed some basic sanity
	 *         checks
	 */
	protected boolean checkSanity(T options) {
		reportProgress(options,
				"Sanity checking the user specified remote endpoint/in-memory dataset...");
		String[] checks = this.getSanityCheckQueries();

		int passed = 0;
		for (int i = 0; i < checks.length; i++) {
			Query q = QueryFactory.create(checks[i]);

			// Remember to account for whether we are running against a remote
			// service or an in-memory dataset
			// Favours testing the remote service even if both are defined
			FutureTask<OperationRun> task = new FutureTask<OperationRun>(
					options.getQueryEndpoint() != null ? new RemoteQueryCallable<T>(
							q, this, options) : new InMemoryQueryCallable<T>(q,
							this, options));
			reportPartialProgress(options, "Sanity Check " + (i + 1) + " of "
					+ checks.length + "...");
			try {
				// Run the operation using a 30 second timeout
				options.getExecutor().submit(task);
				OperationRun run = task.get(30, TimeUnit.SECONDS);
				if (run.wasSuccessful()) {
					reportProgress(options, "OK");
					passed++;
				} else {
					reportProgress(options, "Failed with error - " + run.getErrorMessage());
				}
			} catch (TimeoutException tEx) {
				logger.error("Sanity Check execeeded 30 Second Timeout - "
						+ tEx.getMessage());
				reportProgress(options, "Failed");
			} catch (InterruptedException e) {
				logger.error("Sanity Check was interrupted - " + e.getMessage());
				reportProgress(options, "Failed");
			} catch (ExecutionException e) {
				logger.error("Sanity Check encountered an error - "
						+ e.getMessage());
				reportProgress(options, "Failed");
			}
		}

		// Compare with minimum of checks length and sanity check level because
		// it is possible that the user has configured a sanity check level
		// greater than the number of possible checks. In this case then we
		// require that all checks pass hence the use of min()
		return (passed >= Math
				.min(options.getSanityCheckLevel(), checks.length));
	}

	/**
	 * Gets the queries used for sanity checking
	 * 
	 * @return Sanity checking queries
	 */
	protected String[] getSanityCheckQueries() {
		return new String[] { "ASK WHERE { }", "SELECT * WHERE { }",
				"SELECT * WHERE { ?s a ?type } LIMIT 1" };
	}

	protected void checkOperations(T options) {
		Iterator<Operation> ops = options.getOperationMix().getOperations();
		while (ops.hasNext()) {
			Operation op = ops.next();
			if (!op.canRun(this, options)) {
				System.err
						.println("A specified operation cannot run with the available options");
				halt(options,
						"Operation " + op.getName() + " of type "
								+ op.getType()
								+ " cannot run with the available options");
			}
		}
	}

	/**
	 * Runs the tear down mix (if any) guaranteeing that operations run in
	 * precisely the order specified
	 * 
	 * @param options
	 *            Options
	 */
	protected void runTeardown(T options) {
		if (options.getTeardownMix() != null) {
			// Run the tear down
			reportProgress(options, "Running teardown mix...");
			reportBeforeOperationMix(options, options.getTeardownMix());
			OperationMixRun r = this.inOrderRunner.run(this, options,
					options.getTeardownMix());
			reportAfterOperationMix(options, options.getTeardownMix(), r);
			reportProgress(options);
		}
	}

	/**
	 * Runs the setup mix (if any) guaranteeing that operations run in precisely
	 * the order specified
	 * 
	 * @param options
	 *            Options
	 */
	protected void runSetup(T options) {
		if (options.getSetupMix() != null) {
			// Run the setup
			reportProgress(options, "Running setup mix...");
			reportBeforeOperationMix(options, options.getSetupMix());
			OperationMixRun r = this.inOrderRunner.run(this, options,
					options.getSetupMix());
			reportAfterOperationMix(options, options.getTeardownMix(), r);
			reportProgress(options);
		}
	}

	/**
	 * Runs an operation using the configured operation runner, if there is no
	 * configured runner then it uses the {@link DefaultOperationRunner} to run
	 * the operation.
	 * 
	 * @param options
	 *            Options
	 * @param op
	 *            Operation to run
	 * @return Operation run information
	 */
	protected OperationRun runOp(T options, Operation op) {
		OperationRunner runner = options.getOperationRunner();
		if (runner == null)
			runner = this.defaultOpRunner;
		return runner.run(this, options, op);
	}

	/**
	 * Runs the actual operation mix using the configured operation mix runner,
	 * if there is no configured runner then it uses the
	 * {@link DefaultOperationMixRunner} to run the mix
	 * 
	 * @param options
	 *            Options
	 * @return Operation Mix run
	 */
	protected OperationMixRun runMix(T options) {
		OperationMixRunner runner = options.getMixRunner();
		if (runner == null)
			runner = this.defaultRunner;

		return runner.run(this, options, options.getOperationMix());
	}

	/**
	 * Reports categorized errors
	 * 
	 * @param options
	 *            Options
	 * @param categorizedErrors
	 *            Categorized Errors
	 */
	protected void reportCategorizedErrors(T options,
			Map<Integer, List<OperationRun>> categorizedErrors) {
		reportProgress(options, "Errors by Category: ");
		for (Integer category : categorizedErrors.keySet()) {
			String description = ErrorCategories.getDescription(category);
			if (description == null)
				description = String.format("  Unknown Category %d", category);
			reportProgress(options, "  " + description + ": "
					+ categorizedErrors.get(category).size() + " error(s)");
		}
	}

	/**
	 * Informs all registered progress listeners that the run has finished
	 * 
	 * @param options
	 *            Options
	 */
	protected void finished(T options) {
		for (ProgressListener l : options.getListeners()) {
			try {
				l.finish(this, options, true);
			} catch (Exception e) {
				System.err.println(l.getClass().getName()
						+ " encountered an error during handleFinish() - "
						+ e.getMessage());
				e.printStackTrace(System.err);
				if (options.getHaltOnError() || options.getHaltAny()) {
					halt(options, l.getClass().getName()
							+ " encountering an error during finish");
				}
			}
		}
	}

	/**
	 * Informs all registered progress listeners that the run has started
	 * 
	 * @param options
	 *            Options
	 */
	protected void started(T options) {
		for (ProgressListener l : options.getListeners()) {
			try {
				l.start(this, options);
			} catch (Exception e) {
				System.err.println(l.getClass().getName()
						+ " encountered an error during handleStarted() - "
						+ e.getMessage());
				e.printStackTrace(System.err);
				// IMPORTANT - A startup error always halts benchmarking
				// regardless of halting options
				halt(options, l.getClass().getName()
						+ " encountered an error in startup");
			}
		}
	}

	/**
	 * Reports information about the general options specified
	 * 
	 * @param options
	 *            Options
	 */
	protected void reportGeneralOptions(T options) {
		reportProgress(options, "General Options");
		reportProgress(options, "---------------");
		reportProgress(options);
		reportProgress(options,
				"Query Endpoint = "
						+ (options.getQueryEndpoint() == null ? "not specified"
								: options.getQueryEndpoint()));
		reportProgress(
				options,
				"Update Endpoint = "
						+ (options.getUpdateEndpoint() == null ? "not specified"
								: options.getUpdateEndpoint()));
		reportProgress(
				options,
				"Graph Store Protocol Endpoint = "
						+ (options.getGraphStoreEndpoint() == null ? "not specified"
								: options.getGraphStoreEndpoint()));
		if (options.getCustomEndpoints().size() > 0) {
			for (String key : options.getCustomEndpoints().keySet()) {
				String value = options.getCustomEndpoint(key);
				reportProgress(options, "Custom Endpoint (" + key + ") = "
						+ (value == null ? "not specified" : value));
			}
		}
		reportProgress(options,
				"Sanity Checking Level = " + options.getSanityCheckLevel());
		reportProgress(options,
				"Random Operation Order = "
						+ (options.getRandomizeOrder() ? "On" : "Off"));
		reportProgress(options, "Setup Mix = "
				+ (options.getSetupMix() != null ? options.getSetupMix().size()
						+ " Operation(s)" : "disabled"));
		reportProgress(options, "Teardown Mix = "
				+ (options.getTeardownMix() != null ? options.getTeardownMix()
						.size() + " Operation(s)" : "disabled"));
		reportProgress(options, "Timeout = "
				+ (options.getTimeout() > 0 ? options.getTimeout() + " seconds"
						: "disabled"));
		reportProgress(options,
				"Max Delay between Operations = " + options.getMaxDelay()
						+ " milliseconds");
		reportProgress(options,
				"Halt on Timeout = " + options.getHaltOnTimeout());
		reportProgress(options, "Halt on Error = " + options.getHaltOnError());
		reportProgress(options, "Halt Any = " + options.getHaltAny());
		reportProgress(options,
				"Result Limit = "
						+ (options.getLimit() <= 0 ? "Query Specified"
								: options.getLimit()));
		reportProgress(options, "Result Counting = "
				+ (options.getNoCount() ? "Disabled" : "Enabled"));
		reportProgress(options,
				"ASK Results Format = " + options.getResultsAskFormat());
		reportProgress(options,
				"Graph Results Format = " + options.getResultsGraphFormat());
		reportProgress(options,
				"SELECT Results Format = " + options.getResultsSelectFormat());
		reportProgress(options,
				"Compression = "
						+ (options.getAllowCompression() ? "enabled"
								: "disabled"));
		reportProgress(options,
				"Parallel Threads = " + options.getParallelThreads());
		reportProgress(options,
				"Authentication = "
						+ (options.getAuthenticator() != null ? "enabled"
								: "disabled"));
		reportProgress(options);
	}

	/**
	 * Reports a summary of the operation
	 * 
	 * @param options
	 *            Options
	 * @param op
	 *            Operation
	 */
	protected void reportOperationSummary(T options, Operation op) {
		reportProgress(options,
				"Operation ID " + op.getId() + " of type " + op.getType()
						+ " (" + op.getName() + ")");
		reportProgress(options, "Total Runs: " + op.getStats().getRunCount());
		reportProgress(options, "Total Errors: "
				+ op.getStats().getTotalErrors());
		if (op.getStats().getTotalErrors() > 0) {
			// Show errors by category
			Map<Integer, List<OperationRun>> categorizedErrors = op.getStats()
					.getCategorizedErrors();
			this.reportCategorizedErrors(options, categorizedErrors);
		}
		reportProgress(options, "Total Results: "
				+ op.getStats().getTotalResults());
		reportProgress(options, "Average Results: "
				+ op.getStats().getAverageResults());
		reportProgress(
				options,
				"Total Response Time: "
						+ FormatUtils.formatSeconds(op.getStats()
								.getTotalResponseTime()));
		reportProgress(
				options,
				"Average Response Time (Arithmetic): "
						+ FormatUtils.formatSeconds(op.getStats()
								.getAverageResponseTime()));
		reportProgress(
				options,
				"Total Runtime: "
						+ FormatUtils.formatSeconds(op.getStats()
								.getTotalRuntime()));
		if (options.getParallelThreads() > 1)
			reportProgress(
					options,
					"Actual Runtime: "
							+ FormatUtils.formatSeconds(op.getStats()
									.getActualRuntime()));
		reportProgress(
				options,
				"Average Runtime (Arithmetic): "
						+ FormatUtils.formatSeconds(op.getStats()
								.getAverageRuntime()));
		if (options.getParallelThreads() > 1)
			reportProgress(
					options,
					"Actual Average Runtime (Arithmetic): "
							+ FormatUtils.formatSeconds(op.getStats()
									.getActualAverageRuntime()));
		reportProgress(
				options,
				"Average Runtime (Geometric): "
						+ FormatUtils.formatSeconds(op.getStats()
								.getGeometricAverageRuntime()));
		reportProgress(
				options,
				"Minimum Runtime: "
						+ FormatUtils.formatSeconds(op.getStats()
								.getMinimumRuntime()));
		reportProgress(
				options,
				"Maximum Runtime: "
						+ FormatUtils.formatSeconds(op.getStats()
								.getMaximumRuntime()));
		reportProgress(
				options,
				"Runtime Variance: "
						+ FormatUtils
								.formatSeconds(op.getStats().getVariance()));
		reportProgress(
				options,
				"Runtime Standard Deviation: "
						+ FormatUtils.formatSeconds(op.getStats()
								.getStandardDeviation()));
		reportProgress(options);
		reportProgress(options, "Operations per Second: "
				+ op.getStats().getOperationsPerSecond());
		if (options.getParallelThreads() > 1)
			reportProgress(options, "Actual Operations per Second: "
					+ op.getStats().getActualOperationsPerSecond());
		reportProgress(options, "Operations per Hour: "
				+ op.getStats().getOperationsPerHour());
		if (options.getParallelThreads() > 1)
			reportProgress(options, "Actual Operations per Hour: "
					+ op.getStats().getActualOperationsPerHour());
		reportProgress(options);
	}

	/**
	 * Helper method for running the sanity checks
	 * 
	 * @param options
	 *            Options
	 */
	protected void runSanityChecks(T options) {
		if (options.getSanityCheckLevel() > 0) {
			if (checkSanity(options)) {
				reportProgress(options,
						"Sanity Checks passed required sanity level...");
				reportProgress(options);
			} else {
				// Sanity checks failed so halt
				halt(options,
						"Sanity Checks failed to meet required sanity level, please ensure that the endpoint specified is actually available and working.  If this is the case try setting the sanity checking level to zero and retrying");
			}
		} else {
			reportProgress(options, "Sanity Check skipped by user...");
		}
	}

}