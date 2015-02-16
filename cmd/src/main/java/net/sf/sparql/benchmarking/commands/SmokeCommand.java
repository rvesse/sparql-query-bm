package net.sf.sparql.benchmarking.commands;

import java.io.IOException;

import net.sf.sparql.benchmarking.options.Options;
import net.sf.sparql.benchmarking.options.OptionsImpl;
import net.sf.sparql.benchmarking.options.SoakOptions;
import net.sf.sparql.benchmarking.runners.AbstractRunner;
import net.sf.sparql.benchmarking.runners.SmokeRunner;
import net.sf.sparql.benchmarking.runners.mix.InOrderOperationMixRunner;

import org.apache.commons.lang.ArrayUtils;

import io.airlift.airline.Command;
import io.airlift.airline.Option;
import io.airlift.airline.ParseArgumentsMissingException;
import io.airlift.airline.ParseArgumentsUnexpectedException;
import io.airlift.airline.ParseOptionMissingException;
import io.airlift.airline.ParseOptionMissingValueException;
import io.airlift.airline.SingleCommand;

/**
 * Runs the smoke tester from the Command Line
 * 
 * @author rvesse
 * 
 */
@Command(name = "smoke", description = "Runs a smoke test which consists of running the configured operation mix once to see whether all operations pass successfully.")
public class SmokeCommand extends AbstractCommand {

	public static final int SMOKE_TESTS_FAILED = 15;

	/**
	 * Enable random order option
	 */
	@Option(name = { "--rand", "--random" }, description = "Enables randomized ordering of operations within mixes, smoke tests are normally run in-order.")
	public boolean random = false;

	/**
	 * Runs the command line smoke testing process
	 * 
	 * @param args
	 *            Arguments
	 */
	public static void main(String[] args) {
		int exitCode = ExitCodes.SUCCESS;
		try {
			// Parse options
			SmokeCommand cmd = SingleCommand.singleCommand(SmokeCommand.class)
					.parse(args);

			// Show help if requested
			if (cmd.helpOption.showHelpIfRequested()) {
				return;
			}

			// Run testing
			exitCode = cmd.run();
		} catch (ParseOptionMissingException e) {
			if (!ArrayUtils.contains(args, "--help")) {
				System.err.println(ANSI_RED + e.getMessage());
				System.err.println();
			}
			showUsage(SmokeCommand.class);
			exitCode = ExitCodes.REQUIRED_OPTION_MISSING;
		} catch (ParseOptionMissingValueException e) {
			if (!ArrayUtils.contains(args, "--help")) {
				System.err.println(ANSI_RED + e.getMessage());
				System.err.println();
			}
			showUsage(SmokeCommand.class);
			exitCode = ExitCodes.REQUIRED_OPTION_VALUE_MISSING;
		} catch (ParseArgumentsMissingException e) {
			System.err.println(ANSI_RED + e.getMessage());
			System.err.println();
			exitCode = ExitCodes.REQUIRED_ARGUMENTS_MISSING;
		} catch (ParseArgumentsUnexpectedException e) {
			System.err.println(ANSI_RED + e.getMessage());
			System.err.println();
			exitCode = ExitCodes.UNEXPECTED_ARGUMENT;
		} catch (IOException e) {
			System.err.println(ANSI_RED + e.getMessage());
			System.err.println();
			exitCode = ExitCodes.IO_ERROR;
		} catch (Throwable e) {
			System.err.println(ANSI_RED + e.getMessage());
			e.printStackTrace(System.err);
			exitCode = ExitCodes.UNEXPECTED_ERROR;
		} finally {
			System.err.println(ANSI_RESET);
			System.exit(exitCode);
		}
	}

	@Override
	protected int run() throws IOException {
		// Prepare options
		Options options = new OptionsImpl();
		this.noRandom = true;
		this.applyStandardOptions(options);

		// Run soak tests
		AbstractRunner<Options> runner = new SmokeRunner();
		runner.run(options);

		// Return appropriate error code depending on whether there were any
		// errors
		return options.getOperationMix().getStats().getTotalErrors() == 0 ? ExitCodes.SUCCESS
				: ExitCodes.FAILURE;
	}

	/**
	 * Applies smoke testing options provided by this command
	 * 
	 * @param options
	 *            Options to populate
	 */
	protected void applySoakOptions(SoakOptions options) {
		if (this.random) {
			// Use can explicitly turn randomisation back on
			options.setRandomizeOrder(true);
		} else {
			// Otherwise default behaviour is to force in-order running
			options.setRandomizeOrder(false);
			options.setMixRunner(new InOrderOperationMixRunner());
		}
	}
}
