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

package net.sf.sparql.query.benchmarking;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

import net.sf.sparql.query.benchmarking.monitoring.CsvProgressListener;
import net.sf.sparql.query.benchmarking.monitoring.ProgressListener;
import net.sf.sparql.query.benchmarking.monitoring.XmlProgressListener;
import net.sf.sparql.query.benchmarking.parallel.ParallelClientManagerTask;
import net.sf.sparql.query.benchmarking.queries.BenchmarkQuery;
import net.sf.sparql.query.benchmarking.queries.BenchmarkQueryMix;
import net.sf.sparql.query.benchmarking.queries.QueryRunner;
import net.sf.sparql.query.benchmarking.queries.QueryTask;
import net.sf.sparql.query.benchmarking.stats.QueryMixRun;
import net.sf.sparql.query.benchmarking.stats.QueryRun;

import org.apache.log4j.Logger;
import org.apache.jena.atlas.web.auth.HttpAuthenticator;
import org.apache.jena.riot.WebContent;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;

/**
 * Benchmarker class that is used to create and run a Benchmark
 * 
 * @author rvesse
 * 
 */
public class Benchmarker {

    /**
     * Default Runs
     */
    public static final int DEFAULT_RUNS = 25;
    /**
     * Default Outliers
     */
    public static final int DEFAULT_OUTLIERS = 1;
    /**
     * Default Timeout in Seconds
     */
    public static final int DEFAULT_TIMEOUT = 300;
    /**
     * Default CSV Results File
     */
    public static final String DEFAULT_CSV_RESULTS_FILE = "results.csv";
    /**
     * Default XML Results File
     */
    public static final String DEFAULT_XML_RESULTS_FILE = "results.xml";
    /**
     * Default Warmup Runs
     */
    public static final int DEFAULT_WARMUPS = 5;
    /**
     * Default Sanity Checks
     */
    public static final int DEFAULT_SANITY_CHECKS = 2;
    /**
     * Default Result Format for {@code SELECT} queries
     */
    public static final String DEFAULT_FORMAT_SELECT = WebContent.contentTypeResultsXML;
    /**
     * Default Result Format for {@code ASK} queries
     */
    public static final String DEFAULT_FORMAT_ASK = WebContent.contentTypeResultsXML;
    /**
     * Default Result Format for {@code CONSTRUCT} and {@code DESCRIBE} queries
     */
    public static final String DEFAULT_FORMAT_GRAPH = WebContent.contentTypeRDFXML;
    /**
     * Default Max Delay between Queries in milliseconds
     */
    public static final int DEFAULT_MAX_DELAY = 1000;
    /**
     * Default Halting Behaviour
     */
    public static final HaltBehaviour DEFAULT_HALT_BEHAVIOUR = HaltBehaviour.THROW_EXCEPTION;
    /**
     * Default Parallel Threads for query run evaluation
     */
    public static final int DEFAULT_PARALLEL_THREADS = 1;
    /**
     * Default Limit, values <= 0 are considered to mean leave existing LIMIT
     * as-is and don't impose a limit on unlimited queries
     */
    public static final long DEFAULT_LIMIT = 0;

    private BenchmarkQueryMix queryMix;
    private String endpoint;
    private int runs = DEFAULT_RUNS, warmups = DEFAULT_WARMUPS;
    private String csvResultsFile = DEFAULT_CSV_RESULTS_FILE;
    private String xmlResultsFile = DEFAULT_XML_RESULTS_FILE;
    private int outliers = DEFAULT_OUTLIERS;
    private int timeout = DEFAULT_TIMEOUT;
    private boolean randomize = true;
    private int sanity = DEFAULT_SANITY_CHECKS;
    private long limit = DEFAULT_LIMIT;
    private boolean haltOnTimeout = false, haltOnError = false, haltAny = false;
    private HaltBehaviour haltBehaviour = DEFAULT_HALT_BEHAVIOUR;
    private String selectResultsFormat = DEFAULT_FORMAT_SELECT;
    private String askResultsFormat = DEFAULT_FORMAT_SELECT;
    private String graphResultsFormat = DEFAULT_FORMAT_GRAPH;
    private int delay = DEFAULT_MAX_DELAY;
    private int parallelThreads = DEFAULT_PARALLEL_THREADS;
    private boolean noCount = false;
    private boolean allowOverwite = false;
    private boolean allowGZip = false, allowDeflate = false;
    private HttpAuthenticator authenticator;

