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

import net.sf.sparql.benchmarking.operations.AbstractOperation;
import net.sf.sparql.benchmarking.options.Options;
import net.sf.sparql.benchmarking.runners.Runner;
import net.sf.sparql.benchmarking.stats.OperationRun;
import net.sf.sparql.benchmarking.stats.impl.OperationRunImpl;

/**
 * Abstract implementation of a SPARQL Graph Store Protocol operation
 * 
 * @author rvesse
 * 
 */
public abstract class AbstractGSPOperation extends AbstractOperation {

    private String uri;

    /**
     * Creates a new operation
     * 
     * @param name
     *            Name
     */
    public AbstractGSPOperation(String name) {
        this(name, null);
    }

    /**
     * Creates a new operation
     * 
     * @param name
     *            Name
     * @param uri
     *            Graph URI
     */
    public AbstractGSPOperation(String name, String uri) {
        super(name);
        this.uri = uri;
    }

    /**
     * Gets the URI of the graph being operated upon, {@code null} is considered
     * to mean that the default graph is operated upon
     * 
     * @return Graph URI
     */
    protected String getGraphUri() {
        return this.uri;
    }

    @Override
    public <T extends Options> boolean canRun(Runner<T> runner, T options) {
        if (options.getGraphStoreEndpoint() == null) {
            runner.reportProgress(options, "Graph Store Protocol operations cannot run with no GSP endpoint specified");
            return false;
        }
        return true;
    }

    @Override
    public OperationRun createErrorInformation(String message, int category, long runtime) {
        return new OperationRunImpl(message, category, runtime);
    }

}
