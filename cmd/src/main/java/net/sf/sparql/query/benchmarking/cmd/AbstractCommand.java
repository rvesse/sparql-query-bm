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

package net.sf.sparql.query.benchmarking.cmd;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import net.sf.sparql.benchmarking.loader.OperationMixLoader;
import net.sf.sparql.benchmarking.loader.OperationMixLoaderRegistry;
import net.sf.sparql.benchmarking.monitoring.ConsoleProgressListener;
import net.sf.sparql.benchmarking.options.HaltBehaviour;
import net.sf.sparql.benchmarking.options.Options;
import net.sf.sparql.benchmarking.runners.mix.SamplingOperationMixRunner;
import net.sf.sparql.benchmarking.util.AuthUtils;
import net.sf.sparql.benchmarking.util.FileUtils;
import io.airlift.command.Help;
import io.airlift.command.HelpOption;
import io.airlift.command.Option;
import io.airlift.command.SingleCommand;
import io.airlift.command.model.CommandMetadata;

/**
 * Abstract command which provides all the common options
 * 
 * @author rvesse
 * 
 */
public abstract class AbstractCommand {

    protected static final String ANSI_RED = "\u001B[31m";
    protected static final String ANSI_RESET = "\u001B[0m";

    /**
     * Help option
     */
    @Inject
    public HelpOption helpOption;

    /**
     * Halt on timeout option
     */
    @Option(name = "--halt-on-timeout", description = "Sets whether the tests will halt once an operation times out.")
    public boolean haltOnTimeout = false;

    /**
     * Halt on error option
     */
    @Option(name = "--halt-on-error", description = "Sets whether the tests will halt once an error is encountered.")
    public boolean haltOnError = false;

    /**
     * Halt on any option
     */
    @Option(name = "--halt-any", description = "Sets whether the tests will halt once any issue is encountered.")
    public boolean haltAny = false;

    /**
     * Timeout option
     */
    @Option(name = { "-t", "--timeout" }, arity = 1, title = "Seconds", description = "Sets the operation timeout in seconds, a zero/negative value is used to indicate no timeout.")
    public int timeout = Options.DEFAULT_TIMEOUT;

    /**
     * Parallel threads option
     */
    @Option(name = { "-p", "--parallel" }, arity = 1, title = "Threads", description = "Sets the number of parallel threads to use for testing.")
    public int parallelThreads = 1;

    /**
     * Maximum delay between operations option
     */
    @Option(name = { "-d", "--max-delay" }, arity = 1, title = "Milliseconds", description = "Sets the maximum delay between operations in milliseconds.")
    public int maxDelay = Options.DEFAULT_MAX_DELAY;

    /**
     * Query endpoint option
     */
    @Option(name = { "-q", "--query-endpoint" }, arity = 1, title = "Query Endpoint URI", description = "Sets the SPARQL query endpoint URI.")
    public String queryEndpoint;

    /**
     * Update endpoint option
     */
    @Option(name = { "-u", "--update-endpoint" }, arity = 1, title = "Update Endpoint URI", description = "Sets the SPARQL update endpoint URI.")
    public String updateEndpoint;

    /**
     * Graph store endpoint option
     */
    @Option(name = { "-g", "--gsp-endpoint" }, arity = 1, title = "Graph Store Endpoint URI", description = "Sets the SPARQL graph store protocol endpoint URI.")
    public String gspEndpoint;

    /**
     * Enable compression option
     */
    @Option(name = { "--compression", "--allow-compression" }, description = "Enables the use of GZip/Deflate compression when communicating with the server assuming the server supports it.")
    public boolean enableCompression = false;

    /**
     * ASK Format option
     */
    @Option(name = { "--results-ask", "--ask-format" }, arity = 1, title = "MIME Type", description = "Sets the results format that will be requested for ASK queries.")
    public String askFormat = Options.DEFAULT_FORMAT_ASK;

    /**
     * SELECT Format option
     */
    @Option(name = { "--results-select", "--select-format" }, arity = 1, title = "MIME Type", description = "Sets the results format that will be requested for SELECT queries.")
    public String selectFormat = Options.DEFAULT_FORMAT_SELECT;

    /**
     * Graph Format option
     */
    @Option(name = { "--results-graph", "--graph-format" }, arity = 1, title = "MIME Type", description = "Sets the results format that will be requested for CONSTRUCT/DESCRIBE queries and operations that retrieve a graph.")
    public String graphFormat = Options.DEFAULT_FORMAT_GRAPH;

    /**
     * Mix option
     */
    @Option(name = { "-m", "--mix" }, arity = 1, title = "Mix File", required = true, description = "Sets the operation mix file which provides the mix of operations to be run.")
    public String mixFile;

