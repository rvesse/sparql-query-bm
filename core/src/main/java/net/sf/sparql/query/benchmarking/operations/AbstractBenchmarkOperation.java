/*
 * Copyright 2014 YarcData LLC All Rights Reserved.
 */

package net.sf.sparql.query.benchmarking.operations;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;

import net.sf.sparql.query.benchmarking.BenchmarkerUtils;
import net.sf.sparql.query.benchmarking.parallel.ParallelTimer;
import net.sf.sparql.query.benchmarking.stats.OperationRun;

import org.apache.commons.math.stat.descriptive.moment.GeometricMean;
import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math.stat.descriptive.moment.Variance;

/**
 * Abstract implementation of a benchmark operation
 * 
 * @author rvesse
 * 
 */
public abstract class AbstractBenchmarkOperation implements BenchmarkOperation {

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
    public AbstractBenchmarkOperation(String name) {
        this.name = name;
    }

    /**
     * Adds a run to the operation, typically called from a derived
     * implementations
     * {@link #run(net.sf.sparql.query.benchmarking.Benchmarker)} method
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