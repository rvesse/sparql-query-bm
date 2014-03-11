/*
 * Copyright 2014 YarcData LLC All Rights Reserved.
 */

package net.sf.sparql.benchmarking.operations.update.callables;

import org.apache.jena.atlas.web.HttpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.update.UpdateProcessor;
import com.hp.hpl.jena.update.UpdateRequest;

import net.sf.sparql.benchmarking.operations.AbstractOperationCallable;
import net.sf.sparql.benchmarking.options.Options;
import net.sf.sparql.benchmarking.runners.Runner;
import net.sf.sparql.benchmarking.stats.impl.UpdateRun;
import net.sf.sparql.benchmarking.util.ErrorCategories;

/**
 * Abstract callable for update operations
 * 
 * @author rvesse
 * 
 * @param <T>
 *            Options type
 */
public abstract class AbstractUpdateCallable<T extends Options> extends AbstractOperationCallable<T> {

    static final Logger logger = LoggerFactory.getLogger(AbstractUpdateCallable.class);

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

    /**
     * Gets the update request to be executed
     * 
     * @return Update request
     */
    protected abstract UpdateRequest getUpdate();

    /**
     * Provides derived implementations the option to customize the update
     * processor before actually executing the update e.g. to add custom
     * parameters
     * <p>
     * The default implementation does nothing.
     * </p>
     * 
     * @param processor
     *            Update processor
     */
    protected void customizeRequest(UpdateProcessor processor) {
        // Default implementation does nothing
    }

    /**
     * Creates an update processor for running the update
     * 
     * @return Update processor
     */
    protected abstract UpdateProcessor createUpdateProcessor();

    @Override
    public UpdateRun call() throws Exception {
        UpdateRequest update = this.getUpdate();
        logger.debug("Running update:\n" + update.toString());

        // Create a remote update processor and configure it appropriately
        UpdateProcessor processor = this.createUpdateProcessor();
        this.customizeRequest(processor);

        long startTime = System.nanoTime();
        try {
            // Execute the update
            processor.execute();
        } catch (HttpException e) {
            // Make sure to categorize HTTP errors appropriately
            return new UpdateRun(e.getMessage(), ErrorCategories.categorizeHttpError(e), System.nanoTime() - startTime);
        }

        if (this.isCancelled())
            return null;

        long endTime = System.nanoTime();
        return new UpdateRun(endTime - startTime);
    }

}