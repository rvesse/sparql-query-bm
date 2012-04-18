/**
 * Copyright 2012 Robert Vesse
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * you may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.sparql.query.benchmarking.cmd;

import net.sf.sparql.query.benchmarking.Benchmarker;
import net.sf.sparql.query.benchmarking.HaltBehaviour;
import net.sf.sparql.query.benchmarking.monitoring.ConsoleProgressListener;
import net.sf.sparql.query.benchmarking.queries.BenchmarkQueryMix;

import org.apache.log4j.BasicConfigurator;


/**
 * Runs the Benchmarker from the Command Line
 * <p>
 * See the README file from the source tree or the showUsage() method for full usage information.  Basic usage is as follows:
 * </p>
 * <pre>
 * ./benchmark endpoint queryListFile [OPTIONS]
 * </pre>
 * Or alternatively:
 * <pre>
 * java -jar query-benchmarker-0.8.0.jar endpoint queryListFile [OPTIONS]
 * </pre>
 * <p>
 * To see full options list at command line type the following:
 * </p>
 * <pre>
 * ./benchmark --help
 * </pre>
 * @author rvesse
 *
 */
public class BenchmarkerCmd {

	private static boolean enableLog4jToConsole = false;
	private static boolean quiet = false;
	
	/**
	 * Runs the command line benchmarking process
	 * @param argv
	 */
	public static void main(String[] argv)
	{
		if (argv.length < 2)
		{
			showUsage();
			System.exit(1);
		}
				
		//Set up the Benchmark
		Benchmarker b = new Benchmarker();
		b.setEndpoint(argv[0]);
		b.setQueryMix(new BenchmarkQueryMix(argv[1]));
		b.setHaltBehaviour(HaltBehaviour.EXIT); //When run from command line Halting Behaviour is always to exit
		parseArgs(argv, b);
		if (!quiet)
		{
			//When run from command line we automatically enable Console Progress Listening unless the -q or --quiet option was specified
			System.out.println("Running in verbose mode, run with -q or --quiet to disable");
			b.addListener(new ConsoleProgressListener()); 
		}
		
		//Setup log4j to redirect stuff to stdout if enabled
		if (enableLog4jToConsole)
		{
			BasicConfigurator.configure();
		}
		
		//Run the Benchmark
		b.runBenchmark();
		
		//Finish
		System.exit(0);
	}
	
