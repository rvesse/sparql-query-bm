/*
 * Copyright 2014 YarcData LLC All Rights Reserved.
 */

package net.sf.sparql.query.benchmarking.updates;

import com.hp.hpl.jena.update.UpdateRequest;

import net.sf.sparql.query.benchmarking.operations.BenchmarkOperation;

/**
 * Interface for benchmark operations that make an update
 * 
 * @author rvesse
 * 
 */
public interface BenchmarkUpdateOperation extends BenchmarkOperation {

    /**
     * Gets the actual update
     * 
     * @return Update
     */
    public abstract UpdateRequest getUpdate();

    /**
     * Gets the update string used to create this update
     * 
     * @return Raw update string
     */
    public abstract String getUpdateString();
}
