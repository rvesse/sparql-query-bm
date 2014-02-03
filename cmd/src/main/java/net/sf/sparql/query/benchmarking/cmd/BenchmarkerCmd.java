/** 
 * Copyright 2011-2012 Cray Inc. All Rights Reserved
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name Cray Inc. nor the names of its contributors may be
 *   used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 **/

package net.sf.sparql.query.benchmarking.cmd;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import net.sf.sparql.benchmarking.BenchmarkerUtils;
import net.sf.sparql.benchmarking.HaltBehaviour;
import net.sf.sparql.benchmarking.loader.OperationMixLoader;
import net.sf.sparql.benchmarking.loader.OperationMixLoaderRegistry;
import net.sf.sparql.benchmarking.monitoring.ConsoleProgressListener;
import net.sf.sparql.benchmarking.options.BenchmarkOptions;
import net.sf.sparql.benchmarking.options.Options;
import net.sf.sparql.benchmarking.runners.BenchmarkRunner;

import org.apache.jena.atlas.web.auth.ApacheModAuthFormLogin;
import org.apache.jena.atlas.web.auth.FormLogin;
import org.apache.jena.atlas.web.auth.FormsAuthenticator;
import org.apache.jena.atlas.web.auth.PreemptiveBasicAuthenticator;
import org.apache.jena.atlas.web.auth.SimpleAuthenticator;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.QueryParseException;

/**
 * Runs the Benchmarker from the Command Line
 * <p>
 * See the README file from the source tree or the showUsage() method for full
 * usage information. Basic usage is as follows:
 * </p>
 * 
 * <pre>
 * ./benchmark endpoint queryListFile [OPTIONS]
 * </pre>
 * 
 * Or alternatively:
 * 
 * <pre>
 * java -jar query-benchmarker-0.8.0.jar endpoint queryListFile [OPTIONS]
 * </pre>
 * <p>
 * To see full options list at command line type the following:
 * </p>
 * 
 * <pre>
 * ./benchmark --help
 * </pre>
 * 
 * @author rvesse
 * 
 */
public class BenchmarkerCmd {

    // TODO Refactor to use Airline

    private static boolean enableLog4jToConsole = false, debug = false;
    private static boolean quiet = false;

    /**
     * Runs the command line benchmarking process
     * 
     * @param argv
     */
    public static void main(String[] argv) {
        if (argv.length < 2) {
            showUsage();
            System.exit(1);
        }

        // Set up the Benchmark
        BenchmarkOptions options = new BenchmarkOptions();
        parseArgs(argv, options);

        // Setup log4j to redirect stuff to stdout if enabled
        if (enableLog4jToConsole) {
            BasicConfigurator.configure();
            Logger.getRootLogger().setLevel(debug ? Level.DEBUG : Level.INFO);
        }

        options.setHaltBehaviour(HaltBehaviour.EXIT); // When run from command
                                                      // line
        // Halting Behaviour is always
        // to exit
        if (!quiet) {
            // When run from command line we automatically enable Console
            // Progress Listening unless the -q or --quiet option was specified
            System.out.println("Running in verbose mode, run with -q or --quiet to disable");
            options.addListener(new ConsoleProgressListener());
        }

        // Run the Benchmark
        BenchmarkRunner runner = new BenchmarkRunner();
        runner.run(options);

        // Finish
        System.exit(0);
    }

