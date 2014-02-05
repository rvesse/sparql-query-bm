/*
 * Copyright 2014 YarcData LLC All Rights Reserved.
 */

package net.sf.sparql.benchmarking.operations.util;

import net.sf.sparql.benchmarking.operations.AbstractOperationCallable;
import net.sf.sparql.benchmarking.options.Options;
import net.sf.sparql.benchmarking.runners.Runner;
import net.sf.sparql.benchmarking.stats.OperationRun;

/**
 * A callable that does nothing
 * <p>
 * May be used by operations that don't perform an operation during every mix
 * run.
 * </p>
 * 
 * @author rvesse
 * 
 * @param <T>
 * @param <TRun>
 */
public class NoOpCallable<T extends Options, TRun extends OperationRun> extends AbstractOperationCallable<T, TRun> {

    private TRun runInfo;

    /**
     * Creates a new callable
     * 
     * @param runner
     *            Runner
     * @param options
     *            Options
     * @param runInfo
     *            Run information to return
     */
    public NoOpCallable(Runner<T> runner, T options, TRun runInfo) {
        super(runner, options);
    }

    @Override
    public TRun call() throws Exception {
        return runInfo;
    }

}
