package net.sf.sparql.benchmarking.commands;

import java.io.IOException;

import net.sf.sparql.benchmarking.options.Options;
import net.sf.sparql.benchmarking.options.OptionsImpl;
import net.sf.sparql.benchmarking.options.SoakOptions;
import net.sf.sparql.benchmarking.runners.AbstractRunner;
import net.sf.sparql.benchmarking.runners.SmokeRunner;
import net.sf.sparql.benchmarking.runners.mix.InOrderOperationMixRunner;

import org.apache.commons.lang.ArrayUtils;

import io.airlift.command.Command;
import io.airlift.command.Option;
import io.airlift.command.ParseArgumentsMissingException;
import io.airlift.command.ParseArgumentsUnexpectedException;
import io.airlift.command.ParseOptionMissingException;
import io.airlift.command.ParseOptionMissingValueException;
import io.airlift.command.SingleCommand;

/**
 * Runs the smoke tester from the Command Line
 * 
 * @author rvesse
 * 
 */
@Command(name = "smoke", description = "Runs a smoke test which consists of running the configured operation mix once to see whether all operations pass successfully.")
public class SmokeCommand extends AbstractCommand {

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
		int exitCode = 0;
		try {
			// Parse options
			SmokeCommand cmd = SingleCommand.singleCommand(SmokeCommand.class)
					.parse(args);

			// Show help if requested
			if (cmd.helpOption.showHelpIfRequested()) {
				return;
			}

			// Run testing
			cmd.run();

			// Successful exit
			exitCode = 0;
		} catch (ParseOptionMissingException e) {
			if (!ArrayUtils.contains(args, "--help")) {
				System.err.println(ANSI_RED + e.getMessage());
				System.err.println();
			}
			showUsage(SmokeCommand.class);
			exitCode = 1;
		} catch (ParseOptionMissingValueException e) {
			if (!ArrayUtils.contains(args, "--help")) {
				System.err.println(ANSI_RED + e.getMessage());
				System.err.println();
			}
			showUsage(SmokeCommand.class);
			exitCode = 2;
		} catch (ParseArgumentsMissingException e) {
			System.err.println(ANSI_RED + e.getMessage());
			System.err.println();
			exitCode = 3;
		} catch (ParseArgumentsUnexpectedException e) {
			System.err.println(ANSI_RED + e.getMessage());
			System.err.println();
			exitCode = 4;
		} catch (IOException e) {
			System.err.println(ANSI_RED + e.getMessage());
			System.err.println();
			exitCode = 5;
		} catch (Throwable e) {
			System.err.println(ANSI_RED + e.getMessage());
			e.printStackTrace(System.err);
			exitCode = 10;
		} finally {
			System.err.println(ANSI_RESET);
			System.exit(exitCode);
		}
	}

	@Override
	protected void run() throws IOException {
		// Prepare options
		Options options = new OptionsImpl();
		this.noRandom = true;
		this.applyStandardOptions(options);

		// Run soak tests
		AbstractRunner<Options> runner = new SmokeRunner();
		runner.run(options);
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