    private List<ProgressListener> listeners = new ArrayList<ProgressListener>();
    private ProgressListener csvListener = new CsvProgressListener();
    private ProgressListener xmlListener = new XmlProgressListener();

    private static final Logger logger = Logger.getLogger(Benchmarker.class);

    private ExecutorService executor = Executors.newCachedThreadPool();

    private AtomicLong globalOrder = new AtomicLong(0);

    private boolean halted = false;

    /**
     * Creates a new Benchmarker
     * <p>
     * A Benchmarker will always at least generate CSV and XML output unless you
     * disable this by removing the {@link CsvProgressListener} and
     * {@link XmlProgressListener} using the
     * {@link #removeListener(ProgressListener)} method
     * </p>
     */
    public Benchmarker() {
        // By default a Benchmarker is always configured to generate CSV and XML
        // output
        this.listeners.add(this.csvListener);
        this.listeners.add(this.xmlListener);
    }

    /**
     * Gets the in-use executor for running queries and query mixes in threads
     * using the Java concurrent framework
     * 
     * @return The Executor Service used to execute tasks
     */
    public ExecutorService getExecutor() {
        return executor;
    }

    /**
     * Sets the SPARQL endpoint to be used
     * 
     * @param endpoint
     *            SPARQL Endpoint URI
     */
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * Gets the SPARQL endpoint that is in use
     * 
     * @return SPARQL Endpoint URI
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the Query Mix to use
     * 
     * @param queries
     *            Query Mix
     */
    public void setQueryMix(BenchmarkQueryMix queries) {
        queryMix = queries;
    }

    /**
     * Gets the Query Mix that is used
     * 
     * @return Query Mix
     */
    public BenchmarkQueryMix getQueryMix() {
        return queryMix;
    }

    /**
     * Sets the number of times the Query Mix will be run
     * 
     * @param runs
     *            Number of Runs
     */
    public void setRuns(int runs) {
        if (runs < 0)
            runs = 1;
        this.runs = runs;
    }

    /**
     * Gets the number of times the Query Mix will be run
     * 
     * @return Number of Runs
     */
    public int getRuns() {
        return runs;
    }

    /**
     * Sets the number of times the Query Mix will be run as a warm up prior to
     * actual runs
     * 
     * @param runs
     *            Number of Warmup Runs
     */
    public void setWarmups(int runs) {
        if (runs <= 0)
            runs = 0;
        warmups = runs;
    }

    /**
     * Gets the number of times the Query Mix will be run as a warm up prior to
     * actual runs
     * 
     * @return Number of Warmup Runs
     */
    public int getWarmups() {
        return warmups;
    }

    /**
     * Sets the number of outliers to be discarded
     * 
     * @param outliers
     *            Number of outliers
     */
    public void setOutliers(int outliers) {
        if (outliers < 0)
            outliers = 0;
        if (outliers > runs / 2)
            throw new IllegalArgumentException("Cannot set outliers to be more than half the number of runs");
        this.outliers = outliers;
    }

    /**
     * Gets the number of outliers to be discarded
     * 
     * @return Number of outliers
     */
    public int getOutliers() {
        return outliers;
    }

    /**
     * Sets the timeout for queries
     * 
     * @param timeout
     *            Timeout in seconds
     */
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    /**
     * Gets the timeout for queries
     * 
     * @return Timeout in seconds
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * Sets the CSV Results File
     * 
     * @param file
     *            Filename for CSV Results, null disables CSV results
     */
    public void setCsvResultsFile(String file) {
        if (csvResultsFile == null && file != null) {
            // Add CsvProgressListener if not already present
            this.addListener(this.csvListener);
        }
        csvResultsFile = file;
        if (file == null) {
            // Remove CsvProgressListener if present
            this.removeListener(this.csvListener);
        }
    }

    /**
     * Gets the XML Result File
     * 
     * @return Filename for XML Results
     */
    public String getXmlResultsFile() {
        return xmlResultsFile;
    }

    /**
     * Sets the XML Results File
     * 
     * @param xmlFile
     *            Filename for XML Results, null disables XML results
     */
    public void setXmlResultsFile(String xmlFile) {
        if (xmlResultsFile == null && xmlFile != null) {
            // Add XmlProgressListener if not already present
            this.addListener(this.xmlListener);
        }
        xmlResultsFile = xmlFile;
        if (xmlFile == null) {
            // Remove XmlProgressListener if present
            this.removeListener(this.xmlListener);
        }
    }

    /**
     * Gets the CSV Result File
     * 
     * @return Filename for CSV Results
     */
    public String getCsvResultsFile() {
        return csvResultsFile;
    }

