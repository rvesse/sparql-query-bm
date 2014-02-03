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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;

import net.sf.sparql.benchmarking.BenchmarkerUtils;
import net.sf.sparql.benchmarking.options.Options;
import net.sf.sparql.benchmarking.parallel.ParallelTimer;
import net.sf.sparql.benchmarking.runners.Runner;
import net.sf.sparql.benchmarking.stats.OperationRun;

import org.apache.commons.math.stat.descriptive.moment.GeometricMean;
import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math.stat.descriptive.moment.Variance;

/**
 * Abstract implementation of a test operation
 * 
 * @author rvesse
 * 
 */
public abstract class AbstractOperation implements Operation {

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
            values[i] = BenchmarkerUtils.toSeconds(r.getRuntime());
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
    public double getOperationsPerSecond() {
        double avgRuntime = BenchmarkerUtils.toSeconds(this.getAverageRuntime());
        if (avgRuntime == 0)
            return 0;
        return 1 / avgRuntime;
    }

    @Override
    public double getActualOperationsPerSecond() {
        double avgRuntime = BenchmarkerUtils.toSeconds(this.getActualAverageRuntime());
        if (avgRuntime == 0)
            return 0;
        return 1 / avgRuntime;
    }

    @Override
    public double getOperationsPerHour() {
        double avgRuntime = BenchmarkerUtils.toSeconds(this.getAverageRuntime());
        if (avgRuntime == 0)
            return 0;
        return BenchmarkerUtils.SECONDS_PER_HOUR / avgRuntime;
    }

    @Override
    public double getActualOperationsPerHour() {
        double avgRuntime = BenchmarkerUtils.toSeconds(this.getActualAverageRuntime());
        if (avgRuntime == 0)
            return 0;
        return BenchmarkerUtils.SECONDS_PER_HOUR / avgRuntime;
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