    /**
     * Sanity checking option
     */
    @Option(name = { "-s", "--sanity-checks" }, arity = 1, title = "Level", description = "Sets the sanity checking level, this is the number of basic sanity checks the system to be tested must pass before actual tests will be started.  This should normally be set to a value no greater than 3 though some commands may support higher sanity checking levels.")
    public int sanityCheckLevel = Options.DEFAULT_SANITY_CHECKS;

    /**
     * Disable random order option
     */
    @Option(name = { "--norand", "--no-random" }, description = "Disables randomized ordering of operations within mixes.")
    public boolean noRandom = false;

    /**
     * Sample size option
     */
    @Option(name = { "--sample-size" }, arity = 1, title = "Sample Size", description = "Sets the sample size used, this controls how many of the operations in the mix are run in each run of the mix.  You may also want to set --sample-repeats when setting a sample size larger than the mix size otherwise the sample size will be capped at the mix size.  When neither this nor --sample-repeats is specified the default behaviour of running every operation in every mix run is used.")
    public int sampleSize = 0;

    /**
     * Sample repeats option
     */
    @Option(name = { "--sample-repeats" }, description = "Enables repeats for sampling, this allows an operation to potentially run multiple times within a single run of the mix.  You may also want to set --sample-size to control how many operations are run in each mix run.  When neither this nor --sample-repeats is specified the default behaviour of running every operation in every mix run is used.")
    public boolean sampleRepeats = false;

    /**
     * User name option
     */
    @Option(name = { "--username" }, arity = 1, title = "Username", description = "Sets the user name used for authentication.")
    public String username;

    /**
     * Password option
     */
    @Option(name = { "--password" }, arity = 1, title = "Password", description = "Sets the password used for authentication.")
    public String password;

    /**
     * Pre-emptive authentication option
     */
    @Option(name = { "--preemptive-auth" }, description = "Enables pre-emptive authentication, only has an effect if HTTP basic authentication is being used.")
    public boolean preemptiveAuth = false;

    /**
     * Form URL option
     */
    @Option(name = { "--form-url" }, arity = 1, title = "Form URL", description = "Sets the URL used to login for form based authentication, this option is required if you wish to use form based authentication.  When not specified and the --username and --password options are specified standard HTTP authentication is assumed.")
    public String formUrl;

    /**
     * Form user name field
     */
    @Option(name = { "--form-user-field" }, arity = 1, title = "Form User Field", description = "Sets the user name field used for form based authentication (defaults to httpd_username).")
    public String formUserField;

    /**
     * Form password field
     */
    @Option(name = { "--form-password-field" }, arity = 1, title = "Form Password Field", description = "Sets the password field used for form based authentication (defaults to httpd_password).")
    public String formPwdField;

    /**
     * Logging to console option
     */
    @Option(name = { "--logging" }, description = "Enables log output to the console.")
    public boolean logToConsole = false;

    /**
     * Logging to file option
     */
    @Option(name = { "--log-file" }, arity = 1, title = "File", description = "Enables logging to a file.")
    public String logFile;

    /**
     * Debug option
     */
    @Option(name = { "--debug" }, description = "Enables debug level logging, must be used with the --logging or --log-file option to have a visible effect.")
    public boolean debug = false;

    /**
     * Trace option
     */
    @Option(name = { "--trace" }, description = "Enables trace level logging, must be used with the --logging or --log-file option to have a visible effect.")
    public boolean trace = false;

    /**
     * Quite mode option
     */
    @Option(name = { "--quiet" }, description = "Enables quiet mode, in this mode general progress information is not printed to standard out.")
    public boolean quiet = false;

    /**
     * Setup mix option
     */
    @Option(name = { "--setup" }, arity = 1, title = "Setup Mix", description = "Sets a mix file containing a mix that will be used as a setup mix i.e. it will run the operations specified in it once in the exact order given before actual testing starts.")
    public String setupMixFile;

    /**
     * Tear down mix option
     */
    @Option(name = { "--teardown" }, arity = 1, title = "Teardown Mix", description = "Sets a mix fix containing a mix that will be used as a tear down mix i.e. it will run the operations specified in it once in the exact order given after actual testing finished.")
    public String teardownMixFile;
    /**
     * Limit option
     */
    @Option(name = { "-l", "--limit" }, arity = 1, title = "Limit", description = "Sets a limit that will be added to queries without a LIMIT clause, those with a LIMIT clause will use the lesser of their declared limit and this limit.  Values <= 0 are interpreted as imposing no limit on queries")
    public long limit = Options.DEFAULT_LIMIT;
    /**
     * No count option
     */
    @Option(name = { "--nocount", "--no-count" }, description = "Disables result counting for SELECT queries, allows measuring just the time to respond to queries rather than the time to complete the entire query which may be useful when benchmarking against very large datasets or when the IO path between the benchmarker and the system being benchmarked is known to be a bottleneck.")
    public boolean noCount = false;

