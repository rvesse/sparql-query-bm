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

package net.sf.sparql.benchmarking.operations.query.callables;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;

import net.sf.sparql.benchmarking.operations.AbstractOperationCallable;
import net.sf.sparql.benchmarking.options.BenchmarkOptions;
import net.sf.sparql.benchmarking.options.Options;
import net.sf.sparql.benchmarking.runners.Runner;
import net.sf.sparql.benchmarking.stats.OperationRun;

/**
 * Abstract callable for queries
 * 
 * @author rvesse
 * 
 * @param <T>
 *            Options type
 */
public abstract class AbstractQueryCallable<T extends Options> extends AbstractOperationCallable<T> {

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