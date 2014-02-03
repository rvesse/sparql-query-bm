/*
 * Copyright 2014 YarcData LLC All Rights Reserved.
 */

package net.sf.sparql.benchmarking.stats;

/**
 * Statistics for an update run
 * 
 * @author rvesse
 * 
 */
public class UpdateRun extends AbstractOperationRun {

    /**
     * Creates run information for a successful run
     * 
     * @param runtime
     *            Runtime
     */
    public UpdateRun(long runtime) {
        super(runtime, UNKNOWN);
    }

    /**
     * Create run information for a failed run
     * 
     * @param error
     *            Error message
     * @param runtime
     *            Runtime
     */
    public UpdateRun(String error, long runtime) {
        super(error, runtime);
    }
}
