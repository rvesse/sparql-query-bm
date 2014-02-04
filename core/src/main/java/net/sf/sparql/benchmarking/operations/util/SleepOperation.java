/*
 * Copyright 2014 YarcData LLC All Rights Reserved.
 */

package net.sf.sparql.benchmarking.operations.util;

import net.sf.sparql.benchmarking.operations.AbstractOperation;
import net.sf.sparql.benchmarking.operations.OperationCallable;
import net.sf.sparql.benchmarking.options.Options;
import net.sf.sparql.benchmarking.runners.Runner;
import net.sf.sparql.benchmarking.stats.OperationRun;
import net.sf.sparql.benchmarking.stats.OperationRunImpl;

/**
 * Creates a new sleep operation
 * 
 * @author rvesse
 * 
 */
public class SleepOperation extends AbstractOperation<OperationRun> {

    private long sleep;

    /**
     * Creates a new sleep operation
     * 
     * @param sleep
     *            Sleep time in seconds
     */
    public SleepOperation(long sleep) {
        this(String.format("Sleep %d Seconds", sleep), sleep);
    }

    /**
     * Creates a new sleep operation
     * 
     * @param name
     *            Name
     * @param sleep
     *            Sleep time in seconds
     */
    public SleepOperation(String name, long sleep) {
        super(name);
        if (sleep < 0)
            throw new IllegalArgumentException("Sleep time must be >= 0");
        this.sleep = sleep;
    }

    @Override
    public <T extends Options> boolean canRun(Runner<T> runner, T options) {
        if (this.sleep > options.getTimeout()) {
            runner.reportProgress(options, "Sleep operation cannot sleep for longer than operation timeout");
            return false;
        }
        return true;
    }

    @Override
    public String getType() {
        return "Sleep";
    }

    @Override
    public String getContentString() {
        return String.format("Sleep %d Seconds", this.sleep);
    }

    @Override
    protected <T extends Options> OperationCallable<T, OperationRun> createCallable(Runner<T> runner, T options) {
        return new SleepCallable<T>(sleep, runner, options);
    }

    @Override
    protected OperationRun createErrorInformation(String message, long runtime) {
        return new OperationRunImpl(message, runtime);
    }
}