    /**
     * Sets whether Query Order should be randomized
     * 
     * @param randomize
     *            Whether query order should be random
     */
    public void setRandomizeOrder(boolean randomize) {
        this.randomize = randomize;
    }

    /**
     * Gets whether Query Order should be randomized
     * 
     * @return Whether query order is random
     */
    public boolean getRandomizeOrder() {
        return randomize;
    }

    /**
     * Sets the Sanity Checking level
     * 
     * @param level
     *            Sanity Check Level
     */
    public void setSanityCheckLevel(int level) {
        sanity = level;
    }

    /**
     * Gets the Sanity Checking Level
     * 
     * @return Sanity Check Level
     */
    public int getSanityCheckLevel() {
        return sanity;
    }

    /**
     * Sets the Halt on Timeout behavior
     * 
     * @param halt
     *            Whether a timeout should cause benchmarking to abort
     */
    public void setHaltOnTimeout(boolean halt) {
        haltOnTimeout = halt;
    }

    /**
     * Gets the Halt on Timeout behavior
     * 
     * @return Whether a timeout causes benchmarking to abort
     */
    public boolean getHaltOnTimeout() {
        return haltOnTimeout;
    }

    /**
     * Sets the Halt on Error behavior
     * 
     * @param halt
     *            Whether an error should cause benchmarking to abort
     */
    public void setHaltOnError(boolean halt) {
        haltOnError = halt;
    }

    /**
     * Gets the Halt on Error behavior
     * 
     * @return Whether an error causes benchmarking to abort
     */
    public boolean getHaltOnError() {
        return haltOnError;
    }

    /**
     * Sets Halt on Any behavior, if set to true sets Halt on Error and Halt on
     * Timeout to true as well
     * 
     * @param halt
     *            Whether any issue should cause benchmarking to abort
     */
    public void setHaltAny(boolean halt) {
        haltAny = halt;
        if (halt) {
            haltOnError = true;
            haltOnTimeout = true;
        }
    }

    /**
     * Gets Halt on Any behavior
     * 
     * @return Whether any issue causes benchmarking to abort
     */
    public boolean getHaltAny() {
        return haltAny;
    }

    /**
     * Sets the Halting Behaviour
     * 
     * @param behaviour
     *            Halting Behaviour
     */
    public void setHaltBehaviour(HaltBehaviour behaviour) {
        haltBehaviour = behaviour;
    }

    /**
     * Gets the Halting Behaviour
     * 
     * @return Halting Behaviour
     */
    public HaltBehaviour getHaltBehaviour() {
        return haltBehaviour;
    }

    /**
     * Sets the Results format to be used for ASK queries
     * 
     * @param contentType
     *            MIME Type for ASK results
     */
    public void setResultsAskFormat(String contentType) {
        askResultsFormat = contentType;
    }

    /**
     * Gets the Results format used for ASK queries
     * 
     * @return MIME Type for ASK results
     */
    public String getResultsAskFormat() {
        return askResultsFormat;
    }

    /**
     * Sets the Results format to be used for SELECT queries
     * 
     * @param contentType
     *            MIME Type for SELECT results
     */
    public void setResultsSelectFormat(String contentType) {
        selectResultsFormat = contentType;
    }

    /**
     * Gets the Results format used for SELECT queries
     * 
     * @return MIME Type for SELECT results
     */
    public String getResultsSelectFormat() {
        return selectResultsFormat;
    }

    /**
     * Sets the Results format used for CONSTRUCT/DESCRIBE queries
     * 
     * @param contentType
     *            MIME Type for CONSTRUCT/DESCRIBE results
     */
    public void setResultsGraphFormat(String contentType) {
        graphResultsFormat = contentType;
    }

    /**
     * Gets the Results format used for CONSTRUCT/DESCRIBE queries
     * 
     * @return MIME Type for CONSTRUCT/DESCRUBE results
     */
    public String getResultsGraphFormat() {
        return graphResultsFormat;
    }

    /**
     * Sets whether the client should allow the server to return GZip encoded
     * responses
     * 
     * @param allowed
     *            Whether GZip encoding is allowed
     */
    public void setAllowGZipEncoding(boolean allowed) {
        allowGZip = allowed;
    }

    /**
     * Gets whether the client will allow the server to return GZip encoded
     * responses
     * 
     * @return Whether GZip encoding is allowed
     */
    public boolean getAllowGZipEncoding() {
        return allowGZip;
    }

