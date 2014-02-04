/*
 * Copyright 2014 YarcData LLC All Rights Reserved.
 */

package net.sf.sparql.benchmarking.operations;

import net.sf.sparql.benchmarking.options.Options;
import net.sf.sparql.benchmarking.runners.Runner;
import net.sf.sparql.benchmarking.stats.OperationRun;

/**
 * Abstract implementation of an operation callable
 * 
 * @author rvesse
 * 
 * @param <T>
 *            Options type
 * @param <TRun>
 *            Operation run information type
 */
public abstract class AbstractOperationCallable<T extends Options, TRun extends OperationRun> implements OperationCallable<T, TRun> {

    private Runner<T> runner;
    private T options;
    private boolean cancelled = false;

    /**
     * Creates a new operation callable
     * 
     * @param runner
     *            Runner
     * @param options
     *            Options
     */
    public AbstractOperationCallable(Runner<T> runner, T options) {
        this.runner = runner;
        this.options = options;
    }

    /**
     * Gets the runner
     * 
     * @return Runner
     */
    protected final Runner<T> getRunner() {
        return runner;
    }

    /**
     * Gets the options
     * 
     * @return Options
     */
    protected final T getOptions() {
        return options;
    }

    /**
     * Gets whether the callable has been asked to cancel
     * 
     * @return
     */
    protected final boolean isCancelled() {
        return cancelled;
    }

    @Override
    public final void cancel() {
        cancelled = true;
    }

}