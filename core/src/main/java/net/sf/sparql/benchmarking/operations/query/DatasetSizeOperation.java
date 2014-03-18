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

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;

import net.sf.sparql.benchmarking.loader.InMemoryOperations;
import net.sf.sparql.benchmarking.operations.OperationCallable;
import net.sf.sparql.benchmarking.operations.query.callables.InMemoryQueryCallable;
import net.sf.sparql.benchmarking.operations.query.callables.LongValueCallable;
import net.sf.sparql.benchmarking.operations.query.callables.RemoteQueryCallable;
import net.sf.sparql.benchmarking.options.Options;
import net.sf.sparql.benchmarking.runners.Runner;

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
    public <T extends Options> OperationCallable<T> createCallable(Runner<T> runner, T options) {
        if (options.getQueryEndpoint() != null) {
            return new LongValueCallable<T, RemoteQueryCallable<T>>(runner, options, new RemoteQueryCallable<T>(this.getQuery(),
                    runner, options), COUNT_VARIABLE);
        } else {
            return new LongValueCallable<T, InMemoryQueryCallable<T>>(runner, options, new InMemoryQueryCallable<T>(
                    this.getQuery(), runner, options), COUNT_VARIABLE);
        }
    }

    @Override
    public <T extends Options> boolean canRun(Runner<T> runner, T options) {
        if (options.getQueryEndpoint() == null && !InMemoryOperations.hasDataset(runner, options, "queries")) {
            runner.reportProgress(options,
                    "Dataset size queries cannot run with no remote query endpoint/in-memory dataset specified");
            return false;
        }
        return true;
    }
}