    /**
     * Sets whether the client will allow the server to return Deflate encoded
     * responses
     * 
     * @param allowed
     *            Whether Deflate encoding is allowed
     */
    public void setAllowDeflateEncoding(boolean allowed) {
        allowDeflate = allowed;
    }

    /**
     * Gets whether the client will allow the server to return Deflate encoded
     * responses
     * 
     * @return Whether Deflate encoding is allowed
     */
    public boolean getAllowDeflateEncoding() {
        return allowDeflate;
    }

    /**
     * <s>Gets the Username used for HTTP Basic Authentication</s>
     * 
     * @return null
     */
    @Deprecated
    public String getUsername() {
        return null;
    }

    /**
     * Sets the User name used for HTTP Basic Authentication, set to null to
     * disable authentication
     * <p>
     * Deprecated - use the new {@link #setAuthenticator(HttpAuthenticator)}
     * method
     * </p>
     * 
     * @param user
     *            User name
     * 
     */
    @Deprecated
    public void setUsername(String user) {
        // No longer functions
    }

    /**
     * <s>Gets the Password used for HTTP Basic Authentication</s>
     * <p>
     * Deprecated - non longer functional as of 1.1.0
     * </p>
     * 
     * @return null
     */
    @Deprecated
    public String getPassword() {
        return null;
    }

    /**
     * <s>Sets the Password used for HTTP Basic Authentication</s>
     * <p>
     * Deprecated - use the new {@link #setAuthenticator(HttpAuthenticator)}
     * method
     * </p>
     * 
     * @param password
     *            Password
     */
    public void setPassword(String password) {
        // No longer functions
    }

    /**
     * Sets the HTTP authenticator used
     * 
     * @param authenticator
     *            HTTP authenticator
     * @since 1.1.0
     */
    public void setAuthenticator(HttpAuthenticator authenticator) {
        this.authenticator = authenticator;
    }

    /**
     * Gets the HTTP authenticator in use
     * 
     * @return HTTP authenticator
     * @since 1.1.0
     */
    public HttpAuthenticator getAuthenticator() {
        return this.authenticator;
    }

    /**
     * Sets the maximum delay between queries
     * 
     * @param milliseconds
     *            Maximum Delay in milliseconds
     */
    public void setMaxDelay(int milliseconds) {
        if (delay < 0)
            delay = 0;
        delay = milliseconds;
    }

    /**
     * Gets the maximum delay between queries
     * 
     * @return Maximum Delay in milliseconds
     */
    public int getMaxDelay() {
        return delay;
    }

    /**
     * Sets the LIMIT to impose on queries
     * <p>
     * Values less than or equal to zero mean existing limits are left
     * unchanged, non-zero values will be imposed iff existing limit is greater
     * than the set limit
     * </p>
     * 
     * @param limit
     *            Limit to impose
     */
    public void setLimit(long limit) {
        this.limit = limit;
    }

    /**
     * Gets the LIMIT to impose on queries
     * 
     * @return Limit to impose
     */
    public long getLimit() {
        return limit;
    }

    /**
     * Sets the number of parallel threads used for query run evaluation
     * 
     * @param threads
     *            Number of Parallel Threads
     */
    public void setParallelThreads(int threads) {
        if (threads < 1)
            threads = 1;
        parallelThreads = threads;
    }

    /**
     * Sets whether query results are counted or just thrown away
     * <p>
     * Currently enabling this only applies to SELECT queries as only SELECT
     * queries stream the results currently, future versions of this tool will
     * also stream CONSTRUCT/DESCRIBE results but this is yet to be implemented
     * </p>
     * 
     * @param noCount
     *            Whether query results are counted
     */
    public void setNoCount(boolean noCount) {
        this.noCount = noCount;
    }

    /**
     * Gets whether query results are counted or just thrown away
     * 
     * @return True if results will not be counted
     */
    public boolean getNoCount() {
        return noCount;
    }

    /**
     * Gets the number of parallel threads used for query run evaluation
     * 
     * @return Number of parallel threads
     */
    public int getParallelThreads() {
        return parallelThreads;
    }

    /**
     * Sets whether {@link ProgressListener} which write to files are allowed to
     * overwrite existing files (default false)
     * 
     * @param allowOverwrite
     *            Whether overwriting existing files is allowed
     */
    public void setAllowOverwrite(boolean allowOverwrite) {
        this.allowOverwite = allowOverwrite;
    }

    /**
     * Gets whether overwriting existing files is allowed
     * 
     * @return Whether overwriting existing files is allowed
     */
    public boolean getAllowOverwrite() {
        return allowOverwite;
    }

