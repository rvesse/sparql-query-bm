/*
 * Copyright 2014 YarcData LLC All Rights Reserved.
 */

package net.sf.sparql.benchmarking.parallel;

import java.util.ArrayList;
import java.util.List;

import net.sf.sparql.benchmarking.options.Options;
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

}