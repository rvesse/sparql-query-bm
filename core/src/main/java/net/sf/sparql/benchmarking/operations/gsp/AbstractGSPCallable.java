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

package net.sf.sparql.benchmarking.operations.gsp;

import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.query.DatasetAccessor;
import com.hp.hpl.jena.query.DatasetAccessorFactory;
import com.hp.hpl.jena.sparql.core.Quad;

import net.sf.sparql.benchmarking.operations.AbstractOperationCallable;
import net.sf.sparql.benchmarking.options.Options;
import net.sf.sparql.benchmarking.runners.Runner;
import net.sf.sparql.benchmarking.stats.OperationRun;
import net.sf.sparql.benchmarking.stats.impl.OperationRunImpl;

/**
 * Abstract callable for GSP operations
 * 
 * @author rvesse
 * 
 * @param <T>
 *            Options type
 */
public abstract class AbstractGSPCallable<T extends Options> extends AbstractOperationCallable<T> {

    private String uri;

    /**
     * Creates a new callable
     * 
     * @param runner
     *            Runner
     * @param options
     *            Options
     */
    public AbstractGSPCallable(Runner<T> runner, T options) {
        this(runner, options, null);
    }

    /**
     * Creates a new callable
     * 
     * @param runner
     *            Runner
     * @param options
     *            Options
     * @param uri
     *            Graph URI
     */
    public AbstractGSPCallable(Runner<T> runner, T options, String uri) {
        super(runner, options);
        this.uri = uri;
    }

    @Override
    public OperationRun call() {
        long numResults = OperationRun.NOT_YET_RUN;

        DatasetAccessor accessor = this.getAccessor();
        long startTime = System.nanoTime();
        numResults = this.doOperation(accessor);
        long endTime = System.nanoTime();

        return new OperationRunImpl(endTime - startTime, numResults);
    }

    /**
     * Does the operation using the accessor and returns the number of results
     * 
     * @param accessor
     *            Accessor
     * @return Number of results
     */
    protected abstract long doOperation(DatasetAccessor accessor);

    /**
     * Gets whether the callable is operating on the default grah
     * 
     * @return True if operating on the default graph, false otherwise
     */
    protected boolean isDefaultGraphUri() {
        return this.uri == null || Quad.isDefaultGraph(NodeFactory.createURI(this.uri));
    }

    /**
     * Gets the URI of the graph this callable operates on
     * 
     * @return Graph URI
     */
    protected final String getGraphUri() {
        return this.uri;
    }

    /**
     * Gets a {@link DatasetAccessor} prepared with the configured options
     * 
     * @return Dataset Accessor
     */
    protected DatasetAccessor getAccessor() {
        return DatasetAccessorFactory.createHTTP(this.getOptions().getGraphStoreEndpoint(), this.getOptions().getAuthenticator());
    }

}
