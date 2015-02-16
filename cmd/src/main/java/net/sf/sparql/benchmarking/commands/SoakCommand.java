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

package net.sf.sparql.benchmarking.commands;

import io.airlift.airline.Command;
import io.airlift.airline.Option;
import io.airlift.airline.ParseArgumentsMissingException;
import io.airlift.airline.ParseArgumentsUnexpectedException;
import io.airlift.airline.ParseOptionMissingException;
import io.airlift.airline.ParseOptionMissingValueException;
import io.airlift.airline.SingleCommand;

import java.io.IOException;

import org.apache.commons.lang.ArrayUtils;

import net.sf.sparql.benchmarking.options.Options;
import net.sf.sparql.benchmarking.options.SoakOptions;
import net.sf.sparql.benchmarking.runners.AbstractRunner;
import net.sf.sparql.benchmarking.runners.SoakRunner;

/**
 * Runs the soak tester from the Command Line
 * 
 * @author rvesse
 * 
 */
@Command(name = "soak", description = "Runs a soak test which consists of running the configured operation mix continuously for some period of time in order to soak test a system.")
public class SoakCommand extends AbstractCommand {

    /**
     * Max runs option
     */
    @Option(name = { "-r", "--runs", "--max-runs" }, arity = 1, title = "Max Runs", description = "Sets the maximum number of runs to perform, when set tests will run until the specified number of runs is reached or the maximum runtime is exceeded.  Either this or the --runtime option must be set to configure how long soak tests will run for.")
    public int maxRuns = SoakOptions.DEFAULT_MAX_RUNS;

    /**
     * Max runtime options
     */
    @Option(name = { "--runtime", "--max-runtime" }, arity = 1, title = "Max Runtime", description = "Sets the maximum runtime of the soak tests, when set tests will run until this time is exceeded or the maximum runs set is reached.  Either this or the -r/--runs option must be set to configure how long soak tests will run for.")
    public long runtime = SoakOptions.DEFAULT_RUNTIME;

    /**
     * Runs the command line soak testing process
     * 
     * @param args
     *            Arguments
     */
    public static void main(String[] args) {
        int exitCode = ExitCodes.SUCCESS;
        try {
            // Parse options
            SoakCommand cmd = SingleCommand.singleCommand(SoakCommand.class).parse(args);

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
            showUsage(SoakCommand.class);
            exitCode = ExitCodes.REQUIRED_OPTION_MISSING;
        } catch (ParseOptionMissingValueException e) {
            if (!ArrayUtils.contains(args, "--help")) {
                System.err.println(ANSI_RED + e.getMessage());
                System.err.println();
            }
            showUsage(SoakCommand.class);
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
        SoakOptions options = new SoakOptions();
        this.applyStandardOptions(options);
        this.applySoakOptions(options);

        // Run soak tests
        AbstractRunner<SoakOptions> runner = new SoakRunner();
        runner.run(options);
        
        // Soak tests always return SUCCESS
        return ExitCodes.SUCCESS;
    }

    /**
     * Applies soak testing options provided by this command
     * 
     * @param options
     *            Soak options to populate
     */
    protected <T extends Options> void applySoakOptions(SoakOptions options) {
        options.setMaxRuns(this.maxRuns);
        options.setMaxRuntime(this.runtime);
    }

}
