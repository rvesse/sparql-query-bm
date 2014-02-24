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

package net.sf.sparql.benchmarking.stats.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import org.apache.commons.math.stat.descriptive.moment.GeometricMean;
import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math.stat.descriptive.moment.Variance;

import net.sf.sparql.benchmarking.parallel.ParallelTimer;
import net.sf.sparql.benchmarking.stats.OperationMixRun;
import net.sf.sparql.benchmarking.stats.OperationMixStats;
import net.sf.sparql.benchmarking.stats.OperationRun;
import net.sf.sparql.benchmarking.util.ConvertUtils;

/**
 * Basic implementation of operation mix statistics
 * 
 * @author rvesse
 * 
 */
public class OperationMixStatsImpl implements OperationMixStats {

    private List<OperationMixRun> runs = new ArrayList<OperationMixRun>();
    private ParallelTimer timer = new ParallelTimer();
    private static final StandardDeviation sdev = new StandardDeviation(false);
    private static final Variance var = new Variance(false);
    private static final GeometricMean gmean = new GeometricMean();

    @Override
    public Iterator<OperationMixRun> getRuns() {
        return this.runs.iterator();
    }

    @Override
    public long getRunCount() {
        return this.runs.size();
    }

    @Override
    public void clear() {
        this.runs.clear();
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
    public long getTotalErrors() {
        long total = 0;
        for (OperationMixRun r : this.runs) {
            total += r.getTotalErrors();
        }
        return total;
    }

    @Override
    public Map<Integer, List<OperationRun>> getCategorizedErrors() {
        Map<Integer, List<OperationRun>> errors = new HashMap<Integer, List<OperationRun>>();
        for (OperationMixRun mr : this.runs) {
            if (mr.getTotalErrors() > 0) {
                Iterator<OperationRun> rs = mr.getRuns();
                while (rs.hasNext()) {
                    OperationRun r = rs.next();
                    if (r.wasSuccessful())
                        continue;

                    // Categorize error
                    if (!errors.containsKey(r.getErrorCategory())) {
                        errors.put(r.getErrorCategory(), new ArrayList<OperationRun>());
                    }
                    errors.get(r.getErrorCategory()).add(r);
                }
            }
        }
        return errors;
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
        double avgRuntime = ConvertUtils.toSeconds(this.getAverageRuntime());
        if (avgRuntime == 0)
            return 0;
        return ConvertUtils.SECONDS_PER_HOUR / avgRuntime;
    }

    @Override
    public double getActualOperationMixesPerHour() {
        double avgRuntime = ConvertUtils.toSeconds(this.getActualAverageRuntime());
        if (avgRuntime == 0)
            return 0;
        return ConvertUtils.SECONDS_PER_HOUR / avgRuntime;
    }

    @Override
    public ParallelTimer getTimer() {
        return this.timer;
    }

    @Override
    public void add(OperationMixRun run) {
        if (run == null)
            return;
        this.runs.add(run);
    }
}