	/**
	 * Parses Arguments and sets up the Benchmarker object
	 * @param argv Arguments
	 * @param b Benchmarker
	 */
	private static void parseArgs(String[] argv, Benchmarker b)
	{
		if (argv.length <= 2) return;
		
		for (int i = 2; i < argv.length; i++)
		{
			try
			{
				String arg = argv[i];
				if (arg.equals("-h"))
				{
					//Show Usage Summary and exit
					showUsage();
					System.exit(1);
				}
				else if (arg.equals("-q"))
				{
					//Enable Quiet Mode
					quiet = true;
				}
				else if (arg.startsWith("-") && arg.length() == 2)
				{
					//Short Form Arguments which all expect a parameter after them
					expectNextArg(i, argv, arg);
					i++;
					switch (arg.charAt(1))
					{
					case 'r':
						b.setRuns(Integer.parseInt(argv[i]));
						break;
					case 'o':
						b.setOutliers(Integer.parseInt(argv[i]));
						break;
					case 't':
						b.setTimeout(Integer.parseInt(argv[i]));
						break;
					case 'f':
						b.setCsvResultsFile(argv[i]);
						break;
					case 'w':
						b.setWarmups(Integer.parseInt(argv[i]));
						break;
					case 's':
						b.setSanityCheckLevel(Integer.parseInt(argv[i]));
						break;
					case 'd':
						b.setMaxDelay(Integer.parseInt(argv[i]));
						break;
					case 'p':
						b.setParallelThreads(Integer.parseInt(argv[i]));
						break;
					case 'x':
						b.setXmlResultsFile(argv[i]);
						break;
					case 'l':
						b.setLimit(Long.parseLong(argv[i]));
						break;
					default:
						System.err.println("Illegal Option " + arg);
						System.exit(1);
					}
				}
				else if (arg.startsWith("--") && arg.length() > 2)
				{
					//Long Form Arguments which may or may not expect a parameter after them
					if (arg.equals("--help"))
					{
						showUsage();
						System.exit(1);
					}
					else if (arg.equals("--runs"))
					{
						expectNextArg(i, argv, arg);
						i++;
						b.setRuns(Integer.parseInt(argv[i]));
					}
					else if (arg.equals("--outliers"))
					{
						expectNextArg(i, argv, arg);
						i++;
						b.setOutliers(Integer.parseInt(argv[i]));
					}
					else if (arg.equals("--file"))
					{
						expectNextArg(i, argv, arg);
						i++;
						b.setCsvResultsFile(argv[i]);
					}
					else if (arg.equals("--timeout"))
					{
						expectNextArg(i, argv, arg);
						i++;
						b.setTimeout(Integer.parseInt(argv[i]));
					}
					else if (arg.equals("--warmups"))
					{
						expectNextArg(i, argv, arg);
						i++;
						b.setWarmups(Integer.parseInt(argv[i]));
					}
					else if (arg.equals("--sanity-checks"))
					{
						expectNextArg(i, argv, arg);
						i++;
						b.setSanityCheckLevel(Integer.parseInt(argv[i]));
					}
					else if (arg.equals("--delay"))
					{
						expectNextArg(i, argv, arg);
						i++;
						b.setMaxDelay(Integer.parseInt(argv[i]));
					}
					else if (arg.equals("--halt-on-timeout"))
					{
						b.setHaltOnTimeout(true);
					}
					else if (arg.equals("--halt-on-error"))
					{
						b.setHaltOnError(true);
					}
					else if (arg.equals("--halt-any"))
					{
						b.setHaltAny(true);
					}
					else if (arg.equals("--norand"))
					{
						b.setRandomizeOrder(false);
					}
					else if (arg.equals("--results-ask"))
					{
						expectNextArg(i, argv, arg);
						i++;
						arg = argv[i];
						b.setResultsAskFormat(arg);
					}
					else if (arg.equals("--results-graph"))
					{
						expectNextArg(i, argv, arg);
						i++;
						arg = argv[i];
						b.setResultsGraphFormat(arg);
					}
					else if (arg.equals("--results-select"))
					{
						expectNextArg(i, argv, arg);
						i++;
						arg = argv[i];
						b.setResultsSelectFormat(arg);
					}
					else if (arg.equals("--parallel"))
					{
						expectNextArg(i, argv, arg);
						i++;
						b.setParallelThreads(Integer.parseInt(argv[i]));
					}
					else if (arg.equals("--limit"))
					{
						expectNextArg(i, argv, arg);
						i++;
						b.setLimit(Long.parseLong(argv[i]));
					}
					else if (arg.equals("--logging"))
					{
						enableLog4jToConsole = true;
					}
					else if (arg.equals("--quiet"))
					{
						quiet = true;
					}
					else if (arg.equals("--xml"))
					{
						expectNextArg(i, argv, arg);
						i++;
						b.setXmlResultsFile(argv[i]);
					}
					else if (arg.equals("--noxml"))
					{
						b.setXmlResultsFile(null);
					}
					else if (arg.equals("--nocsv"))
					{
						b.setCsvResultsFile(null);
					}
					else if (arg.equals("--nocount"))
					{
						b.setNoCount(true);
					}
					else if (arg.equals("--overwrite"))
					{
						b.setAllowOverwrite(true);
					}
					else if (arg.equals("--gzip"))
					{
						b.setAllowGZipEncoding(true);
					}
					else if (arg.equals("--deflate"))
					{
						b.setAllowDeflateEncoding(true);
					}
					else if (arg.equals("--username"))
					{
						expectNextArg(i, argv, arg);
						i++;
						b.setUsername(argv[i]);
					}
					else if (arg.equals("--password"))
					{
						expectNextArg(i, argv, arg);
						i++;
						b.setPassword(argv[i]);
					}
					else
					{
						System.err.println("Illegal Option " + arg);
						System.exit(1);
					}
				}
				else
				{
					//Other Illegal Option
					System.err.println("Illegal Option " + arg);
					System.exit(1);
				}
			}
			catch (NumberFormatException numEx)
			{
				//Occurs when a numeric parameter is expected but not received
				System.err.println("Illegal value '" + argv[i] + "' encountered after option " + argv[i-1] + " when an integer value was expected");
				System.exit(1);
			}
		}
	}
	
	/**
	 * Expects a next argument, prints an error and exists if none present
	 * @param i Current Argument Index
	 * @param argv Arguments
	 * @param arg Current Argument for which we expect a value as the next argument
	 */
	private static void expectNextArg(int i, String[] argv, String arg)
	{
		if (i >= argv.length - 1)
		{
			System.err.println("Unexpected end of arguments, expected a value to be specified after the " + arg + " option");
			System.exit(1);
		}
	}
		