    /**
     * Gets the Progress Listeners registered
     * 
     * @return Progress Listeners
     */
    public List<ProgressListener> getListeners() {
        return this.listeners;
    }

    /**
     * Adds a Progress Listener if it is not already registered
     * 
     * @param listener
     *            Progress Listener
     */
    public void addListener(ProgressListener listener) {
        if (!this.listeners.contains(listener)) {
            this.listeners.add(listener);
        }
    }

    /**
     * Removes a Progress Listener if it is registered
     * 
     * @param listener
     *            Progress Listener
     */
    public void removeListener(ProgressListener listener) {
        this.listeners.remove(listener);
    }

    /**
     * Runs the Benchmark
     * <p>
     * The progress of the benchmarking process can be monitored by the use of
     * {@link ProgressListener} instances registered via the
     * {@link Benchmarker#addListener(ProgressListener)} method
     * </p>
     * <p>
     * At the conclusion of the benchmarking process a CSV and an XML file will
     * be generated with full result details
     * </p>
     */
    public void runBenchmark() {
        // Inform Listeners that we are starting benchmarking
        for (ProgressListener l : this.listeners) {
            try {
                l.handleStarted(this);
            } catch (Exception e) {
                System.err.println(l.getClass().getName() + " encountered an error during handleStarted() - " + e.getMessage());
                // IMPORTANT - A startup error always halts benchmarking
                // regardless of halting options
                halt(l.getClass().getName() + " encountered an error in startup");
            }
        }

        // Validate Options
        if (endpoint == null) {
            System.err.println("SPARQL Endpoint has not been set");
            halt("No SPARQL Endpoint was set");
        }
        if (queryMix == null) {
            System.err.println("Query Mix has not been set");
            halt("No Query Mix was set");
        }
        if (outliers * 2 >= runs) {
            System.err
                    .println("Specified number of outliers would mean all run results would be discarded, please specify a lower number of outliers");
            halt("Number of Outliers too high");
        }

        // Print Options for User Reference
        reportProgress("Benchmark Options");
        reportProgress("-----------------");
        reportProgress();
        reportProgress("Endpoint = " + endpoint);
        reportProgress("Sanity Checking Level = " + sanity);
        reportProgress("Warmups = " + warmups);
        reportProgress("Runs = " + runs);
        reportProgress("Random Query Order = " + (randomize ? "On" : "Off"));
        reportProgress("Outliers = " + outliers);
        reportProgress("Timeout = " + timeout + " seconds");
        reportProgress("Max Delay between Queries = " + delay + " milliseconds");
        reportProgress("Result Limit = " + (limit <= 0 ? "Query Specified" : limit));
        reportProgress("CSV Results File = " + (csvResultsFile == null ? "disabled" : csvResultsFile));
        reportProgress("XML Results File = " + (xmlResultsFile == null ? "disabled" : xmlResultsFile));
        reportProgress("Halt on Timeout = " + haltOnTimeout);
        reportProgress("Halt on Error = " + haltOnError);
        reportProgress("Halt Any = " + haltAny);
        reportProgress("ASK Results Format = " + askResultsFormat);
        reportProgress("Graph Results Format = " + graphResultsFormat);
        reportProgress("SELECT Results Format = " + selectResultsFormat);
        reportProgress("GZip Encoding = " + (allowGZip ? "enabled" : "disabled"));
        reportProgress("Deflate Encoding = " + (allowDeflate ? "enabled" : "disabled"));
        reportProgress("Parallel Threads = " + parallelThreads);
        reportProgress("Result Counting = " + (noCount ? "disabled" : "enabled"));
        reportProgress("Authentication = " + (authenticator != null ? "enabled" : "disabled"));
        reportProgress();

        // Sanity Checking
        if (sanity > 0) {
            if (checkSanity()) {
                reportProgress("Sanity Checks passed required sanity level...");
                reportProgress();
            } else {
                reportProgress("Sanity Checks failed to meet required sanity level, please ensure that the endpoint specified is actually available and working.  If this is the case try setting -s 0 and retrying");
                System.exit(1);
            }
        } else {
            reportProgress("Sanity Check skipped by user...");
        }

        // Summarise Queries to be used
        reportProgress("Starting Benchmarking...");
        reportProgress(queryMix.size() + " Queries were loaded:");

        int i = 0;
        Iterator<BenchmarkQuery> qs = queryMix.getQueries();
        while (qs.hasNext()) {
            BenchmarkQuery q = qs.next();
            reportProgress("Query ID " + i + " (" + q.getName() + ")");
            reportProgress(q.getQuery().toString());
            reportProgress();
            i++;
        }

        // Warmups
        reportProgress("Running Warmups...");
        reportProgress();
        for (i = 0; i < warmups; i++) {
            reportProgress("Warmup Run " + (i + 1) + " of " + warmups);
            QueryMixRun r = queryMix.run(this);
            reportProgress();
            reportProgress("Total Response Time: " + BenchmarkerUtils.formatTime(r.getTotalResponseTime()));
            reportProgress("Total Runtime: " + BenchmarkerUtils.formatTime(r.getTotalRuntime()));
            int minQueryId = r.getMinimumRuntimeQueryID();
            int maxQueryId = r.getMaximumRuntimeQueryID();
            reportProgress("Minimum Query Runtime: " + BenchmarkerUtils.formatTime(r.getMinimumRuntime()) + " (Query "
                    + queryMix.getQuery(minQueryId).getName() + ")");
            reportProgress("Maximum Query Runtime: " + BenchmarkerUtils.formatTime(r.getMaximumRuntime()) + " (Query "
                    + queryMix.getQuery(maxQueryId).getName() + ")");
            reportProgress();
        }
        queryMix.clear();

        // Actual Runs
        reportProgress("Running Benchmarks...");
        reportProgress();

        // Reset Order because warm up runs/prior runs may have altered this
        globalOrder.set(0);

        if (parallelThreads == 1) {
            // Single Threaded Benchmark
            for (i = 0; i < runs; i++) {
                reportProgress("Query Mix Run " + (i + 1) + " of " + runs);
                QueryMixRun r = queryMix.run(this);
                reportProgress(r);
                reportProgress();
                reportProgress("Total Response Time: " + BenchmarkerUtils.formatTime(r.getTotalResponseTime()));
                reportProgress("Total Runtime: " + BenchmarkerUtils.formatTime(r.getTotalRuntime()));
                int minQueryId = r.getMinimumRuntimeQueryID();
                int maxQueryId = r.getMaximumRuntimeQueryID();
                reportProgress("Minimum Query Runtime: " + BenchmarkerUtils.formatTime(r.getMinimumRuntime()) + " (Query "
                        + queryMix.getQuery(minQueryId).getName() + ")");
                reportProgress("Maximum Query Runtime: " + BenchmarkerUtils.formatTime(r.getMaximumRuntime()) + " (Query "
                        + queryMix.getQuery(maxQueryId).getName() + ")");
                reportProgress();
            }
        } else {

            // Multi Threaded Benchmark
            queryMix.setRunAsThread(true);
            ParallelClientManagerTask task = new ParallelClientManagerTask(this);
            this.executor.submit(task);
            try {
                task.get();
            } catch (InterruptedException e) {
                logger.error("Multi Threaded Benchmarking was interrupted - " + e.getMessage());
                if (haltAny)
                    halt(e);
            } catch (ExecutionException e) {
                logger.error("Multi Threaded Benchmarking encountered an error - " + e.getMessage());

                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                logger.error(sw.toString());

                if (haltOnError || haltAny)
                    halt(e);
            }
        }
        reportProgress("Finished Benchmarking, calculating statistics...");
        reportProgress();

        // Query Summary
        reportProgress("Query Summary");
        reportProgress("-------------");
        reportProgress();
        qs = queryMix.getQueries();
        i = 0;
        while (qs.hasNext()) {
            BenchmarkQuery q = qs.next();
            // Trim outliers
            q.trim(outliers);

            // Print Summary
            reportProgress("Query ID " + i + " (" + q.getName() + ")");
            reportProgress("Total Response Time: " + BenchmarkerUtils.formatTime(q.getTotalResponseTime()));
            reportProgress("Average Response Time (Arithmetic): " + BenchmarkerUtils.formatTime(q.getAverageResponseTime()));
            reportProgress("Total Runtime: " + BenchmarkerUtils.formatTime(q.getTotalRuntime()));
            if (parallelThreads > 1)
                reportProgress("Actual Runtime: " + BenchmarkerUtils.formatTime(q.getActualRuntime()));
            reportProgress("Average Runtime (Arithmetic): " + BenchmarkerUtils.formatTime(q.getAverageRuntime()));
            if (parallelThreads > 1)
                reportProgress("Actual Average Runtime (Arithmetic): " + BenchmarkerUtils.formatTime(q.getActualAverageRuntime()));
            reportProgress("Average Runtime (Geometric): " + BenchmarkerUtils.formatTime(q.getGeometricAverageRuntime()));
            reportProgress("Minimum Runtime: " + BenchmarkerUtils.formatTime(q.getMinimumRuntime()));
            reportProgress("Maximum Runtime: " + BenchmarkerUtils.formatTime(q.getMaximumRuntime()));
            reportProgress("Runtime Variance: " + BenchmarkerUtils.formatTime(q.getVariance()));
            reportProgress("Runtime Standard Deviation: " + BenchmarkerUtils.formatTime(q.getStandardDeviation()));
            reportProgress("Queries per Second: " + q.getQueriesPerSecond());
            if (parallelThreads > 1)
                reportProgress("Actual Queries per Second: " + q.getActualQueriesPerSecond());
            reportProgress("Queries per Hour: " + q.getQueriesPerHour());
            if (parallelThreads > 1)
                reportProgress("Actual Queries per Hour: " + q.getActualQueriesPerHour());
            reportProgress();
            i++;
        }

        // Benchmark Summary
        queryMix.trim(outliers);
        reportProgress("Query Mix Summary");
        reportProgress("-----------------");
        reportProgress();
        reportProgress("Ran Query Mix containing " + queryMix.size() + " queries a total of " + runs + " times");
        reportProgress("Total Response Time: " + BenchmarkerUtils.formatTime(queryMix.getTotalResponseTime()));
        reportProgress("Average Response Time (Arithmetic): " + BenchmarkerUtils.formatTime(queryMix.getAverageResponseTime()));
        reportProgress("Total Runtime: " + BenchmarkerUtils.formatTime(queryMix.getTotalRuntime()));
        if (parallelThreads > 1)
            reportProgress("Actual Runtime: " + BenchmarkerUtils.formatTime(queryMix.getActualRuntime()));
        reportProgress("Average Runtime (Arithmetic): " + BenchmarkerUtils.formatTime(queryMix.getAverageRuntime()));
        if (parallelThreads > 1)
            reportProgress("Actual Average Runtime (Arithmetic): "
                    + BenchmarkerUtils.formatTime(queryMix.getActualAverageRuntime()));
        reportProgress("Average Runtime (Geometric): " + queryMix.getGeometricAverageRuntime() + "s");
        reportProgress("Minimum Mix Runtime: " + BenchmarkerUtils.formatTime(queryMix.getMinimumRuntime()));
        reportProgress("Maximum Mix Runtime: " + BenchmarkerUtils.formatTime(queryMix.getMaximumRuntime()));
        reportProgress("Mix Runtime Variance: " + BenchmarkerUtils.formatTime(queryMix.getVariance()));
        reportProgress("Mix Runtime Standard Deviation: " + BenchmarkerUtils.formatTime(queryMix.getStandardDeviation()));
        reportProgress("Query Mixes per Hour: " + queryMix.getQueryMixesPerHour());
        if (parallelThreads > 1)
            reportProgress("Actual Query Mixes per Hour: " + queryMix.getActualQueryMixesPerHour());
        reportProgress();

        // Finally inform listeners that benchmarking finished OK
        for (ProgressListener l : this.listeners) {
            try {
                l.handleFinished(true);
            } catch (Exception e) {
                System.err.println(l.getClass().getName() + " encountered an error during handleFinish() - " + e.getMessage());
                if (haltOnError || haltAny) {
                    halt(l.getClass().getName() + " encountering an error during finish");
                }
            }
        }
    }

