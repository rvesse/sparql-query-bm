/** 
 * Copyright 2011-2014 Cray Inc. All Rights Reserved
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
package net.sf.sparql.query.benchmarking.operations;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;

import org.apache.commons.math.stat.descriptive.moment.GeometricMean;
import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math.stat.descriptive.moment.Variance;
import org.apache.log4j.Logger;

import net.sf.sparql.query.benchmarking.Benchmarker;
import net.sf.sparql.query.benchmarking.BenchmarkerUtils;
import net.sf.sparql.query.benchmarking.parallel.ParallelTimer;
import net.sf.sparql.query.benchmarking.stats.OperationMixRun;
import net.sf.sparql.query.benchmarking.stats.OperationMixRunImpl;
import net.sf.sparql.query.benchmarking.stats.OperationRun;

/**
 * A basic implementation of a benchmark operation mix
 * @author rvesse
 *
 */
public class BenchmarkOperationMixImpl implements BenchmarkOperationMix {

    protected static final Logger logger = Logger.getLogger(BenchmarkOperationMixImpl.class);
    
    private List<BenchmarkOperation> operations = new ArrayList<BenchmarkOperation>();
    private List<OperationMixRun> runs = new ArrayList<OperationMixRun>();
    private boolean asThread = false;
    private ParallelTimer timer = new ParallelTimer();
    private static final StandardDeviation sdev = new StandardDeviation(false);
    private static final Variance var = new Variance(false);
    private static final GeometricMean gmean = new GeometricMean();

    protected final void addOperation(BenchmarkOperation operation) {
        this.operations.add(operation);
    }

    @Override
    public Iterator<BenchmarkOperation> getOperations() {
        return this.operations.iterator();
    }

    @Override
    public Iterator<OperationMixRun> getRuns() {
        return this.runs.iterator();
    }

    @Override
    public BenchmarkOperation getOperation(int id) {
        return this.operations.get(id);
    }

    @Override
    public int size() {
        return this.operations.size();
    }

    @Override
    public void setRunAsThread(boolean asThread) {
        this.asThread = asThread;
    }

    @Override
    public OperationMixRun run(Benchmarker b) {
        OperationMixRun run = new OperationMixRunImpl(this.operations.size(), b.getGlobalOrder());
    
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
            for (int i = 0; i < this.operations.size(); i++) {
                unallocatedIds.add(i);
            }
            while (unallocatedIds.size() > 0) {
                int id = (int) (Math.random() * unallocatedIds.size());
                ids.add(unallocatedIds.get(id));
                unallocatedIds.remove(id);
            }
        } else {
            // Fixed Order
            for (int i = 0; i < this.operations.size(); i++) {
                ids.add(i);
            }
        }
        StringBuffer operationOrder = new StringBuffer();
        operationOrder.append(prefix + "Operation Order for this Run is ");
        for (int i = 0; i < ids.size(); i++) {
            operationOrder.append(ids.get(i).toString());
            if (i < ids.size() - 1)
                operationOrder.append(", ");
        }
        b.reportProgress(operationOrder.toString());
    
