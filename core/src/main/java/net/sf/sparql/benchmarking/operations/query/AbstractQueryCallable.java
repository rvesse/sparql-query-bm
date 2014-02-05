/*
 * Copyright 2014 YarcData LLC All Rights Reserved.
 */

package net.sf.sparql.benchmarking.operations.query;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

import net.sf.sparql.benchmarking.BenchmarkerUtils;
import net.sf.sparql.benchmarking.operations.AbstractOperationCallable;
import net.sf.sparql.benchmarking.options.BenchmarkOptions;
import net.sf.sparql.benchmarking.options.Options;
import net.sf.sparql.benchmarking.runners.Runner;
import net.sf.sparql.benchmarking.stats.OperationRun;
import net.sf.sparql.benchmarking.stats.QueryRun;

/**
 * Abstract callable for operations that run queries
 * 
 * @author rvesse
 * 
 * @param <T>
 */
public abstract class AbstractQueryCallable<T extends Options> extends AbstractOperationCallable<T, QueryRun> {

    private static final Logger logger = Logger.getLogger(QueryCallable.class);

    /**
     * Creates a new callable
     * 
     * @param runner
     *            Runner
     * @param options
     *            Options
     */
    public AbstractQueryCallable(Runner<T> runner, T options) {
        super(runner, options);
    }

    /**
     * Gets the query to be run
     * 
     * @return Query
     */
    protected abstract Query getQuery();

    /**
     * Runs the Query counting the number of Results
     */
    @Override
    public QueryRun call() {
        T options = this.getOptions();
        BenchmarkOptions bOps = null;
        if (options instanceof BenchmarkOptions) {
            bOps = (BenchmarkOptions) options;
        }

        Query query = this.getQuery();

        // Impose Limit if applicable
        if (bOps != null) {
            if (bOps.getLimit() > 0) {
                if (!query.isAskType()) {
                    if (query.getLimit() == Query.NOLIMIT || query.getLimit() > bOps.getLimit()) {
                        query.setLimit(bOps.getLimit());
                    }
                }
            }
        }

        logger.debug("Running query:\n" + query.toString());

        // Create a QueryEngineHTTP directly as we want to set a bunch of
        // parameters on it
        QueryEngineHTTP exec = new QueryEngineHTTP(options.getQueryEndpoint(), query);
        exec.setSelectContentType(options.getResultsSelectFormat());
        exec.setAskContentType(options.getResultsAskFormat());
        exec.setModelContentType(options.getResultsGraphFormat());
        exec.setAllowDeflate(options.getAllowDeflateEncoding());
        exec.setAllowGZip(options.getAllowGZipEncoding());
        if (options.getAuthenticator() != null) {
            exec.setAuthenticator(options.getAuthenticator());
        }

        try {
            long numResults = 0;
            long responseTime = OperationRun.NOT_YET_RUN;
            long startTime = System.nanoTime();
            if (query.isAskType()) {
                boolean result = exec.execAsk();
                numResults = countResults(options, result);
            } else if (query.isConstructType()) {
                Model m = exec.execConstruct();
                numResults = countResults(options, m);
            } else if (query.isDescribeType()) {
                Model m = exec.execDescribe();
                numResults = countResults(options, m);
            } else if (query.isSelectType()) {
                ResultSet rset = exec.execSelect();
                responseTime = System.nanoTime() - startTime;
                if (isCancelled())
                    return null; // Abort if we have been cancelled by the time
                                 // the engine responds
                this.getRunner().reportPartialProgress(options,
                        "started responding in " + BenchmarkerUtils.toSeconds(responseTime) + "s...");
                numResults = countResults(options, rset);
            } else {
                logger.warn("Query is not of a recognised type and so was not run");
                if (options.getHaltAny())
                    this.getRunner().halt(options, "Unrecognized Query Type");
            }
            long endTime = System.nanoTime();
            return new QueryRun(endTime - startTime, responseTime, numResults);
        } finally {
            // Clean up query execution
            if (exec != null)
                exec.close();
        }
    }

    /**
     * Counts the results for queries that return a boolean
     * <p>
     * The default implementation always returns {@code 1}
     * </p>
     * 
     * @param options
     *            Options
     * @param result
     *            Result
     * @return Number of results
     */
    protected long countResults(T options, boolean result) {
        return 1;
    }

    /**
     * Counts results for queries that return a model.
     * <p>
     * The default implementation returns the size of the model
     * </p>
     * 
     * @param options
     *            Options
     * @param m
     *            Model
     * @return Number of results
     */
    protected long countResults(T options, Model m) {
        return m.size();
    }

    /**
     * Counts results for queries that return a result set
     * <p>
     * The default implementation either returns {@link OperationRun#UNKNOWN} if
     * the options indicate that counting is disabled or iterates over the
     * results to count them.
     * </p>
     * 
     * @param options
     *            Options
     * @param rset
     *            Result Set
     * @return Number of results
     */
    protected long countResults(T options, ResultSet rset) {
        // Result Counting may be skipped depending on user options
        if (options instanceof BenchmarkOptions) {
            if (!((BenchmarkOptions) options).getNoCount()) {
                return OperationRun.UNKNOWN;
            }
        }

        // Count Results
        long numResults = 0;
        while (rset.hasNext() && !isCancelled()) {
            numResults++;
            rset.next();
        }
        return numResults;
    }

}