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

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import net.sf.sparql.benchmarking.loader.InMemoryOperations;
import net.sf.sparql.benchmarking.options.Options;
import net.sf.sparql.benchmarking.runners.Runner;

/**
 * Abstract callable for operations that run queries against a local in-memory
 * dataset
 * 
 * @author rvesse
 * 
 * @param <T>
 *            Options type
 */
public abstract class AbstractInMemoryQueryCallable<T extends Options> extends AbstractQueryCallable<T> {

    /**
     * Creates a new callable
     * 
     * @param runner
     *            Runner
     * @param options
     *            Options
     */
    public AbstractInMemoryQueryCallable(Runner<T> runner, T options) {
        super(runner, options);
    }

    /**
     * Gets the dataset to run the query against
     * <p>
     * By default all in-memory based operations expect to find a dataset in the
     * custom setting {@code dataset} however derived implementations may choose
     * to do this differently and provide the dataset in other ways.
     * </p>
     * 
     * @return Dataset
     */
    protected Dataset getDataset(T options) {
        Object dsObj = options.getCustomSettings().get(InMemoryOperations.DATASET_SETTING_KEY);
        if (dsObj instanceof Dataset)
            return (Dataset) dsObj;
        throw new IllegalArgumentException("Expected an object of type Dataset but got an object of type "
                + dsObj.getClass().getName());
    }

    @Override
    protected QueryExecution createQueryExecution(Query query) {
        return QueryExecutionFactory.create(this.getQuery(), this.getDataset(this.getOptions()));
    }
}