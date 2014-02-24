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

package net.sf.sparql.benchmarking.parallel;

import java.util.ArrayList;
import java.util.List;

import net.sf.sparql.benchmarking.options.Options;
import net.sf.sparql.benchmarking.parallel.impl.DefaultParallelClient;
import net.sf.sparql.benchmarking.runners.Runner;

/**
 * Abstract implementation of a parallel client manager
 * 
 * @author rvesse
 * 
 * @param <T>
 *            Options type
 */
public abstract class AbstractParallelClientManager<T extends Options> implements ParallelClientManager<T> {

    private Runner<T> runner;
    private T options;
    private boolean ready = false;
    private boolean halt = false;

    /**
     * Creates a new parallel client manager
     * 
     * @param runner
     *            Runner
     * @param options
     *            Options
     */
    public AbstractParallelClientManager(Runner<T> runner, T options) {
        this.runner = runner;
        this.options = options;
    }

    @Override
    public final T getOptions() {
        return options;
    }

    @Override
    public final Runner<T> getRunner() {
        return runner;
    }

    @Override
    public final boolean isReady() {
        return this.ready;
    }

    /**
     * Sets that the client manager is ready
     */
    protected final void setReady() {
        this.ready = true;
    }

    @Override
    public final void halt() {
        this.halt = true;
    }

    /**
     * Gets whether the client should be halting
     * 
     * @return True if should be halting, false otherwise
     */
    protected final boolean shouldHalt() {
        return this.halt;
    }

    /**
     * Runs the parallel clients
     */
    @Override
    public Object call() throws Exception {
        this.getRunner().reportProgress(this.getOptions(), "Parallel Client manager starting...");
    
        // Start the required number of clients, they won't start doing any work
        // until we finish this as they
        // rely on the isReady() method to determine when to start work and it
        // will return false until
        // after this loop
        List<ParallelClientTask<T>> tasks = new ArrayList<ParallelClientTask<T>>();
        for (int i = 1; i <= this.getOptions().getParallelThreads(); i++) {
            ParallelClientTask<T> task = new ParallelClientTask<T>(this, i);
            tasks.add(task);
            this.getOptions().getExecutor().submit(task);
            this.getRunner().reportProgress(this.getOptions(), "Created Parallel Client ID " + i);
        }
        this.getRunner().reportProgress(this.getOptions(), "Parallel Client manager is starting clients...");
        this.setReady();
    
        // Now the manager should wait until all runs have at least started
        while (this.shouldRun()) {
            Thread.sleep(100);
        }
        
        // And then wait until all runs have finished
        while (true) {
            int numFinished = 0;
            for (ParallelClientTask<T> task : tasks) {
                if (task.isDone() || task.isCancelled()) numFinished++;
            }
            if (numFinished >= tasks.size()) break;
            Thread.sleep(100);
        }
    
        return null;
    }
    
    @Override
    public ParallelClient<T> createClient(int id) {
        return new DefaultParallelClient<T>(this, id);
    }
}