    /**
     * Parses Arguments and sets up the options object
     * 
     * @param argv
     *            Arguments
     * @param options
     *            Options
     */
    private static void parseArgs(String[] argv, BenchmarkOptions options) {
        if (argv.length <= 2)
            return;

        String user = null, pwd = null, formUrl = null, userField = null, pwdField = null;
        boolean preemptive = false;

        for (int i = 0; i < argv.length; i++) {
            try {
                String arg = argv[i];
                if (arg.equals("-h")) {
                    // Show Usage Summary and exit
                    showUsage();
                    System.exit(1);
                } else if (arg.equals("-q")) {
                    // Enable Quiet Mode
                    quiet = true;
                } else if (arg.startsWith("-") && arg.length() == 2) {
                    // Short Form Arguments which all expect a parameter after
                    // them
                    expectNextArg(i, argv, arg);
                    i++;
                    switch (arg.charAt(1)) {
                    case 'r':
                        options.setRuns(Integer.parseInt(argv[i]));
                        break;
                    case 'o':
                        options.setOutliers(Integer.parseInt(argv[i]));
                        break;
                    case 't':
                        options.setTimeout(Integer.parseInt(argv[i]));
                        break;
                    case 'c':
                        options.setCsvResultsFile(argv[i]);
                        break;
                    case 'w':
                        options.setWarmups(Integer.parseInt(argv[i]));
                        break;
                    case 's':
                        options.setSanityCheckLevel(Integer.parseInt(argv[i]));
                        break;
                    case 'd':
                        options.setMaxDelay(Integer.parseInt(argv[i]));
                        break;
                    case 'p':
                        options.setParallelThreads(Integer.parseInt(argv[i]));
                        break;
                    case 'x':
                        options.setXmlResultsFile(argv[i]);
                        break;
                    case 'l':
                        options.setLimit(Long.parseLong(argv[i]));
                        break;
                    case 'm':
                        parseOperationMix(options, argv[i]);
                        break;
                    case 'q':
                        options.setQueryEndpoint(argv[i]);
                        break;
                    case 'u':
                        options.setUpdateEndpoint(argv[i]);
                        break;
                    case 'g':
                        options.setGraphStoreEndpoint(argv[i]);
                        break;
                    default:
                        System.err.println("Illegal Option " + arg);
                        System.exit(1);
                    }
                } else if (arg.startsWith("--") && arg.length() > 2) {
                    // Long Form Arguments which may or may not expect a
                    // parameter after them
                    if (arg.equals("--help")) {
                        showUsage();
                        System.exit(1);
                    } else if (arg.equals("--mix")) {
                        expectNextArg(i, argv, arg);
                        i++;
                        parseOperationMix(options, argv[i]);
                    } else if (arg.equals("--query-endpoint")) {
                        expectNextArg(i, argv, arg);
                        i++;
                        options.setQueryEndpoint(argv[i]);
                    } else if (arg.equals("--update-endpoint")) {
                        expectNextArg(i, argv, arg);
                        i++;
                        options.setUpdateEndpoint(argv[i]);
                    } else if (arg.equals("--gsp-endpoint")) {
                        expectNextArg(i, argv, arg);
                        i++;
                        options.setGraphStoreEndpoint(argv[i]);
                    } else if (arg.equals("--custom-endpoint")) {
                        expectNextArg(i, argv, arg);
                        i++;
                        String name = argv[i];
                        expectNextArg(i, argv, arg);
                        i++;
                        options.setCustomEndpoint(name, argv[i]);
                    } else if (arg.equals("--runs")) {
                        expectNextArg(i, argv, arg);
                        i++;
                        options.setRuns(Integer.parseInt(argv[i]));
                    } else if (arg.equals("--outliers")) {
                        expectNextArg(i, argv, arg);
                        i++;
                        options.setOutliers(Integer.parseInt(argv[i]));
                    } else if (arg.equals("--csv")) {
                        expectNextArg(i, argv, arg);
                        i++;
                        options.setCsvResultsFile(argv[i]);
                    } else if (arg.equals("--timeout")) {
                        expectNextArg(i, argv, arg);
                        i++;
                        options.setTimeout(Integer.parseInt(argv[i]));
                    } else if (arg.equals("--warmups")) {
                        expectNextArg(i, argv, arg);
                        i++;
                        options.setWarmups(Integer.parseInt(argv[i]));
                    } else if (arg.equals("--sanity-checks")) {
                        expectNextArg(i, argv, arg);
                        i++;
                        options.setSanityCheckLevel(Integer.parseInt(argv[i]));
                    } else if (arg.equals("--delay")) {
                        expectNextArg(i, argv, arg);
                        i++;
                        options.setMaxDelay(Integer.parseInt(argv[i]));
                    } else if (arg.equals("--halt-on-timeout")) {
                        options.setHaltOnTimeout(true);
                    } else if (arg.equals("--halt-on-error")) {
                        options.setHaltOnError(true);
                    } else if (arg.equals("--halt-any")) {
                        options.setHaltAny(true);
                    } else if (arg.equals("--norand")) {
                        options.setRandomizeOrder(false);
                    } else if (arg.equals("--results-ask")) {
                        expectNextArg(i, argv, arg);
                        i++;
                        arg = argv[i];
                        options.setResultsAskFormat(arg);
                    } else if (arg.equals("--results-graph")) {
                        expectNextArg(i, argv, arg);
                        i++;
                        arg = argv[i];
                        options.setResultsGraphFormat(arg);
                    } else if (arg.equals("--results-select")) {
                        expectNextArg(i, argv, arg);
                        i++;
                        arg = argv[i];
                        options.setResultsSelectFormat(arg);
                    } else if (arg.equals("--parallel")) {
                        expectNextArg(i, argv, arg);
                        i++;
                        options.setParallelThreads(Integer.parseInt(argv[i]));
                    } else if (arg.equals("--limit")) {
                        expectNextArg(i, argv, arg);
                        i++;
                        options.setLimit(Long.parseLong(argv[i]));
                    } else if (arg.equals("--logging")) {
                        enableLog4jToConsole = true;
                    } else if (arg.equals("--debug")) {
                        debug = true;
                    } else if (arg.equals("--quiet")) {
                        quiet = true;
                    } else if (arg.equals("--xml")) {
                        expectNextArg(i, argv, arg);
                        i++;
                        options.setXmlResultsFile(argv[i]);
                    } else if (arg.equals("--noxml")) {
                        options.setXmlResultsFile(null);
                    } else if (arg.equals("--nocsv")) {
                        options.setCsvResultsFile(null);
                    } else if (arg.equals("--nocount")) {
                        options.setNoCount(true);
                    } else if (arg.equals("--overwrite")) {
                        options.setAllowOverwrite(true);
                    } else if (arg.equals("--gzip")) {
                        options.setAllowGZipEncoding(true);
                    } else if (arg.equals("--deflate")) {
                        options.setAllowDeflateEncoding(true);
                    } else if (arg.equals("--username")) {
                        expectNextArg(i, argv, arg);
                        i++;
                        user = argv[i];
                    } else if (arg.equals("--password")) {
                        expectNextArg(i, argv, arg);
                        i++;
                        pwd = argv[i];
                    } else if (arg.equals("--form-url")) {
                        expectNextArg(i, argv, arg);
                        i++;
                        formUrl = argv[i];
                    } else if (arg.equals("--form-user-field")) {
                        expectNextArg(i, argv, arg);
                        i++;
                        userField = argv[i];
                    } else if (arg.equals("--form-pwd-field")) {
                        expectNextArg(i, argv, arg);
                        i++;
                        pwdField = argv[i];
                    } else if (arg.equals("--preemptive-auth")) {
                        preemptive = true;
                    } else {
                        System.err.println("Illegal Option " + arg);
                        System.exit(1);
                    }
                } else {
                    // Other Illegal Option
                    System.err.println("Illegal Option " + arg);
                    System.exit(1);
                }
            } catch (NumberFormatException numEx) {
                // Occurs when a numeric parameter is expected but not received
                System.err.println("Illegal value '" + argv[i] + "' encountered after option " + argv[i - 1]
                        + " when an integer value was expected");
                System.exit(1);
            }
        }

        // Finally we will try and configure authentication
        if (user != null && pwd != null) {
            if (formUrl != null) {
                // Configure forms auth
                if (userField == null)
                    userField = ApacheModAuthFormLogin.USER_FIELD;
                if (pwdField == null)
                    pwdField = ApacheModAuthFormLogin.PASSWORD_FIELD;

                FormLogin login = new FormLogin(formUrl, userField, pwdField, user, pwd.toCharArray());
                try {
                    options.setAuthenticator(new FormsAuthenticator(new URI(options.getQueryEndpoint()), login));
                } catch (URISyntaxException e) {
                    System.err.println("Invalid Endpoint URL, unable to configure form based authentication: " + e.getMessage());
                    System.exit(1);
                }
            } else {
                // Use standard HTTP authentication
                options.setAuthenticator(new SimpleAuthenticator(user, pwd.toCharArray()));
                if (preemptive)
                    options.setAuthenticator(new PreemptiveBasicAuthenticator(options.getAuthenticator()));
            }
        }
    }

