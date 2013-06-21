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

package net.sf.sparql.query.benchmarking.queries;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;

import net.sf.sparql.query.benchmarking.Benchmarker;
import net.sf.sparql.query.benchmarking.BenchmarkerUtils;
import net.sf.sparql.query.benchmarking.parallel.ParallelTimer;
import net.sf.sparql.query.benchmarking.stats.QueryMixRun;
import net.sf.sparql.query.benchmarking.stats.QueryRun;

import org.apache.commons.math.stat.descriptive.moment.GeometricMean;
import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math.stat.descriptive.moment.Variance;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.QueryParseException;
import com.hp.hpl.jena.util.FileUtils;

/**
 * Represents a set of queries that form a Benchmark
 * 
 * @author rvesse
 * 
 */
public class BenchmarkQueryMix {

    private static final Logger logger = Logger.getLogger(BenchmarkQueryMix.class);

    private List<BenchmarkQuery> queries = new ArrayList<BenchmarkQuery>();
    private List<QueryMixRun> runs = new ArrayList<QueryMixRun>();
    private boolean asThread = false;
    private ParallelTimer timer = new ParallelTimer();

    private static final StandardDeviation sdev = new StandardDeviation(false);
    private static final Variance var = new Variance(false);
    private static final GeometricMean gmean = new GeometricMean();

    /**
     * Creates a Query Mix from a set of Query Strings
     * 
     * @param queries
     */
    public BenchmarkQueryMix(String[] queries) {
        for (String query : queries) {
            this.queries.add(new BenchmarkQuery("", query));
        }
    }

