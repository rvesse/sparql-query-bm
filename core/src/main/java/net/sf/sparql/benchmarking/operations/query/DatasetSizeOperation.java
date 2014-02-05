/*
 * Copyright 2014 YarcData LLC All Rights Reserved.
 */

package net.sf.sparql.benchmarking.operations.query;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;

import net.sf.sparql.benchmarking.operations.OperationCallable;
import net.sf.sparql.benchmarking.options.Options;
import net.sf.sparql.benchmarking.runners.Runner;
import net.sf.sparql.benchmarking.stats.QueryRun;

/**
 * An operation which calculates the size of the dataset
 * 
 * @author rvesse
 * 
 */
public class DatasetSizeOperation extends AbstractQueryOperation {

    /**
     * Gets the variable name that the count will be in
     */
    private static final String COUNT_VARIABLE = "count";

    private Query query;

    /**
     * Creates an operation with the default name
     */
    public DatasetSizeOperation() {
        this("Calculate Dataset Size");
    }

    /**
     * Creates an operation with the user supplied name
     * 
     * @param name
     *            Name
     */
    public DatasetSizeOperation(String name) {
        super(name);
        this.query = QueryFactory.create("SELECT (COUNT(*) AS ?count) WHERE { { ?s ?p ?o } UNION { GRAPH ?g { ?s ?p ?o } } }");
    }

    @Override
    public String getType() {
        return "Dataset Size";
    }

    @Override
    public Query getQuery() {
        return this.query;
    }

    @Override
    public String getQueryString() {
        return this.getQuery().toString();
    }

    @Override
    protected <T extends Options> OperationCallable<T, QueryRun> createCallable(Runner<T> runner, T options) {
        return new LongValueCallable<T>(this.getQuery(), COUNT_VARIABLE, runner, options);
    }
}
