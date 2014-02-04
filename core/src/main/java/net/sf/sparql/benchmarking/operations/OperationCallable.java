/*
 * Copyright 2014 YarcData LLC All Rights Reserved.
 */

package net.sf.sparql.benchmarking.operations;

import java.util.concurrent.Callable;

import net.sf.sparql.benchmarking.options.Options;
import net.sf.sparql.benchmarking.stats.OperationRun;

/**
 * Interface for operation callables
 * 
 * @author rvesse
 * 
 * @param <T>
 *            Options type
 * @param <TRun>
 *            Run information type
 */
public interface OperationCallable<T extends Options, TRun extends OperationRun> extends Callable<TRun> {

    /**
     * Cancels the runner
     */
    public abstract void cancel();

}