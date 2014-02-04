/*
 * Copyright 2014 YarcData LLC All Rights Reserved.
 */

package net.sf.sparql.benchmarking.operations.util;

import java.util.concurrent.TimeUnit;

import net.sf.sparql.benchmarking.operations.AbstractOperationCallable;
import net.sf.sparql.benchmarking.options.Options;
import net.sf.sparql.benchmarking.runners.Runner;
import net.sf.sparql.benchmarking.stats.OperationRun;
import net.sf.sparql.benchmarking.stats.OperationRunImpl;

/**
 * A callable for sleeping
 * 
 * @author rvesse
 * 
 * @param <T>
 *            Options type
 */
public class SleepCallable<T extends Options> extends AbstractOperationCallable<T, OperationRun> {

    private long sleep;

    /**
     * Creates a new sleep callable
     * 
     * @param sleep
     *            Sleep
     * @param runner
     *            Runner
     * @param options
     *            Options
     */
    public SleepCallable(long sleep, Runner<T> runner, T options) {
        super(runner, options);
        this.sleep = sleep;
    }

    @Override
    public OperationRun call() throws Exception {
        Thread.sleep(TimeUnit.SECONDS.toMillis(sleep));
        long runtime = TimeUnit.SECONDS.toNanos(sleep);
        return new OperationRunImpl(runtime, runtime, 0);
    }

}
