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

import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.restrictions.Required;
import com.github.rvesse.airline.annotations.restrictions.ranges.DoubleRange;
import com.github.rvesse.airline.parser.errors.ParseArgumentsMissingException;
import com.github.rvesse.airline.parser.errors.ParseArgumentsUnexpectedException;
import com.github.rvesse.airline.parser.errors.ParseOptionMissingException;
import com.github.rvesse.airline.parser.errors.ParseOptionMissingValueException;
import com.github.rvesse.airline.SingleCommand;

import java.io.IOException;

import org.apache.commons.lang.ArrayUtils;

import net.sf.sparql.benchmarking.options.BenchmarkOptions;
import net.sf.sparql.benchmarking.runners.AbstractRunner;
import net.sf.sparql.benchmarking.runners.BenchmarkRunner;
import net.sf.sparql.benchmarking.runners.mix.IntelligentMixRunner;
import net.sf.sparql.benchmarking.runners.mix.ordering.DefaultMixOrderProvider;
import net.sf.sparql.benchmarking.runners.mix.ordering.MixOrderProvider;
import net.sf.sparql.benchmarking.runners.mix.ordering.SamplingMixOrderProvider;

/**
 * Runs the Benchmarker from the Command Line
 * 
 * @author rvesse
 * 
 */
@Command(name = "benchmark", description = "Runs a benchmark which consists of running the configured operation mix a set number of times and calculating statistics over those runs.  Benchmarking behaviour can be configured in various ways to cope with different desired benchmark characteristics and systems to be benchmarked.")
public class BenchmarkCommand extends AbstractCommand {

    /**
     * Runs option
     */
    @Option(name = { "-r",
            "--runs" }, arity = 1, title = "Runs", description = "Sets the number of timed runs used for the benchmark statistics")
    @Required
    public int runs = BenchmarkOptions.DEFAULT_RUNS;

    /**
     * Warm ups option
     */
    @Option(name = { "-w",
            "--warmups" }, arity = 1, title = "Warmups", description = "Sets the number of warmup runs which are run to warm up the system before performing timed runs")
    public int warmups = BenchmarkOptions.DEFAULT_WARMUPS;

    /**
     * Outliers option
     */
    @Option(name = { "-o",
            "--outliers" }, arity = 1, title = "Outliers", description = "Sets the number of outliers which will be discarded from the timed runs, the set number of best and worst runs will be discarded in order to reduce skew in the calculated statistics that may be introduced by an outlying run")
    public int outliers = BenchmarkOptions.DEFAULT_OUTLIERS;

    /**
     * Allow overwrite option
     */
    @Option(name = { "--overwrite",
            "--allow-overwrite" }, description = "Enables overwriting of existing result files, off by default to make it difficult for you to accidentally overwrite the results of a previous run with those of a subsequent run")
    public boolean allowOverwrite = false;

    /**
     * CSV result file option
     */
    @Option(name = { "-c",
            "--csv" }, arity = 1, title = "CSV Filename", description = "Sets the name of the CSV results file to be used, if not set then no CSV results will be produced")
    public String csvResultsFile;

    /**
     * XML result file option
     */
    @Option(name = { "-x",
            "--xml" }, arity = 1, title = "XML Filename", description = "Sets the name of the XML results file to be used, if not set then no XML results will be produced")
    public String xmlResultsFile;

    @Option(name = {
            "--intelligent" }, description = "When set runs the mix intelligently, intelligent running will exclude operations that fail too many times or timeout during warmups from subsequent runs and tune the operation timeout based on the maximum observed runtime from the warmups.  During actual runs operations that fail too many times will be excluded from subsequent runs.  The related --intelligent-timeout and --intelligent-failures options are used to tune the parameters for this.")
    private boolean intelligent = false;

    @Option(name = {
            "--intelligent-failures" }, arity = 1, title = "MaxFailures", description = "When using the --intelligent runner sets the failure threshold that must be reached before an operation is exlcuded from subsequent runs (default 3).  A zero/negative value will disable this behaviour of the intelligent runner.")
    private int intelligentFailureThreshold = 3;

    @Option(name = {
            "--intelligent-timeout" }, arity = 1, title = "TimeoutTuningFactor", description = "When using the --intelligent runner sets the tuning factor used to determine the adjusted timeout based on the observed maximum timeout (default 2).  This takes a multiplication factor that will be applied to the observed max runtime, if there is no operation timeout set this option will have no effect.")
    @DoubleRange(min = 1.0, minInclusive = false)
    private double intelligentTimeoutTuning = 2.0;

    /**
     * Runs the command line benchmarking process
     * 
     * @param args
     *            Arguments
     */
    public static void main(String[] args) {
        int exitCode = 0;
        try {
            // Parse options
            AbstractCommand cmd = SingleCommand.singleCommand(BenchmarkCommand.class).parse(args);

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
            showUsage(BenchmarkCommand.class);
            exitCode = ExitCodes.REQUIRED_OPTION_MISSING;
        } catch (ParseOptionMissingValueException e) {
            if (!ArrayUtils.contains(args, "--help")) {
                System.err.println(ANSI_RED + e.getMessage());
                System.err.println();
            }
            showUsage(BenchmarkCommand.class);
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
        BenchmarkOptions options = new BenchmarkOptions();
        this.applyStandardOptions(options);
        this.applyBenchmarkOptions(options);

        // Run benchmark
        AbstractRunner<BenchmarkOptions> runner = new BenchmarkRunner();
        runner.run(options);

        // Benchmarks always return success even if errors occurred as part of
        // the benchmarking
        return ExitCodes.SUCCESS;
    }

    /**
     * Applies benchmarking options provided by this command
     * 
     * @param options
     *            Benchmark options to populate
     */
    protected <T extends BenchmarkOptions> void applyBenchmarkOptions(T options) {
        // Results options
        options.setAllowOverwrite(this.allowOverwrite);
        options.setCsvResultsFile(this.csvResultsFile);
        options.setXmlResultsFile(this.xmlResultsFile);

        // Benchmarking options
        options.setRuns(this.runs);
        options.setWarmups(this.warmups);
        options.setOutliers(this.outliers);

        // Use the intelligent mix runner if appropriate
        if (this.intelligent) {
            MixOrderProvider orderProvider;
            if (this.sampleRepeats || this.sampleSize > 0) {
                orderProvider = new SamplingMixOrderProvider(this.sampleSize, this.sampleRepeats);
            } else {
                orderProvider = new DefaultMixOrderProvider();
            }

            options.setMixRunner(new IntelligentMixRunner(orderProvider, this.intelligentFailureThreshold,
                    this.intelligentTimeoutTuning));
        }
    }
}