	/**
	 * Prints Usage Summary
	 */
	private static void showUsage()
	{
		System.out.println("Runs a benchmark mix of queries against a SPARQL endpoint and generates performance metrics");
		System.out.println("Usage is as follows:");
		System.out.println("query-benchmarker endpoint queryListFile [options]");
		System.out.println();
		System.out.println("queryListFile is a file listing paths to files containing SPARQL queries to run, 1 filename per line");
		System.out.println();
		System.out.println("The following options are supported:");
		System.out.println(" -d N");
		System.out.println(" --delay N            Sets maximum delay between queries in milliseconds, will be random delay up to this maximum, use 0 for no delay (default N=" + Benchmarker.DEFAULT_MAX_DELAY + ")");
		System.out.println(" --deflate            Sets whether HTTP requests will accept Deflate encoding");
		System.out.println(" -f filename.csv");
		System.out.println(" --file filename.csv  Sets filename to which the CSV results summary will be output (default " + Benchmarker.DEFAULT_CSV_RESULTS_FILE + ")");
		System.out.println(" --gzip               Sets whether HTTP requests will accept GZip encoding");
		System.out.println(" -h");
		System.out.println(" --help               Prints this usage message and exits");
		System.out.println(" --halt-on-timeout    Halts and aborts benchmarking if any query times out");
		System.out.println(" --halt-on-error      Halts and aborts benchmarking if any query errors");
		System.out.println(" --halt-any           Halts and aborts benchmarking if any issue is encountered");
		System.out.println(" -l N                 Enforces a Results Limit on queries, if N>0 result limit for query is minimum of N and M where M is the existing limit for the query");
		System.out.println(" --limit N            Enforces a Results Limit on queries, if N>0 result limit for query is minimum of N and M where M is the existing limit for the query");
		System.out.println(" --nocsv              Disables CSV output, supercedes any preceding -f/--filename option but may be superceded by a subsequent -f/--filename option");
		System.out.println(" --nocount            Disables result counting, benchmarking will only record time to receive first result from the endpount");
		System.out.println(" --norand             If present the order in which queries are executed will not be randomized");
		System.out.println(" --noxml              Disables XML output, supercedes any preceding -x/--xml option but may be superceded by a subsequent -x/--xml option");
		System.out.println(" -o N");
		System.out.println(" --outliers N         Sets number of outliers to ignore i.e. discards the N best and N worst results when calculating overall averages (default N=" + Benchmarker.DEFAULT_OUTLIERS + ")");
		System.out.println(" --overwrite          Allows overwriting of existing results files of the same names, if not set and files existing benchmarking will abort immediately");
		System.out.println(" -p N");
		System.out.println(" --parallel N         Sets the number of parallel threads to use for benchmarking (default N=1 i.e. single threaded evaluation)");
		System.out.println(" --password PWD       Sets the password used for basic authentication");
		System.out.println(" -q");
		System.out.println(" --quiet              Enables quiet mode so only errors will go to the console and no progress messages will be shown");
		System.out.println(" -r N");
		System.out.println(" --runs N             Sets number of runs where N is an integer (default " + Benchmarker.DEFAULT_RUNS + ")");
		System.out.println(" --results-ask FMT    Sets the format to request for ASK query results (default " + Benchmarker.DEFAULT_FORMAT_SELECT + ")");
        System.out.println(" --results-graph FMT  Sets the format to request for CONSTRUCT/DESCRIBE results (default " + Benchmarker.DEFAULT_FORMAT_GRAPH + ")");
        System.out.println(" --results-select FMT Sets the format to request for SELECT query results (default " + Benchmarker.DEFAULT_FORMAT_ASK + ")");
		System.out.println(" -s N");
		System.out.println(" --sanity-checks N    Sets what level of sanity checking used to ensure the endpoint is up and running before starting benchmarking (default N=" + Benchmarker.DEFAULT_SANITY_CHECKS + ")");
		System.out.println(" -t N");
		System.out.println(" --timeout N          Sets timeout for queries where N is number of seconds (default " + Benchmarker.DEFAULT_TIMEOUT + ")");
		System.out.println(" --username USER      Sets the username used for basic authentication");
		System.out.println(" -w N");
		System.out.println(" --warmups N          Sets number of warm up runs to run prior to actual benchmarking runs (default " + Benchmarker.DEFAULT_WARMUPS + ")");
		System.out.println(" -x filename.xml");
		System.out.println(" --xml filename.xml   Request XML output and sets filename to which the XML results will be output");
		System.out.println();
	}
}
