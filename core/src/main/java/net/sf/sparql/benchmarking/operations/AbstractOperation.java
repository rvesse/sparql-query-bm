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

package net.sf.sparql.benchmarking.operations;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import net.sf.sparql.benchmarking.options.Options;
import net.sf.sparql.benchmarking.parallel.ParallelTimer;
import net.sf.sparql.benchmarking.runners.Runner;
import net.sf.sparql.benchmarking.stats.OperationRun;
import net.sf.sparql.benchmarking.util.ConvertUtils;
import net.sf.sparql.benchmarking.util.ErrorCategories;

import org.apache.commons.math.stat.descriptive.moment.GeometricMean;
import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math.stat.descriptive.moment.Variance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract implementation of a test operation
 * 
 * @author rvesse
 * @param <TRun>
 *            Run information type
 * 
 */
public abstract class AbstractOperation<TRun extends OperationRun> implements Operation {

    private static final Logger logger = LoggerFactory.getLogger(AbstractOperation.class);

    private String name;
    private List<OperationRun> runs = new ArrayList<OperationRun>();
    protected ParallelTimer timer = new ParallelTimer();
    private static final StandardDeviation sdev = new StandardDeviation(false);
    private static final Variance var = new Variance(false);
    private static final GeometricMean gmean = new GeometricMean();

    /**
     * Creates a new operation
     * 
     * @param name
     *            Name of the operation
     */
    public AbstractOperation(String name) {
        this.name = name;
    }

    /**
     * Creates the callable for running the operation in a background thread
     * 
     * @param runner
     *            Runner
     * @param options
     *            Options
     * @return Callable
     */
    protected abstract <T extends Options> OperationCallable<T, TRun> createCallable(Runner<T> runner, T options);

    /**
     * Creates run information for error information
     * 
     * @param message
     *            Message
     * @param category
     *            Error category
     * @param runtime
     *            Runtime
     * @return Error information
     */
    protected abstract TRun createErrorInformation(String message, int category, long runtime);

    @Override
    public final <T extends Options> OperationRun run(Runner<T> runner, T options) {
        timer.start();
        long order = options.getGlobalOrder();

        // Prepare and submit the task
        OperationCallable<T, TRun> callable = this.createCallable(runner, options);
        FutureTask<TRun> task = new FutureTask<TRun>(callable);
        options.getExecutor().submit(task);

        OperationRun r;
        long startTime = System.nanoTime();
        try {
            // Wait for the operation to complete
            if (options.getTimeout() > 0) {
                // Enforce a timeout on the operation
                r = task.get(options.getTimeout(), TimeUnit.SECONDS);
            } else {
                // No timeout on the operations
                r = task.get();
            }
        } catch (TimeoutException tEx) {
            // Handle timeout error
            logger.error("Operation Callable execeeded Timeout - " + tEx.getMessage());
            if (options.getHaltOnTimeout() || options.getHaltAny())
                runner.halt(options, tEx);
            r = this.createErrorInformation("Operation Callable execeeded Timeout - " + tEx.getMessage(),
                    ErrorCategories.TIMEOUT, System.nanoTime() - startTime);

            // If the query times out but we aren't halting cancel further
            // evaluation of the operation
            callable.cancel();
            task.cancel(true);
        } catch (InterruptedException e) {
            // Handle interrupted error
            logger.error("Operation Callable was interrupted - " + e.getMessage());
            if (options.getHaltAny())
                runner.halt(options, e);
            r = this.createErrorInformation("Operation Callable was interrupted - " + e.getMessage(), ErrorCategories.INTERRUPT,
                    System.nanoTime() - startTime);
        } catch (ExecutionException e) {
            // Handle unexpected execution error
            logger.error("Operation Callable encountered an error - " + e.getMessage());

            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            logger.error(sw.toString());

            if (options.getHaltOnError() || options.getHaltAny())
                runner.halt(options, e);
            r = this.createErrorInformation("Operation Callable encountered an error - " + e.getMessage(),
                    ErrorCategories.EXECUTION, System.nanoTime() - startTime);
        }
        timer.stop();

        // In the event that an authentication error has been reported force an
        // invalidation of the authenticator
        if (!r.wasSuccessful() && r.getErrorCategory() == ErrorCategories.AUTHENTICATION) {
            if (options.getAuthenticator() != null) {
                options.getAuthenticator().invalidate();
            }
        }

        // Return the results
        this.addRun(r);
        r.setRunOrder(order);
        return r;
    }

