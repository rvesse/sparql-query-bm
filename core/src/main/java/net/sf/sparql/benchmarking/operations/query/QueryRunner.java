/** 
 * Copyright 2011-2012 Cray Inc. All Rights Reserved
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name Cray Inc. nor the names of its contributors may be
 *   used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 **/

package net.sf.sparql.benchmarking.operations.query;

import java.util.concurrent.Callable;

import net.sf.sparql.benchmarking.BenchmarkerUtils;
import net.sf.sparql.benchmarking.options.BenchmarkOptions;
import net.sf.sparql.benchmarking.options.Options;
import net.sf.sparql.benchmarking.runners.Runner;
import net.sf.sparql.benchmarking.stats.OperationRun;
import net.sf.sparql.benchmarking.stats.QueryRun;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

/**
 * A Callable for queries so we can execute them asynchronously with timeouts
 * 
 * @author rvesse
 * @param <T>
 *            Options type
 * 
 */
public class QueryRunner<T extends Options> implements Callable<QueryRun> {

    private static final Logger logger = Logger.getLogger(QueryRunner.class);
    private Query query;
    private Runner<T> runner;
    private T options;
    private boolean cancelled = false;

    /**
     * Creates a new Query Runner
     * 
     * @param q
     *            Query to run
     * @param runner
     *            Runner
     * @param options
     *            Options
     */
    public QueryRunner(Query q, Runner<T> runner, T options) {
        this.query = q;
        this.runner = runner;
        this.options = options;
    }

    /**
     * Runs the Query counting the number of Results
     */
    @Override
    public QueryRun call() {
        BenchmarkOptions bOps = null;
        if (this.options instanceof BenchmarkOptions) {
            bOps = (BenchmarkOptions) this.options;
        }

        // Impose Limit if applicable
        if (bOps != null) {
            if (bOps.getLimit() > 0) {
                if (!this.query.isAskType()) {
                    if (this.query.getLimit() == Query.NOLIMIT || this.query.getLimit() > bOps.getLimit()) {
                        this.query.setLimit(bOps.getLimit());
                    }
                }
            }
        }

        // Create a QueryEngineHTTP directly as we want to set a bunch of
        // parameters on it
        QueryEngineHTTP exec = new QueryEngineHTTP(this.options.getQueryEndpoint(), this.query);
        exec.setSelectContentType(options.getResultsSelectFormat());
        exec.setAskContentType(options.getResultsAskFormat());
        exec.setModelContentType(options.getResultsGraphFormat());
        exec.setAllowDeflate(options.getAllowDeflateEncoding());
        exec.setAllowGZip(options.getAllowGZipEncoding());
        if (this.options.getAuthenticator() != null) {
            exec.setAuthenticator(this.options.getAuthenticator());
        }

        try {
            long numResults = 0;
            long responseTime = OperationRun.NOT_YET_RUN;
            long startTime = System.nanoTime();
            if (this.query.isAskType()) {
                exec.execAsk();
                numResults = 1;
            } else if (this.query.isConstructType()) {
                Model m = exec.execConstruct();
                numResults = m.size();
            } else if (this.query.isDescribeType()) {
                Model m = exec.execDescribe();
                numResults = m.size();
            } else if (this.query.isSelectType()) {
                ResultSet rset = exec.execSelect();
                responseTime = System.nanoTime() - startTime;
                if (cancelled)
                    return null; // Abort if we have been cancelled by the time
                                 // the engine responds
                this.runner.reportPartialProgress(this.options,
                        "started responding in " + BenchmarkerUtils.toSeconds(responseTime) + "s...");

                // Result Counting may be skipped depending on user options
                if (bOps != null) {
                    if (!bOps.getNoCount()) {
                        return new QueryRun(System.nanoTime() - startTime, responseTime, OperationRun.UNKNOWN);
                    }
                }

                // Count Results
                while (rset.hasNext() && !cancelled) {
                    numResults++;
                    rset.next();
                }
            } else {
                logger.warn("Query is not of a recognised type and so was not run");
                if (this.options.getHaltAny())
                    this.runner.halt(this.options, "Unrecognized Query Type");
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
     * Cancels a Query Runner
     */
    public void cancel() {
        cancelled = true;
    }
}
