/*
 * Copyright 2014 YarcData LLC All Rights Reserved.
 */

package net.sf.sparql.benchmarking.operations.update;

import com.hp.hpl.jena.update.UpdateRequest;

import net.sf.sparql.benchmarking.operations.Operation;

/**
 * Interface for operations that make an update
 * 
 * @author rvesse
 * 
 */
public interface UpdateOperation extends Operation {

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