    /**
     * Checks that the Endpoint being benchmarked passes some basic queries and
     * is up and running
     * 
     * @return Whether the endpoint passed some basic sanity checks
     */
    public boolean checkSanity() {
        reportProgress("Sanity checking the user specified endpoint...");
        String[] checks = new String[] { "ASK WHERE { }", "SELECT * WHERE { }", "SELECT * WHERE { ?s a ?type } LIMIT 1" };

        int passed = 0;
        for (int i = 0; i < checks.length; i++) {
            Query q = QueryFactory.create(checks[i]);
            QueryTask task = new QueryTask(new QueryRunner(q, this));
            reportPartialProgress("Sanity Check " + (i + 1) + " of " + checks.length + "...");
            try {
                executor.submit(task);
                task.get(timeout, TimeUnit.SECONDS);
                reportProgress("OK");
                passed++;
            } catch (TimeoutException tEx) {
                logger.error("Query Runner execeeded Timeout - " + tEx.getMessage());
                reportProgress("Failed");
            } catch (InterruptedException e) {
                logger.error("Query Runner was interrupted - " + e.getMessage());
                reportProgress("Failed");
            } catch (ExecutionException e) {
                logger.error("Query Runner encountered an error - " + e.getMessage());
                reportProgress("Failed");
            }
        }

        return (passed >= sanity);
    }

