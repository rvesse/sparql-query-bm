/*
Copyright 2011-2014 Cray Inc. All Rights Reserved

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:

 * Redistributions of source code must retain the above copyright
  notice, this list of conditions and the following disclaimer.

 * Redistributions in binary form must reproduce the above copyright
  notice, this list of conditions and the following disclaimer in the
  documentation and/or other materials provided with the distribution.

 * Neither the name Cray Inc. nor the names of its contributors may be
  used to endorse or promote products derived from this software
  without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
 */

package net.sf.sparql.benchmarking.operations.query;

import org.apache.jena.atlas.web.HttpException;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
import com.hp.hpl.jena.sparql.engine.http.QueryExceptionHTTP;

import net.sf.sparql.benchmarking.operations.AbstractOperationCallable;
import net.sf.sparql.benchmarking.options.BenchmarkOptions;
import net.sf.sparql.benchmarking.options.Options;
import net.sf.sparql.benchmarking.runners.Runner;
import net.sf.sparql.benchmarking.stats.OperationRun;
import net.sf.sparql.benchmarking.stats.impl.QueryRun;
import net.sf.sparql.benchmarking.util.ConvertUtils;
import net.sf.sparql.benchmarking.util.ErrorCategories;

/**
 * Abstract callable for operations that run queries
 * 
 * @author rvesse
 * 
 * @param <T>
 */
public abstract class AbstractQueryCallable<T extends Options> extends AbstractOperationCallable<T> {

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
        exec.setAllowDeflate(options.getAllowCompression());
        exec.setAllowGZip(options.getAllowCompression());
        if (options.getAuthenticator() != null) {
            exec.setAuthenticator(options.getAuthenticator());
        }
        this.customizeRequest(exec);

        long numResults = 0;
        long responseTime = OperationRun.NOT_YET_RUN;
        long startTime = System.nanoTime();
        try {

            // Make the query
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

                // Abort if we have been cancelled by the time the engine
                // responds
                if (isCancelled()) {
                    return null;
                }
                this.getRunner().reportPartialProgress(options,
                        "started responding in " + ConvertUtils.toSeconds(responseTime) + "s...");
                numResults = countResults(options, rset);
            } else {
                logger.warn("Query is not of a recognised type and so was not run");
                if (options.getHaltAny())
                    this.getRunner().halt(options, "Unrecognized Query Type");
            }

            // Abort if we have been cancelled by the time the engine
            // responds
            if (isCancelled()) {
                return null;
            }

            // Return results
            long endTime = System.nanoTime();
            return new QueryRun(endTime - startTime, responseTime, numResults);

        } catch (HttpException e) {
            // Make sure to categorize HTTP errors appropriately
            return new QueryRun(e.getMessage(), ErrorCategories.categorizeHttpError(e), System.nanoTime() - startTime);
        } catch (QueryExceptionHTTP e) {
            return new QueryRun(e.getMessage(), ErrorCategories.categorizeHttpError(e), System.nanoTime() - startTime);
        } finally {
            // Clean up query execution
            if (exec != null)
                exec.close();
        }
    }

    /**
     * Provides derived implementations the option to customize the query
     * execution before actually executing the query e.g. to add custom
     * parameters
     * <p>
     * The default implementation does nothing.
     * </p>
     * 
     * @param qe
     *            Query Execution
     */
    protected void customizeRequest(QueryEngineHTTP qe) {
        // Default implementation does nothing
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
            if (((BenchmarkOptions) options).getNoCount()) {
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