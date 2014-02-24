/*
 * Copyright 2014 YarcData LLC All Rights Reserved.
 */

package net.sf.sparql.benchmarking.parallel;

import java.util.concurrent.Callable;

import net.sf.sparql.benchmarking.options.Options;

/**
 * Interface for parallel clients
 * 
 * @author rvesse
 * 
 * @param <T>
 *            Options type
 */
public interface ParallelClient<T extends Options> extends Callable<Object> {

    /**
     * Gets the ID of this client
     * 
     * @return ID
     */
    public abstract int getID();

    /**
     * Runs operation mixes while the Client Manager indicates there are still
     * mixes to be run
     */
    @Override
    public abstract Object call() throws Exception;

    /**
     * Gets the parallel client manager which is managing this client
     * 
     * @return Parallel client manager
     */
    ParallelClientManager<T> getManager();

}