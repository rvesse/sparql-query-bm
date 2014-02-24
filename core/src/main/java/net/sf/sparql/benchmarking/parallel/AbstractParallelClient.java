/*
 * Copyright 2014 YarcData LLC All Rights Reserved.
 */

package net.sf.sparql.benchmarking.parallel;

import net.sf.sparql.benchmarking.options.Options;

/**
 * Abstract implementation of a parallel client
 * 
 * @author rvesse
 * 
 * @param <T>
 *            Options type
 */
public abstract class AbstractParallelClient<T extends Options> implements ParallelClient<T> {

    private ParallelClientManager<T> manager;
    private int id;

    /**
     * Creates a new Parallel Client
     * 
     * @param manager
     *            Client Manager
     * @param id
     *            Client ID
     */
    public AbstractParallelClient(ParallelClientManager<T> manager, int id) {
        this.manager = manager;
        this.id = id;
    }

    @Override
    public int getID() {
        return id;
    }

    @Override
    public ParallelClientManager<T> getManager() {
        return this.manager;
    }

}