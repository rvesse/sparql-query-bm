/*
 * Copyright 2014 YarcData LLC All Rights Reserved.
 */

package net.sf.sparql.benchmarking.operations.query;

import net.sf.sparql.benchmarking.operations.AbstractOperation;
import net.sf.sparql.benchmarking.operations.OperationCallable;
import net.sf.sparql.benchmarking.options.Options;
import net.sf.sparql.benchmarking.runners.Runner;
import net.sf.sparql.benchmarking.stats.QueryRun;

/**
 * Abstract implementation of a query operation
 * 
 * @author rvesse
 * 
 */
public abstract class AbstractQueryOperation extends AbstractOperation<QueryRun> implements QueryOperation {

    /**
     * Creates a new operation
     * 
     * @param name
     *            Query name
     */
    public AbstractQueryOperation(String name) {
        super(name);
    }

    @Override
    public <T extends Options> boolean canRun(Runner<T> runner, T options) {
        if (options.getQueryEndpoint() == null) {
            runner.reportProgress(options, "Queries cannot run with no query endpoint specified");
            return false;
        }
        return true;
    }

    @Override
    protected <T extends Options> OperationCallable<T, QueryRun> createCallable(Runner<T> runner, T options) {
        return new QueryCallable<T>(this.getQuery(), runner, options);
    }

    @Override
    protected QueryRun createErrorInformation(String message, long runtime) {
        return new QueryRun(message, runtime);
    }

    @Override
    public String getContentString() {
        return this.getQueryString();
    }

    /**
     * Gets the string representation (i.e. the name) of the operation
     */
    @Override
    public String toString() {
        return this.getName();
    }

}