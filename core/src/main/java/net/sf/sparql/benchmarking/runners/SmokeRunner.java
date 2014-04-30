package net.sf.sparql.benchmarking.runners;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.joda.time.Instant;

import net.sf.sparql.benchmarking.operations.Operation;
import net.sf.sparql.benchmarking.options.Options;
import net.sf.sparql.benchmarking.stats.OperationMixRun;
import net.sf.sparql.benchmarking.stats.OperationRun;
import net.sf.sparql.benchmarking.util.ConvertUtils;
import net.sf.sparql.benchmarking.util.FormatUtils;

/**
 * A smoke test runner
 * <p>
 * The smoke test runner is a runner designed to run an operation mix through
 * only once simply to validate whether all operations are successful. This is
 * useful for validation and integration testing purposes.
 * </p>
 * 
 * @author rvesse
 * 
 */
public class SmokeRunner extends AbstractRunner<Options> {

	@Override
	public void run(Options options) {
		// Inform Listeners that we are starting benchmarking
		started(options);

		// Validate options
		if (options.getOperationMix() == null) {
			System.err.println("Operation Mix has not been set");
			halt(options, "No Operation Mix was set");
		}

		Iterator<Operation> ops;
		checkOperations(options);

		// Print Options for User Reference
		reportGeneralOptions(options);

		// Sanity Checking
		if (options.getSanityCheckLevel() > 0) {
			if (checkSanity(options)) {
				reportProgress(options,
						"Sanity Checks passed required sanity level...");
				reportProgress(options);
			} else {
				reportProgress(
						options,
						"Sanity Checks failed to meet required sanity level, please ensure that the endpoint specified is actually available and working.  If this is the case try setting the sanity check level to zero and retrying");
				System.exit(1);
			}
		} else {
			reportProgress(options, "Sanity Check skipped by user...");
		}

		// Summarise operations to be used
		reportProgress(options, "Starting smoke testing...");
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

		// Actual Run
		reportProgress(options, "Running smoke tests...");
		long startTime = System.nanoTime();
		Instant startInstant = Instant.now();
		Instant endInstant = startInstant;
		reportProgress(options,
				"Start Time: " + FormatUtils.formatInstant(startInstant));
		reportProgress(options);

		// Smoke tests run the mix only once
		reportBeforeOperationMix(options, options.getOperationMix());
		OperationMixRun r = this.runMix(options);
		reportAfterOperationMix(options, null, r);
		reportProgress(options);

		// Get end time
		long endTime = System.nanoTime();
		endInstant = Instant.now();

		// Tear down
		runTeardown(options);

		reportProgress(options, "Finished smoke tests");
		reportProgress(options);

		// Summarise Operations
		if (options.getOperationMix().getStats().getTotalErrors() > 0) {
			reportProgress(options, "Failed Operation Summary");
			reportProgress(options, "-----------------");
			reportProgress(options);

			ops = options.getOperationMix().getOperations();
			while (ops.hasNext()) {
				Operation op = ops.next();

				// Don't report operations if they had zero errors
				if (op.getStats().getTotalErrors() == 0)
					continue;

				// Print Summary
				reportOperationSummary(options, op);
			}
		}

		reportProgress(options, "Smoke Test Summary");
		reportProgress(options, "-----------------");
		reportProgress(options);
		reportProgress(options,
				"Result: "
						+ (options.getOperationMix().getStats()
								.getTotalErrors() == 0 ? "Pass" : "Failure"));
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
		reportProgress(options);

		// Finally inform listeners that running finished OK
		finished(options);
	}
}