        // Now run each query recording its run details
        for (Integer id : ids) {
            b.reportPartialProgress(prefix + "Running Operation " + this.operations.get(id).getName() + "...");
            timer.start();
            OperationRun r = this.operations.get(id).run(b);
            timer.stop();
            if (r.wasSuccessful()) {
                b.reportProgress(prefix + "got " + r.getResultCount() + " result(s) in "
                        + BenchmarkerUtils.toSeconds(r.getRuntime()) + "s");
            } else {
                b.reportProgress(prefix + "got error after " + BenchmarkerUtils.toSeconds(r.getRuntime()) + "s: "
                        + r.getErrorMessage());
            }
            b.reportProgress(this.operations.get(id), r);
            run.setRunStats(id, r);
    
            // Apply delay between operations
            if (b.getMaxDelay() > 0) {
                try {
                    long delay = (long) (Math.random() * b.getMaxDelay());
                    b.reportProgress(prefix + "Sleeping for "
                            + BenchmarkerUtils.toSeconds((long) (delay * BenchmarkerUtils.NANOSECONDS_PER_MILLISECONDS))
                            + "s before next operation");
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    // We don't care if we get interrupted while delaying
                    // between operations
                }
            }
        }
        this.runs.add(run);
        return run;
    }

    @Override
    public void clear() {
        this.runs.clear();
        Iterator<BenchmarkOperation> qs = this.operations.iterator();
        while (qs.hasNext()) {
            BenchmarkOperation q = qs.next();
            q.clear();
        }
    }

    @Override
    public void trim(int outliers) {
        if (outliers <= 0)
            return;
    
        PriorityQueue<OperationMixRun> rs = new PriorityQueue<OperationMixRun>();
        rs.addAll(this.runs);
        // Discard Best N
        for (int i = 0; i < outliers; i++) {
            this.runs.remove(rs.remove());
        }
        // Discard Last N
        while (rs.size() > outliers) {
            rs.remove();
        }
        for (OperationMixRun r : rs) {
            this.runs.remove(r);
        }
    }

    @Override
    public long getTotalRuntime() {
        long total = 0;
        for (OperationMixRun r : this.runs) {
            if (r.getTotalRuntime() == Long.MAX_VALUE)
                return Long.MAX_VALUE;
            total += r.getTotalRuntime();
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
        for (OperationMixRun r : this.runs) {
            if (r.getTotalResponseTime() == Long.MAX_VALUE)
                return Long.MAX_VALUE;
            total += r.getTotalResponseTime();
        }
        return total;
    }

    @Override
    public long getAverageRuntime() {
        if (this.runs.size() == 0)
            return 0;
        long total = this.getTotalRuntime();
        return total / this.runs.size();
    }

    @Override
    public long getActualAverageRuntime() {
        if (this.runs.size() == 0)
            return 0;
        long total = this.getActualRuntime();
        return total / this.runs.size();
    }

    @Override
    public long getAverageResponseTime() {
        if (this.runs.size() == 0)
            return 0;
        long total = this.getTotalResponseTime();
        return total / this.runs.size();
    }

    @Override
    public double getGeometricAverageRuntime() {
        if (this.runs.size() == 0)
            return 0;
        double[] values = new double[this.runs.size()];
        int i = 0;
        for (OperationMixRun r : this.runs) {
            values[i] = (double) r.getTotalRuntime();
            i++;
        }
        return gmean.evaluate(values);
    }

    @Override
    public long getMinimumRuntime() {
        long min = Long.MAX_VALUE;
        for (OperationMixRun r : this.runs) {
            if (r.getTotalRuntime() < min) {
                min = r.getTotalRuntime();
            }
        }
        return min;
    }

    @Override
    public long getMaximumRuntime() {
        long max = Long.MIN_VALUE;
        for (OperationMixRun r : this.runs) {
            if (r.getTotalRuntime() > max) {
                max = r.getTotalRuntime();
            }
        }
        return max;
    }

    @Override
    public double getVariance() {
        double[] values = new double[this.runs.size()];
        int i = 0;
        for (OperationMixRun r : this.runs) {
            values[i] = (double) r.getTotalRuntime();
            i++;
        }
        return var.evaluate(values);
    }

    @Override
    public double getStandardDeviation() {
        double[] values = new double[this.runs.size()];
        int i = 0;
        for (OperationMixRun r : this.runs) {
            values[i] = (double) r.getTotalRuntime();
            i++;
        }
        return sdev.evaluate(values);
    }

    @Override
    public double getOperationMixesPerHour() {
        double avgRuntime = BenchmarkerUtils.toSeconds(this.getAverageRuntime());
        if (avgRuntime == 0)
            return 0;
        return BenchmarkerUtils.SECONDS_PER_HOUR / avgRuntime;
    }

    @Override
    public double getActualOperationMixesPerHour() {
        double avgRuntime = BenchmarkerUtils.toSeconds(this.getActualAverageRuntime());
        if (avgRuntime == 0)
            return 0;
        return BenchmarkerUtils.SECONDS_PER_HOUR / avgRuntime;
    }

}