    /**
     * Adds a run to the operation, typically called from a derived operations
     * {@link #run(Runner, Options)}
     * 
     * @param run
     */
    protected final void addRun(OperationRun run) {
        this.runs.add(run);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Iterator<OperationRun> getRuns() {
        return this.runs.iterator();
    }

    @Override
    public long getTotalRuntime() {
        long total = 0;
        for (OperationRun r : this.runs) {
            if (r.getRuntime() == Long.MAX_VALUE)
                return Long.MAX_VALUE;
            total += r.getRuntime();
        }
        return total;
    }

    @Override
    public long getTotalErrors() {
        long total = 0;
        for (OperationRun r : this.runs) {
            if (!r.wasSuccessful())
                total++;
        }
        return total;
    }

    @Override
    public long getActualRuntime() {
        return this.timer.getActualRuntime();
    }

    @Override
    public long getTotalResponseTime() {
        long total = 0;
        for (OperationRun r : this.runs) {
            if (r.getResponseTime() == Long.MAX_VALUE)
                return Long.MAX_VALUE;
            total += r.getResponseTime();
        }
        return total;
    }

    @Override
    public long getAverageRuntime() {
        if (this.runs.size() == 0)
            return 0;
        return this.getTotalRuntime() / this.runs.size();
    }

    @Override
    public long getAverageResponseTime() {
        if (this.runs.size() == 0)
            return 0;
        return this.getTotalResponseTime() / this.runs.size();
    }

    @Override
    public double getGeometricAverageRuntime() {
        if (this.runs.size() == 0)
            return 0;
        double[] values = new double[this.runs.size()];
        int i = 0;
        for (OperationRun r : this.runs) {
            values[i] = (double) r.getRuntime();
            i++;
        }
        return gmean.evaluate(values);
    }

    @Override
    public long getActualAverageRuntime() {
        if (this.runs.size() == 0)
            return 0;
        return this.getActualRuntime() / this.runs.size();
    }

    @Override
    public long getMinimumRuntime() {
        long min = Long.MAX_VALUE;
        for (OperationRun r : this.runs) {
            if (r.getRuntime() < min) {
                min = r.getRuntime();
            }
        }
        return min;
    }

    @Override
    public long getMaximumRuntime() {
        long max = Long.MIN_VALUE;
        for (OperationRun r : this.runs) {
            if (r.getRuntime() > max) {
                max = r.getRuntime();
            }
        }
        return max;
    }

    @Override
    public double getVariance() {
        double[] values = new double[this.runs.size()];
        int i = 0;
        for (OperationRun r : this.runs) {
            values[i] = ConvertUtils.toSeconds(r.getRuntime());
            i++;
        }
        return var.evaluate(values);
    }

    @Override
    public double getStandardDeviation() {
        double[] values = new double[this.runs.size()];
        int i = 0;
        for (OperationRun r : this.runs) {
            values[i] = (double) r.getRuntime();
            i++;
        }
        return sdev.evaluate(values);
    }

    @Override
    public long getTotalResults() {
        long total = 0;
        for (OperationRun r : this.runs) {
            if (r.getResultCount() >= 0)
                total += r.getResultCount();
        }
        return total;
    }

    @Override
    public long getAverageResults() {
        long total = this.getTotalResults();
        if (total == 0 || this.runs.size() == 0)
            return 0;
        return total / this.runs.size();
    }

    @Override
    public double getOperationsPerSecond() {
        double avgRuntime = ConvertUtils.toSeconds(this.getAverageRuntime());
        if (avgRuntime == 0)
            return 0;
        return 1 / avgRuntime;
    }

    @Override
    public double getActualOperationsPerSecond() {
        double avgRuntime = ConvertUtils.toSeconds(this.getActualAverageRuntime());
        if (avgRuntime == 0)
            return 0;
        return 1 / avgRuntime;
    }

    @Override
    public double getOperationsPerHour() {
        double avgRuntime = ConvertUtils.toSeconds(this.getAverageRuntime());
        if (avgRuntime == 0)
            return 0;
        return ConvertUtils.SECONDS_PER_HOUR / avgRuntime;
    }

    @Override
    public double getActualOperationsPerHour() {
        double avgRuntime = ConvertUtils.toSeconds(this.getActualAverageRuntime());
        if (avgRuntime == 0)
            return 0;
        return ConvertUtils.SECONDS_PER_HOUR / avgRuntime;
    }

    @Override
    public void clear() {
        this.runs.clear();
    }

    @Override
    public void trim(int outliers) {
        if (outliers <= 0)
            return;

        PriorityQueue<OperationRun> rs = new PriorityQueue<OperationRun>();
        rs.addAll(this.runs);
        // Discard Best N
        for (int i = 0; i < outliers; i++) {
            this.runs.remove(rs.remove());
        }
        // Discard Last N
        while (rs.size() > outliers) {
            rs.remove();
        }
        for (OperationRun r : rs) {
            this.runs.remove(r);
        }
    }

}