/*
 * Copyright 2014 YarcData LLC All Rights Reserved.
 */

package net.sf.sparql.query.benchmarking.stats;

/**
 * A general purpose operation run implementation
 * 
 * @author rvesse
 * 
 */
public final class OperationRunImpl extends AbstractOperationRun {

    /**
     * Creates a operation run which represents that the running of an operation
     * resulted in an error
     * 
     * @param error
     *            Error Message
     * @param runtime
     *            Runtime, this is the amount of time elapsed until the
     *            error/timeout was reached
     */
    public OperationRunImpl(String error, long runtime) {
        super(error, runtime);
    }

    /**
     * Creates an operation run which represents the results of running an
     * operation
     * 
     * @param runtime
     *            Runtime
     * @param resultCount
     *            Result Count
     */
    private OperationRunImpl(long runtime, long resultCount) {
        super(runtime, resultCount);
    }

    /**
     * Creates an operation run which represents the results of running an
     * operation
     * 
     * @param runtime
     *            Runtime
     * @param responseTime
     *            Response Time
     * @param resultCount
     *            Result Count
     */
    public OperationRunImpl(long runtime, long responseTime, long resultCount) {
        super(runtime, responseTime, resultCount);
    }
}