    /**
     * Method that should be implemented to run the actual command
     * 
     * @throws IOException
     */
    protected abstract void run() throws IOException;

    /**
     * Shows a usage summary for the command
     * 
     * @param cls
     *            Command class
     */
    public static void showUsage(Class<?> cls) {
        CommandMetadata metadata = SingleCommand.singleCommand(cls).getCommandMetadata();
        StringBuilder builder = new StringBuilder();
        Help.help(metadata, builder);
        System.err.print(ANSI_RESET);
        System.err.println(builder.toString());
        System.exit(1);
    }

    /**
     * Applies all the standard options provided by this abstract class
     * 
     * @param options
     *            Options to populate
     * @throws IOException
     */
    protected final <T extends Options> void applyStandardOptions(T options) throws IOException {
        // Configure logging options first because later configuration steps may
        // log interesting information
        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.INFO);
        if (!this.logToConsole) {
            Logger.getRootLogger().removeAllAppenders();
        }
        if (this.logFile != null) {
            FileAppender appender = new FileAppender(new PatternLayout(PatternLayout.TTCC_CONVERSION_PATTERN), this.logFile, true);
            Logger.getRootLogger().addAppender(appender);
        }

        // Log level
        if (this.trace) {
            Logger.getRootLogger().setLevel(Level.TRACE);
        } else if (this.debug) {
            Logger.getRootLogger().setLevel(Level.DEBUG);
        }

        if (!this.quiet) {
            System.out.println("Running in verbose mode, run with --quiet to disable");
            options.addListener(new ConsoleProgressListener());
        } else if (this.logToConsole) {
            System.out
                    .println("Running with logging to console enabled, quiet mode is enabled but will have limited effect especially if you've set --debug or --trace as well");
        }

        // Load the operation mix
        // Try to get a loader for the given mix file
        OperationMixLoader mixLoader = OperationMixLoaderRegistry.getLoader(FileUtils.getExtension(this.mixFile, true, false));
        if (mixLoader == null)
            throw new RuntimeException("No mix loader is associated with files with the extension "
                    + FileUtils.getExtension(this.mixFile, true, true));

        // Set operation mixes
        options.setOperationMix(mixLoader.load(new File(this.mixFile)));
        if (this.setupMixFile != null) {
            mixLoader = OperationMixLoaderRegistry.getLoader(FileUtils.getExtension(this.setupMixFile, true, false));
            if (mixLoader == null)
                throw new RuntimeException("No mix loader is associated with files with the extension "
                        + FileUtils.getExtension(this.setupMixFile, true, true));
            options.setSetupMix(mixLoader.load(new File(this.setupMixFile)));
        }
        if (this.teardownMixFile != null) {
            mixLoader = OperationMixLoaderRegistry.getLoader(FileUtils.getExtension(this.teardownMixFile, true, false));
            if (mixLoader == null)
                throw new RuntimeException("No mix loader is associated with files with the extension "
                        + FileUtils.getExtension(this.teardownMixFile, true, true));
            options.setTeardownMix(mixLoader.load(new File(this.teardownMixFile)));
        }

        // Endpoints
        options.setQueryEndpoint(this.queryEndpoint);
        options.setUpdateEndpoint(this.updateEndpoint);
        options.setGraphStoreEndpoint(this.gspEndpoint);

        // Results Formats
        options.setResultsAskFormat(this.askFormat);
        options.setResultsGraphFormat(this.graphFormat);
        options.setResultsSelectFormat(this.selectFormat);

        // General options
        options.setAllowCompression(this.enableCompression);
        options.setHaltAny(this.haltAny);
        options.setHaltBehaviour(HaltBehaviour.EXIT);
        options.setHaltOnError(this.haltOnError);
        options.setHaltOnTimeout(this.haltOnTimeout);
        options.setLimit(this.limit);
        options.setMaxDelay(this.maxDelay);
        options.setNoCount(this.noCount);
        options.setParallelThreads(this.parallelThreads);
        options.setRandomizeOrder(!this.noRandom);
        options.setSanityCheckLevel(this.sanityCheckLevel);
        options.setTimeout(this.timeout);

        // Mix Runner
        if (this.sampleRepeats || this.sampleSize > 0) {
            options.setMixRunner(new SamplingOperationMixRunner(this.sampleSize, this.sampleRepeats));
        }

        // Authentication
        options.setAuthenticator(AuthUtils.prepareAuthenticator(this.username, this.password, this.preemptiveAuth, formUrl,
                formUserField, formPwdField, new String[] { this.queryEndpoint, this.updateEndpoint, this.gspEndpoint }));
    }
}
