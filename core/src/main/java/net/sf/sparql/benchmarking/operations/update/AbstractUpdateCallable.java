/*
 * Copyright 2014 YarcData LLC All Rights Reserved.
 */

package net.sf.sparql.benchmarking.operations.update;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.sparql.modify.UpdateProcessRemoteBase;
import com.hp.hpl.jena.update.UpdateExecutionFactory;
import com.hp.hpl.jena.update.UpdateRequest;

import net.sf.sparql.benchmarking.operations.AbstractOperationCallable;
import net.sf.sparql.benchmarking.options.Options;
import net.sf.sparql.benchmarking.runners.Runner;
import net.sf.sparql.benchmarking.stats.UpdateRun;

/**
 * Abstract callable for operations that run updates
 * 
 * @author rvesse
 * 
 * @param <T>
 *            Options type
 */
public abstract class AbstractUpdateCallable<T extends Options> extends AbstractOperationCallable<T, UpdateRun> {

    private static final Logger logger = LoggerFactory.getLogger(UpdateCallable.class);

    /**
     * Creates a new callable
     * 
     * @param runner
     *            Runner
     * @param options
     *            Options
     */
    public AbstractUpdateCallable(Runner<T> runner, T options) {
        super(runner, options);
    }

    protected abstract UpdateRequest getUpdate();

    @Override
    public UpdateRun call() throws Exception {
        UpdateRequest update = this.getUpdate();
        logger.debug("Running update:\n" + update.toString());

        // Create a remote update processor and configure it appropriately
        UpdateProcessRemoteBase processor = (UpdateProcessRemoteBase) UpdateExecutionFactory.createRemote(update, this
                .getOptions().getUpdateEndpoint());
        if (this.getOptions().getAuthenticator() != null) {
            processor.setAuthenticator(this.getOptions().getAuthenticator());
        }
        long startTime = System.nanoTime();

        // Execute the update
        processor.execute();

        if (this.isCancelled())
            return null;

        long endTime = System.nanoTime();
        return new UpdateRun(endTime - startTime);
    }

}