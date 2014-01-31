/*
 * Copyright 2014 YarcData LLC All Rights Reserved.
 */ 

package net.sf.sparql.query.benchmarking.stats;

public class UpdateRun extends AbstractOperationRun {

    public UpdateRun(long runtime) {
        super(runtime, UNKNOWN);
    }
    
    public UpdateRun(String error, long runtime) {
        super(error, runtime);
    }
}
