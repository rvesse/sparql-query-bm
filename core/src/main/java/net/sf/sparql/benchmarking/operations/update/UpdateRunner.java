/*
 * Copyright 2014 YarcData LLC All Rights Reserved.
 */

package net.sf.sparql.benchmarking.operations.update;

import java.util.concurrent.Callable;

import com.hp.hpl.jena.sparql.modify.UpdateProcessRemoteBase;
import com.hp.hpl.jena.update.UpdateExecutionFactory;
import com.hp.hpl.jena.update.UpdateRequest;

import net.sf.sparql.benchmarking.options.Options;
import net.sf.sparql.benchmarking.runners.Runner;
import net.sf.sparql.benchmarking.stats.UpdateRun;

/**
 * @author rvesse
 * 
 * @param <T>
 */
public class UpdateRunner<T extends Options> implements Callable<UpdateRun> {

    private UpdateRequest update;
    @SuppressWarnings("unused")
    private Runner<T> runner;
    private T options;
    private boolean cancelled = false;

    /**
     * Creates a new update runner
     * 
     * @param update
     *            Update to run
     * @param runner
     *            Runner
     * @param options
     *            Options
     */
    public UpdateRunner(UpdateRequest update, Runner<T> runner, T options) {
        this.update = update;
        this.runner = runner;
        this.options = options;
    }

    @Override
    public UpdateRun call() throws Exception {
        // Create a remote update processor and configure it appropriately
        UpdateProcessRemoteBase processor = (UpdateProcessRemoteBase) UpdateExecutionFactory.createRemote(this.update,
                this.options.getUpdateEndpoint());
        if (this.options.getAuthenticator() != null) {
            processor.setAuthenticator(this.options.getAuthenticator());
        }
        long startTime = System.nanoTime();

        // Execute the update
        processor.execute();

        if (cancelled)
            return null;

        long endTime = System.nanoTime();
        return new UpdateRun(endTime - startTime);
    }

    /**
     * Cancels the runner
     */
    public void cancel() {
        cancelled = true;
    }

}