    /**
     * Gets the Global Run Order
     * <p>
     * Called elsewhere so that mix runs and query runs record what order they
     * were run in
     * </p>
     * 
     * @return Global Run Order
     */
    public long getGlobalOrder() {
        return globalOrder.incrementAndGet();
    }

    /**
     * Causes benchmarking to be halted, exact halting conditions and behaviour
     * is configurable
     * 
     * @param message
     *            Halting Message
     */
    public void halt(String message) {
        System.err.println("Benchmarking Aborted - Halting due to " + message);
        if (!halted) {
            // make sure we only reallyHalt once, otherwise, we infinite loop
            // with
            // bad behavior from a listener.
            halted = true;
            reallyHalt(message);
        }
    }

    /**
     * Need a helper method for halt to ensure that errors in the listeners
     * don't cause an infinite loop since any error in any of the
     * {@link ProgressListener} method calls
     * 
     * @param message
     *            Halting Message
     */
    private void reallyHalt(String message) {
        // Inform Listeners that Benchmarking Finished with a halt condition
        for (ProgressListener l : this.listeners) {
            try {
                l.handleFinished(false);
            } catch (Exception e) {
                System.err.println(l.getClass().getName() + " encountered an error during handleFinish() - " + e.getMessage());
                if (haltOnError || haltAny) {
                    halt(l.getClass().getName() + " encountering an error during finish");
                }
            }
        }

        // Then perform actual halting depending on configured behaviour
        switch (haltBehaviour) {
        case EXIT:
            System.exit(2);
        case THROW_EXCEPTION:
            throw new RuntimeException("Benchmarking Aborted - Halting due to " + message);
        }
    }

