/*
 * Copyright 2014 YarcData LLC All Rights Reserved.
 */

package net.sf.sparql.query.benchmarking.runners;

import net.sf.sparql.query.benchmarking.monitoring.ProgressListener;
import net.sf.sparql.query.benchmarking.operations.BenchmarkOperation;
import net.sf.sparql.query.benchmarking.options.Options;
import net.sf.sparql.query.benchmarking.stats.OperationMixRun;
import net.sf.sparql.query.benchmarking.stats.OperationRun;

/**
 * Interface for test runners
 * 
 * @author rvesse
 * 
 * @param <T>
 *            Options type
 */
public interface Runner<T extends Options> {

    /**
     * Runs with the given options
     * 
     * @param options
     */
    public abstract void run(T options);

    /**
     * Reports progress with the information from a single run of the operation
     * mix
     * 
     * @param options
     *            Options
     * @param run
     *            Operation mix run information
     */
    public abstract void reportProgress(T options, OperationMixRun run);

    /**
     * Reports progress with the information from a single run of a specific
     * operation
     * 
     * @param options
     *            Options
     * @param operation
     *            Operation
     * @param run
     *            Run information
     */
    public abstract void reportProgress(T options, BenchmarkOperation operation, OperationRun run);

    /**
     * Reports progress with an informational message
     * <p>
     * Messages passed to this function will always have a terminating newline
     * character added to them before being sent to listeners
     * </p>
     * <p>
     * You can configure what happens to the reporting messages by adding
     * {@link ProgressListener} instances with the
     * {@link Options#addListener(ProgressListener)} method
     * </p>
     * 
     * @param options
     *            Options
     * @param message
     *            Informational Message
     */
    public abstract void reportProgress(T options, String message);

    /**
     * Reports progress with an informational message
     * <p>
     * Messages passed to this function are sent to listeners as-is
     * </p>
     * 
     * @param options
     *            Options
     * @param message
     *            Informational Message
     */
    public abstract void reportPartialProgress(T options, String message);

    /**
     * Reports a newline as a progress message
     * 
     * @param options
     *            Options
     */
    public abstract void reportProgress(T options);

    /**
     * Requests that the run be halted, exact halting conditions and behaviour
     * is specified by the given options
     * 
     * @param options
     *            Options
     * @param e
     *            Exception
     */
    public abstract void halt(T options, Exception e);

    /**
     * Requests that the run be halted, exact halting conditions and behaviour
     * is specified by the given options
     * 
     * @param options
     *            Options
     * @param message
     *            Halting Message
     */
    public abstract void halt(T options, String message);
}
