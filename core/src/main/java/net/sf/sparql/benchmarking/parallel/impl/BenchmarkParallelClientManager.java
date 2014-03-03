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

import net.sf.sparql.benchmarking.options.BenchmarkOptions;
import net.sf.sparql.benchmarking.parallel.AbstractParallelClientManager;
import net.sf.sparql.benchmarking.runners.Runner;

/**
 * A Callable uses to manage the running of parallel clients for multi-threaded
 * testing
 * 
 * @author rvesse
 * @param <T>
 *            Options type
 * 
 */
public class BenchmarkParallelClientManager<T extends BenchmarkOptions> extends AbstractParallelClientManager<T> {

    int startedRuns = 0;
    int completedRuns = 0;

    /**
     * Creates a new Parallel Client Manager
     * 
     * @param runner
     *            Benchmark runner
     * @param options
     *            Options
     */
    public BenchmarkParallelClientManager(Runner<T> runner, T options) {
        super(runner, options);
    }

    @Override
    public synchronized boolean shouldRun() {
        if (this.shouldHalt())
            return false;
        if (startedRuns < this.getOptions().getRuns()) {
            return true;
        } else {
            return false;
        }
    }
    
    @Override
    public synchronized boolean startRun() {
        if (this.shouldHalt())
            return false;
        if (startedRuns < this.getOptions().getRuns()) {
            startedRuns++;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public synchronized int completeRun() {
        completedRuns++;
        int x = completedRuns;
        return x;
    }
    
    @Override
    public synchronized boolean hasFinished() {
        return completedRuns >= this.getOptions().getRuns();
    }

}