    /**
     * Creates a Query Mix from a File
     * <p>
     * The file is assumed to contain a list of paths to actual query files
     * </p>
     * 
     * @param file
     */
    public BenchmarkQueryMix(String file) {
        try {
            // Try to read the Query List File
            File f = new File(file);
            if (!f.exists()) {
                // Try and see if this is actually a resource
                logger.info("Can't find query list file '" + file + "' on disk, seeing if it is a classpath resource...");
                URL u = this.getClass().getResource(file);
                if (u != null) {
                    f = new File(u.getFile());
                    logger.info("Located query list file '" + file + "' as a classpath resource");
                } else {
                    throw new FileNotFoundException("Can't find query list file '" + file + "' (" + f.getAbsolutePath() + ") on disk or as a classpath resource");
                }
            }
            BufferedReader reader = new BufferedReader(new FileReader(f));
            try {
                String line = reader.readLine();
                while (line != null) {
                    // For Each Query File listed try to load a Query
                    f = new File(line);
                    if (!f.isAbsolute()) {
                        File base = new File(file);
                        base = base.getParentFile();
                        f = new File(base.getPath() + File.separatorChar + line);
                        logger.info("Made relative path '" + line + "' into absolute path '" + f.getAbsolutePath() + "'");
                    }
                    if (!f.exists()) {
                        // Try and see if this is actually a resource
                        logger.info("Can't find file '" + f.getPath() + "' on disk, seeing if it is a classpath resource...");
                        URL u = this.getClass().getResource(f.getPath());
                        if (u != null) {
                            f = new File(u.getFile());
                            logger.info("Located query file '" + file + "' as a classpath resource");
                        } else {
                            throw new FileNotFoundException("Can't find query file '" + line + "' (" + f.getAbsolutePath() + ") on disk or as a classpath resource");
                        }
                    }

                    String query = FileUtils.readWholeFileAsUTF8(f.getPath());
                    try {
                        this.queries.add(new BenchmarkQuery(f.getName(), query));
                    } catch (QueryParseException e) {
                        logger.error("Error reading query file: " + e.getMessage());
                        throw new QueryParseException("Query parsing error reading query file " + f.getAbsolutePath() + "\n" + e.getMessage(), e.getCause(), e.getLine(), e.getColumn());
                    }
                    line = reader.readLine();
                }
            } finally {
                reader.close();
            }
        } catch (FileNotFoundException e) {
            logger.error("Error reading query file: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        } catch (IOException e) {
            logger.error("Error reading query file: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Creates a Query Mix from an iterator
     * 
     * @param queries
     */
    public BenchmarkQueryMix(Iterator<BenchmarkQuery> queries) {
        while (queries.hasNext()) {
            this.queries.add(queries.next());
        }
    }

    /**
     * Gets the Queries in this mix
     * 
     * @return
     */
    public Iterator<BenchmarkQuery> getQueries() {
        return this.queries.iterator();
    }

    /**
     * Gets an iterator over the query mix runs
     * 
     * @return
     */
    public Iterator<QueryMixRun> getRuns() {
        return this.runs.iterator();
    }

    /**
     * Gets the Query with the specified ID
     * 
     * @param id
     * @return
     */
    public BenchmarkQuery getQuery(int id) {
        return this.queries.get(id);
    }

    /**
     * Gets the number of queries in the query set
     * 
     * @return
     */
    public int size() {
        return this.queries.size();
    }

    /**
     * Sets whether the Query Mix is being run as a thread, if so it will prefix
     * thread identifier to its progress messages
     * 
     * @param asThread
     *            Whether the Query Mix is being run as a thread
     */
    public void setRunAsThread(boolean asThread) {
        this.asThread = asThread;
    }

    /**
     * Performs a Query Mix run recording the results as a {@link QueryMixRun}
     */
    public QueryMixRun run(Benchmarker b) {
        QueryMixRun run = new QueryMixRun(this.queries.size(), b.getGlobalOrder());

        // If running as thread then we prefix all our progress messages with a
        // Thread ID
        String prefix = this.asThread ? "[Thread " + Thread.currentThread().getId() + "] " : "";

        // Generate a random sequence of integers so we execute the queries in a
        // random order
        // each time the query set is run
        List<Integer> ids = new ArrayList<Integer>();
        if (b.getRandomizeOrder()) {
            // Randomize the Order
            List<Integer> unallocatedIds = new ArrayList<Integer>();
            for (int i = 0; i < this.queries.size(); i++) {
                unallocatedIds.add(i);
            }
            while (unallocatedIds.size() > 0) {
                int id = (int) (Math.random() * unallocatedIds.size());
                ids.add(unallocatedIds.get(id));
                unallocatedIds.remove(id);
            }
        } else {
            // Fixed Order
            for (int i = 0; i < this.queries.size(); i++) {
                ids.add(i);
            }
        }
        StringBuffer queryOrder = new StringBuffer();
        queryOrder.append(prefix + "Query Order for this Run is ");
        for (int i = 0; i < ids.size(); i++) {
            queryOrder.append(ids.get(i).toString());
            if (i < ids.size() - 1)
                queryOrder.append(", ");
        }
        b.reportProgress(queryOrder.toString());

        // Now run each query recording its run details
        for (Integer id : ids) {
            b.reportPartialProgress(prefix + "Running Query " + this.queries.get(id).getName() + "...");
            timer.start();
            QueryRun r = this.queries.get(id).run(b);
            timer.stop();
            if (r.wasSuccessful()) {
                b.reportProgress(prefix + "got " + r.getResultCount() + " result(s) in "
                        + BenchmarkerUtils.toSeconds(r.getRuntime()) + "s");
            } else {
                b.reportProgress(prefix + "got error after " + BenchmarkerUtils.toSeconds(r.getRuntime()) + "s: "
                        + r.getErrorMessage());
            }
            b.reportProgress(this.queries.get(id), r);
            run.setRunStats(id, r);

            // Apply delay between queries
            if (b.getMaxDelay() > 0) {
                try {
                    long delay = (long) (Math.random() * b.getMaxDelay());
                    b.reportProgress(prefix + "Sleeping for "
                            + BenchmarkerUtils.toSeconds((long) (delay * BenchmarkerUtils.NANOSECONDS_PER_MILLISECONDS))
                            + "s before next query");
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    // We don't care if we get interrupted while delaying
                    // between queries
                }
            }
        }
        this.runs.add(run);
        return run;
    }

    /**
     * Clears all run statistics
     */
    public void clear() {
        this.runs.clear();
        Iterator<BenchmarkQuery> qs = this.queries.iterator();
        while (qs.hasNext()) {
            BenchmarkQuery q = qs.next();
            q.clear();
        }
    }

    /**
     * Trims the worst and best N results
     * 
     * @param outliers
     */
    public void trim(int outliers) {
        if (outliers <= 0)
            return;

        PriorityQueue<QueryMixRun> rs = new PriorityQueue<QueryMixRun>();
        rs.addAll(this.runs);
        // Discard Best N
        for (int i = 0; i < outliers; i++) {
            this.runs.remove(rs.remove());
        }
        // Discard Last N
        while (rs.size() > outliers) {
            rs.remove();
        }
        for (QueryMixRun r : rs) {
            this.runs.remove(r);
        }
    }

    /**
     * Gets the total runtime over all runs
     * 
     * @return Total Runtime in nanoseconds
     */
    public long getTotalRuntime() {
        long total = 0;
        for (QueryMixRun r : this.runs) {
            if (r.getTotalRuntime() == Long.MAX_VALUE)
                return Long.MAX_VALUE;
            total += r.getTotalRuntime();
        }
        return total;
    }

    /**
     * Gets the actual runtime for the query over all runs (takes into account
     * queries that run in parallel)
     * 
     * @return Actual Runtime in nanoseconds
     */
    public long getActualRuntime() {
        return this.timer.getActualRuntime();
    }

    /**
     * Gets the total response time over all runs
     * 
     * @return Total Response Time in nanoseconds
     */
    public long getTotalResponseTime() {
        long total = 0;
        for (QueryMixRun r : this.runs) {
            if (r.getTotalResponseTime() == Long.MAX_VALUE)
                return Long.MAX_VALUE;
            total += r.getTotalResponseTime();
        }
        return total;
    }

    /**
     * Gets the average runtime over all runs
     * 
     * @return Arithmetic Average Runtime in nanoseconds
     */
    public long getAverageRuntime() {
        if (this.runs.size() == 0)
            return 0;
        long total = this.getTotalRuntime();
        return total / this.runs.size();
    }

    /**
     * Gets the average actual runtime (takes into account parallization of
     * queries)
     * 
     * @return Arithmetic Actual Average Runtime in nanoseconds
     */
    public long getActualAverageRuntime() {
        if (this.runs.size() == 0)
            return 0;
        long total = this.getActualRuntime();
        return total / this.runs.size();
    }

    /**
     * Gets the average response time
     * 
     * @return Arithmetic Average Response in nanoseconds
     */
    public long getAverageResponseTime() {
        if (this.runs.size() == 0)
            return 0;
        long total = this.getTotalResponseTime();
        return total / this.runs.size();
    }

    /**
     * Gets the average runtime for the query over all runs (geometric mean)
     * 
     * @return Geometric Average Runtime in nanoseconds
     */
    public double getGeometricAverageRuntime() {
        if (this.runs.size() == 0)
            return 0;
        double[] values = new double[this.runs.size()];
        int i = 0;
        for (QueryMixRun r : this.runs) {
            values[i] = (double) r.getTotalRuntime();
            i++;
        }
        return BenchmarkQueryMix.gmean.evaluate(values);
    }

    /**
     * Gets the minimum runtime for a run
     * 
     * @return Minimum Runtime in nanoseconds
     */
    public long getMinimumRuntime() {
        long min = Long.MAX_VALUE;
        for (QueryMixRun r : this.runs) {
            if (r.getTotalRuntime() < min) {
                min = r.getTotalRuntime();
            }
        }
        return min;
    }

    /**
     * Gets the maximum runtime for a run
     * 
     * @return Maximum Runtime in nanoseconds
     */
    public long getMaximumRuntime() {
        long max = Long.MIN_VALUE;
        for (QueryMixRun r : this.runs) {
            if (r.getTotalRuntime() > max) {
                max = r.getTotalRuntime();
            }
        }
        return max;
    }

    /**
     * Gets the Variance in Mix Runtime
     * 
     * @return Runtime Variance in nanoseconds
     */
    public double getVariance() {
        double[] values = new double[this.runs.size()];
        int i = 0;
        for (QueryMixRun r : this.runs) {
            values[i] = (double) r.getTotalRuntime();
            i++;
        }
        return var.evaluate(values);
    }

    /**
     * Gets the Standard Deviation in Mix Runtime
     * 
     * @return Runtime Standard Deviation in nanoseconds
     */
    public double getStandardDeviation() {
        double[] values = new double[this.runs.size()];
        int i = 0;
        for (QueryMixRun r : this.runs) {
            values[i] = (double) r.getTotalRuntime();
            i++;
        }
        return sdev.evaluate(values);
    }

    /**
     * Calculates the number of query mixes per hour that could be executed
     * based on the average runtime of the query mix
     * 
     * @return Query Mixes per Hour
     */
    public double getQueryMixesPerHour() {
        double avgRuntime = BenchmarkerUtils.toSeconds(this.getAverageRuntime());
        if (avgRuntime == 0)
            return 0;
        return BenchmarkerUtils.SECONDS_PER_HOUR / avgRuntime;
    }

    /**
     * Calculates the number of query mixes per hour that could be executed
     * based on the {@link BenchmarkQueryMix#getActualAverageRuntime()}
     * 
     * @return Query Mixes per Hour
     */
    public double getActualQueryMixesPerHour() {
        double avgRuntime = BenchmarkerUtils.toSeconds(this.getActualAverageRuntime());
        if (avgRuntime == 0)
            return 0;
        return BenchmarkerUtils.SECONDS_PER_HOUR / avgRuntime;
    }
}
