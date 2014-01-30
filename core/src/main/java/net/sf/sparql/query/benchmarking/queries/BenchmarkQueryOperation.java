/*
 * Copyright 2014 YarcData LLC All Rights Reserved.
 */

package net.sf.sparql.query.benchmarking.queries;

import net.sf.sparql.query.benchmarking.operations.BenchmarkOperation;

import com.hp.hpl.jena.query.Query;

public interface BenchmarkQueryOperation extends BenchmarkOperation {

    /**
     * Gets the actual Query
     * 
     * @return Query
     */
    public abstract Query getQuery();

    /**
     * Gets the Query String used to create this Query
     * 
     * @return Query as a string
     */
    public abstract String getQueryString();
}
