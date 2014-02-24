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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import net.sf.sparql.benchmarking.stats.OperationMixRun;
import net.sf.sparql.benchmarking.stats.OperationRun;

/**
 * Implementation of an operation mix run
 * 
 * @author rvesse
 * 
 */
public class OperationMixRunImpl implements OperationMixRun {

    protected List<OperationRun> runs;
    protected long order = 0;

    /**
     * Creates a new operation mix run which represents the results of running a
     * mix of operations
     * 
     * @param runs
     *            Operation runs which make up this mix run
     * @param runOrder
     *            Global Run Order
     */
    public OperationMixRunImpl(Collection<OperationRun> runs, long runOrder) {
        this.runs = new ArrayList<OperationRun>(runs);
        this.order = runOrder;
    }

    /**
     * Gets an iterator over the runs that make up this operation mix
     * <p>
     * The runs are in the same order as the operations are in the originating
     * mix i.e. the order does not reflect the execution order
     * </p>
     **/
    @Override
    public Iterator<OperationRun> getRuns() {
        return this.runs.iterator();
    }

    @Override
    public long getRunOrder() {
        return this.order;
    }

    @Override
    public long getTotalErrors() {
        long total = 0;
        for (OperationRun r : this.runs) {
            if (r != null && !r.wasSuccessful())
                total++;
        }
        return total;
    }

    @Override
    public long getTotalRuntime() {
        Iterator<OperationRun> rs = this.getRuns();
        long total = 0;
        while (rs.hasNext()) {
            OperationRun r = rs.next();
            if (r != null) {
                if (r.getRuntime() == Long.MAX_VALUE)
                    return Long.MAX_VALUE;
                total += r.getRuntime();
            }
        }
        return total;
    }

    @Override
    public long getTotalResponseTime() {
        Iterator<OperationRun> rs = this.getRuns();
        long total = 0;
        while (rs.hasNext()) {
            OperationRun r = rs.next();
            if (r != null) {
                if (r.getResponseTime() == Long.MAX_VALUE)
                    return Long.MAX_VALUE;
                total += r.getResponseTime();
            }
        }
        return total;
    }

    @Override
    public long getMinimumRuntime() {
        Iterator<OperationRun> rs = this.getRuns();
        long min = Long.MAX_VALUE;
        while (rs.hasNext()) {
            OperationRun r = rs.next();
            if (r != null) {
                if (r.getRuntime() < min) {
                    min = r.getRuntime();
                }
            }
        }
        return min;
    }

    @Override
    public int getMinimumRuntimeOperationID() {
        Iterator<OperationRun> rs = this.getRuns();
        long min = Long.MAX_VALUE;
        int id = 0;
        int i = -1;
        while (rs.hasNext()) {
            OperationRun r = rs.next();
            i++;
            if (r != null) {
                if (r.getRuntime() < min) {
                    id = i;
                    min = r.getRuntime();
                }
            }
        }
        return id;
    }

    @Override
    public long getMaximumRuntime() {
        Iterator<OperationRun> rs = this.getRuns();
        long max = Long.MIN_VALUE;
        while (rs.hasNext()) {
            OperationRun r = rs.next();
            if (r != null) {
                if (r.getRuntime() > max) {
                    max = r.getRuntime();
                }
            }
        }
        return max;
    }

    @Override
    public int getMaximumRuntimeOperationID() {
        Iterator<OperationRun> rs = this.getRuns();
        long max = Long.MIN_VALUE;
        int id = 0;
        int i = -1;
        while (rs.hasNext()) {
            OperationRun r = rs.next();
            i++;
            if (r != null) {
                if (r.getRuntime() > max) {
                    id = i;
                    max = r.getRuntime();
                }
            }
        }
        return id;
    }

    /**
     * Compares one run to another
     * <p>
     * Used for sorting the runs so that outliers can be trimmed
     * </p>
     */
    @Override
    public int compareTo(OperationMixRun other) {
        long runtime = this.getTotalRuntime();
        long otherRuntime = other.getTotalRuntime();
        if (runtime < otherRuntime) {
            return -1;
        } else if (runtime > otherRuntime) {
            return 1;
        } else {
            return 0;
        }
    }

}