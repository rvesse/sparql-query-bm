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

package net.sf.sparql.query.benchmarking.queries;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import net.sf.sparql.query.benchmarking.operations.AbstractBenchmarkOperation;
import net.sf.sparql.query.benchmarking.options.Options;
import net.sf.sparql.query.benchmarking.runners.Runner;
import net.sf.sparql.query.benchmarking.stats.OperationRun;
import net.sf.sparql.query.benchmarking.stats.QueryRun;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;

/**
 * Represents a Query that will be run as part of a Benchmark
 * 
 * @author rvesse
 */
public class BenchmarkQuery extends AbstractBenchmarkOperation implements BenchmarkQueryOperation {

    private static final Logger logger = Logger.getLogger(BenchmarkQuery.class);
    private Query query;
    private String origQueryStr;

    /**
     * Creates a new Query
     * 
     * @param name
     *            Name of the query
     * @param queryString
     *            Query string
     */
    public BenchmarkQuery(String name, String queryString) {
        super(name);
        this.origQueryStr = queryString;
        this.query = QueryFactory.create(this.origQueryStr);
    }

    @Override
    public Query getQuery() {
        return this.query;
    }

    @Override
    public String getQueryString() {
        return this.origQueryStr;
    }

    @Override
    public <T extends Options> boolean canRun(Runner<T> runner, T options) {
        if (options.getQueryEndpoint() == null) {
            runner.reportProgress(options, "Benchmark Queries cannot run with no query endpoint specified");
            return false;
        }
        return true;
    }

    @Override
    public <T extends Options> OperationRun run(Runner<T> runner, T options) {
        timer.start();
        long order = options.getGlobalOrder();
        QueryRunner<T> queryRunner = new QueryRunner<T>(this.query, runner, options);
        QueryTask<T> task = new QueryTask<T>(queryRunner);
        options.getExecutor().submit(task);
        QueryRun r;
        long startTime = System.nanoTime();
        try {
            r = task.get(options.getTimeout(), TimeUnit.SECONDS);
        } catch (TimeoutException tEx) {
            logger.error("Query Runner execeeded Timeout - " + tEx.getMessage());
            if (options.getHaltOnTimeout() || options.getHaltAny())
                runner.halt(options, tEx);
            r = new QueryRun("Query Runner execeeded Timeout - " + tEx.getMessage(), System.nanoTime() - startTime);

            // If the query times out but we aren't halting cancel further
            // evaluation of the query
            task.cancel(true);
            queryRunner.cancel();
        } catch (InterruptedException e) {
            logger.error("Query Runner was interrupted - " + e.getMessage());
            if (options.getHaltAny())
                runner.halt(options, e);
            r = new QueryRun("Query Runner was interrupted - " + e.getMessage(), System.nanoTime() - startTime);
        } catch (ExecutionException e) {
            logger.error("Query Runner encountered an error - " + e.getMessage());

            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            logger.error(sw.toString());

            if (options.getHaltOnError() || options.getHaltAny())
                runner.halt(options, e);
            r = new QueryRun("Query Runner encountered an error - " + e.getMessage(), System.nanoTime() - startTime);
        }
        timer.stop();
        this.addRun(r);
        r.setRunOrder(order);
        return r;
    }

    /**
     * Gets the string representation (i.e. the name) of the operation
     */
    @Override
    public String toString() {
        return this.getName();
    }

    @Override
    public String getType() {
        return "SPARQL Query";
    }

    @Override
    public String getContentString() {
        return this.getQueryString();
    }
}