    /**
     * Expects a next argument, prints an error and exists if none present
     * 
     * @param i
     *            Current Argument Index
     * @param argv
     *            Arguments
     * @param arg
     *            Current Argument for which we expect a value as the next
     *            argument
     */
    private static void expectNextArg(int i, String[] argv, String arg) {
        if (i >= argv.length - 1) {
            System.err.println("Unexpected end of arguments, expected a value to be specified after the " + arg + " option");
            System.exit(1);
        }
    }

    private static void parseOperationMix(Options b, String mixFile) {
        try {
            // Try to get a loader for the given mix file
            OperationMixLoader mixLoader = OperationMixLoaderRegistry.getLoader(BenchmarkerUtils.getExtension(mixFile, true,
                    false));
            if (mixLoader == null)
                throw new RuntimeException("No mix loader is associated with files with the extension "
                        + BenchmarkerUtils.getExtension(mixFile, true, true));

            // Set operation mix
            b.setOperationMix(mixLoader.load(new File(mixFile)));
        } catch (QueryParseException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Unexpected error loading operation mix - " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Prints Usage Summary
     */
    private static void showUsage() {
        // @formatter:off        
        System.out.println("Runs a benchmark mix of queries against a SPARQL endpoint and generates performance metrics");
        System.out.println("Usage is as follows:");
        System.out.println("query-benchmarker [required options] [options]");
        System.out.println();
        System.out.println("The following options are required:");
        System.out.println();
        System.out.println(" -m mixfile");
        System.out.println(" --mix mixfile               Sets the filename of the operation mix file which contains the operations to be run");
        System.out.println();
        System.out.println("One/more of the following options are also required, exact requirements will depend on your operation mix:");
        System.out.println();
        System.out.println(" -q uri");
        System.out.println(" --query-endpoint uri        Sets the SPARQL query endpoint to use");
        System.out.println(" -u uri");
        System.out.println(" --update-endpoint uri       Sets the SPARQL update endpoint to use");
        System.out.println(" -g uri");
        System.out.println(" --gsp-endpoint uri          Sets the SPARQL graph store protocol endpoint to use");
        System.out.println(" --custom-endpoint name uri  Sets a custom endpoint to use for custom operations");
        System.out.println();
        System.out.println("The following optional options are also supported:");
        System.out.println(" -c filename.csv");
        System.out.println(" --csv filename.csv          Sets filename to which the CSV results summary will be output (default " + BenchmarkOptions.DEFAULT_CSV_RESULTS_FILE + ")");
        System.out.println(" --debug                     Sets log level to DEBUG, use in conjunction with --logging option to see detailed HTTP traces for debugging purposes");
        System.out.println(" -d N");
        System.out.println(" --delay N                   Sets maximum delay between queries in milliseconds, will be random delay up to this maximum, use 0 for no delay (default N=" + Options.DEFAULT_MAX_DELAY + ")");
        System.out.println(" --deflate                   Sets whether HTTP requests will accept Deflate encoding");
        System.out.println(" --form-url URL              Sets the login URL used for form based login");
        System.out.println(" --form-user-field FIELD     Sets the user name field used for form based login (default httpd_username)");
        System.out.println(" --form-pwd-field  FIELD     Sets the password field used for form based login (default httpd_password)");
        System.out.println(" --gzip                      Sets whether HTTP requests will accept GZip encoding");
        System.out.println(" -h");
        System.out.println(" --help                      Prints this usage message and exits");
        System.out.println(" --halt-on-timeout           Halts and aborts benchmarking if any query times out");
        System.out.println(" --halt-on-error             Halts and aborts benchmarking if any query errors");
        System.out.println(" --halt-any                  Halts and aborts benchmarking if any issue is encountered");
        System.out.println(" -l N                        Enforces a Results Limit on queries, if N>0 result limit for query is minimum of N and M where M is the existing limit for the query");
        System.out.println(" --limit N                   Enforces a Results Limit on queries, if N>0 result limit for query is minimum of N and M where M is the existing limit for the query");
        System.out.println(" --logging                   Enables redirection of log output to console, use with --debug option to get additional information");
        System.out.println(" --nocsv                     Disables CSV output, supercedes any preceding -c/--csv option but may be superceded by a subsequent -c/--csv option");
        System.out.println(" --nocount                   Disables result counting, benchmarking will only record time to receive first result from the endpoint");
        System.out.println(" --norand                    If present the order in which queries are executed will not be randomized");
        System.out.println(" --noxml                     Disables XML output, supercedes any preceding -x/--xml option but may be superceded by a subsequent -x/--xml option");
        System.out.println(" -o N");
        System.out.println(" --outliers N                Sets number of outliers to ignore i.e. discards the N best and N worst results when calculating overall averages (default N=" + BenchmarkOptions.DEFAULT_OUTLIERS + ")");
        System.out.println(" --overwrite                 Allows overwriting of existing results files of the same names, if not set and files existing benchmarking will abort immediately");
        System.out.println(" -p N");
        System.out.println(" --parallel N                Sets the number of parallel threads to use for benchmarking (default N=1 i.e. single threaded evaluation)");
        System.out.println(" --password PWD              Sets the password used for basic authentication");
        System.out.println(" --preemptive-auth           Enables use of preemptive authentication, may marginally improve performance when basic authentication is used");
        System.out.println(" -q");
        System.out.println(" --quiet                     Enables quiet mode so only errors will go to the console and no progress messages will be shown");
        System.out.println(" -r N");
        System.out.println(" --runs N                    Sets number of runs where N is an integer (default " + Options.DEFAULT_RUNS + ")");
        System.out.println(" --results-ask FMT           Sets the format to request for ASK query results (default " + Options.DEFAULT_FORMAT_SELECT + ")");
        System.out.println(" --results-graph FMT         Sets the format to request for CONSTRUCT/DESCRIBE results (default " + Options.DEFAULT_FORMAT_GRAPH + ")");
        System.out.println(" --results-select FMT        Sets the format to request for SELECT query results (default " + Options.DEFAULT_FORMAT_ASK + ")");
        System.out.println(" -s N");
        System.out.println(" --sanity-checks N           Sets what level of sanity checking used to ensure the endpoint is up and running before starting benchmarking (default N=" + BenchmarkOptions.DEFAULT_SANITY_CHECKS + ")");
        System.out.println(" -t N");
        System.out.println(" --timeout N                 Sets timeout for queries where N is number of seconds (default " + Options.DEFAULT_TIMEOUT + ")");
        System.out.println(" --username USER             Sets the username used for basic authentication");
        System.out.println(" -w N");
        System.out.println(" --warmups N                 Sets number of warm up runs to run prior to actual benchmarking runs (default " + Options.DEFAULT_WARMUPS + ")");
        System.out.println(" -x filename.xml");
        System.out.println(" --xml filename.xml          Request XML output and sets filename to which the XML results will be output");
        System.out.println();
        // @formatter:on
    }
}