    /**
     * Causes benchmarking to be halted, exact halting conditions and behaviour
     * is configurable
     * 
     * @param e
     *            Exception
     */
    public void halt(Exception e) {
        halt(e.getMessage());
    }

    /**
     * Reports a newline as a progress message
     */
    public void reportProgress() {
        this.reportPartialProgress("\n");
    }

    /**
     * Reports Benchmarking progress with an informational message
     * <p>
     * Messages passed to this function are sent to listeners as-is
     * </p>
     * 
     * @param message
     *            Informational Message
     */
    public void reportPartialProgress(String message) {
        for (ProgressListener l : this.listeners) {
            try {
                l.handleProgress(message);
            } catch (Exception e) {
                System.err.println(l.getClass().getName() + " encountered an error during handleProgress() - " + e.getMessage());
                if (haltAny || haltOnError) {
                    halt(l.getClass().getName() + " encountering an error in progress reporting");
                }
            }
        }
    }

    /**
     * Reports Benchmarking Progress with an informational message
     * <p>
     * Messages passed to this function will always have a terminating newline
     * character added to them before being sent to listeners
     * </p>
     * <p>
     * You can configure what happens to the reporting messages by adding
     * {@link ProgressListener} instances with the
     * {@link #addListener(ProgressListener)} method
     * </p>
     * 
     * @param message
     *            Informational Message
     */
    public void reportProgress(String message) {
        this.reportPartialProgress(message + '\n');
    }

    /**
     * Reports Benchmarking Progress with the stats from a single run of a
     * specific query
     * 
     * @param query
     *            Query
     * @param run
     *            Run statistics
     */
    public void reportProgress(BenchmarkQuery query, QueryRun run) {
        for (ProgressListener l : this.listeners) {
            try {
                l.handleProgress(query, run);
            } catch (Exception e) {
                System.err.println(l.getClass().getName() + " encountered an error during handleProgress() - " + e.getMessage());
                if (haltAny || haltOnError) {
                    halt(l.getClass().getName() + " encountering an error in progress reporting");
                }
            }
        }
    }

    /**
     * Reports Benchmarking Progress with the stats from a single run of the
     * entire query set
     * 
     * @param run
     *            Query Set Run statistics
     */
    public void reportProgress(QueryMixRun run) {
        for (ProgressListener l : this.listeners) {
            try {
                l.handleProgress(run);
            } catch (Exception e) {
                System.err.println(l.getClass().getName() + " encountered an error during handleProgress() - " + e.getMessage());
                if (haltAny || haltOnError) {
                    halt(l.getClass().getName() + " encountering an error in progress reporting");
                }
            }
        }
    }
}