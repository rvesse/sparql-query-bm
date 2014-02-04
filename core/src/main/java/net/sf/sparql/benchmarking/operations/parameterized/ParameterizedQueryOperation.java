/*
 * Copyright 2014 YarcData LLC All Rights Reserved.
 */

package net.sf.sparql.benchmarking.operations.parameterized;

import java.util.Collection;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

import net.sf.sparql.benchmarking.operations.OperationCallable;
import net.sf.sparql.benchmarking.operations.query.QueryCallable;
import net.sf.sparql.benchmarking.operations.query.QueryOperation;
import net.sf.sparql.benchmarking.options.Options;
import net.sf.sparql.benchmarking.runners.Runner;
import net.sf.sparql.benchmarking.stats.QueryRun;

/**
 * A parameterized query operation
 * 
 * @author rvesse
 * 
 */
public class ParameterizedQueryOperation extends AbstractParameterizedSparqlOperation<QueryRun> implements QueryOperation {

    /**
     * Creates a new parameterized query operation
     * 
     * @param sparqlString
     *            SPARQL String
     * @param parameters
     *            Parameters
     * @param name
     *            Name
     */
    public ParameterizedQueryOperation(String sparqlString, Collection<Binding> parameters, String name) {
        super(sparqlString, parameters, name);
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
    public String getType() {
        return "Parameterized SPARQL Query";
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
    public Query getQuery() {
        return this.getParameterizedSparql().asQuery();
    }

    @Override
    public String getQueryString() {
        return this.getParameterizedSparql().getCommandText();
    }

}
