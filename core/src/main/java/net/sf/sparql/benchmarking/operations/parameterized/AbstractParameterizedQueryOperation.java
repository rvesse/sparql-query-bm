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

package net.sf.sparql.benchmarking.operations.parameterized;

import java.util.Collection;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

import net.sf.sparql.benchmarking.operations.OperationCallable;
import net.sf.sparql.benchmarking.operations.query.QueryOperation;
import net.sf.sparql.benchmarking.operations.query.callables.RemoteQueryCallable;
import net.sf.sparql.benchmarking.options.Options;
import net.sf.sparql.benchmarking.runners.Runner;
import net.sf.sparql.benchmarking.stats.impl.QueryRun;

/**
 * Abstract parameterized SPARQL query operation
 * 
 * @author rvesse
 * 
 */
public abstract class AbstractParameterizedQueryOperation extends AbstractParameterizedSparqlOperation implements QueryOperation {

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
    public AbstractParameterizedQueryOperation(String sparqlString, Collection<Binding> parameters, String name) {
        super(sparqlString, parameters, name);
    }

    @Override
    public QueryRun createErrorInformation(String message, int category, long runtime) {
        return new QueryRun(message, category, runtime);
    }

    @Override
    public Query getQuery() {
        return this.getParameterizedSparql().asQuery();
    }

    @Override
    public String getQueryString() {
        return this.getParameterizedSparql().getCommandText();
    }

    @Override
    public <T extends Options> OperationCallable<T> createCallable(Runner<T> runner, T options) {
        return new RemoteQueryCallable<T>(this.getQuery(), runner, options);
    }
}
