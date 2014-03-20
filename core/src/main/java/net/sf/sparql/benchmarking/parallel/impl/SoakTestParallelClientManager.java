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

package net.sf.sparql.benchmarking.parallel.impl;

import net.sf.sparql.benchmarking.options.SoakOptions;
import net.sf.sparql.benchmarking.parallel.AbstractParallelClientManager;
import net.sf.sparql.benchmarking.runners.Runner;
import net.sf.sparql.benchmarking.util.ConvertUtils;

/**
 * A Callable uses to manage the running of parallel clients for multi-threaded
 * soak testing
 * 
 * @author rvesse
 * 
 */
public class SoakTestParallelClientManager extends AbstractParallelClientManager<SoakOptions> {

    private int startedRuns = 0, completedRuns = 0;
    private long startTime = System.nanoTime();

    /**
     * Creates a new Parallel Client Manager
     * 
     * @param runner
     *            Benchmark runner
     * @param options
     *            Options
     */
    public SoakTestParallelClientManager(Runner<SoakOptions> runner, SoakOptions options) {
        super(runner, options);
    }

    @Override
    public synchronized boolean shouldRun() {
        if (this.shouldHalt())
            return false;
        // Check max runtime first
        if (this.getOptions().getMaxRuntime() > 0) {
            double runtime = ConvertUtils.toMinutes(System.nanoTime() - this.startTime);
            if (runtime >= this.getOptions().getMaxRuntime())
                return false;
        }

        // Then check max runs
        if (this.getOptions().getMaxRuns() > 0) {
            if (startedRuns >= this.getOptions().getMaxRuns())
                return false;
        }

        // Otherwise good to go
        return true;
    }
    
    @Override
    public synchronized boolean startRun() {
        if (this.shouldHalt())
            return false;
        // Check max runtime first
        if (this.getOptions().getMaxRuntime() > 0) {
            double runtime = ConvertUtils.toMinutes(System.nanoTime() - this.startTime);
            if (runtime >= this.getOptions().getMaxRuntime())
                return false;
        }

        // Then check max runs
        if (this.getOptions().getMaxRuns() > 0) {
            if (startedRuns >= this.getOptions().getMaxRuns())
                return false;
        }

        // Otherwise good to go
        startedRuns++;
        return true;
    }

    @Override
    public synchronized int completeRun() {
        completedRuns++;
        int x = completedRuns;
        return x;
    }
    
    @Override
    public synchronized boolean hasFinished() {
        return !this.shouldRun() && this.completedRuns == this.startedRuns;
    }

}
