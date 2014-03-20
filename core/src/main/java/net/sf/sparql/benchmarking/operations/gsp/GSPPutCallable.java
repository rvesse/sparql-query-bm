/*
SPARQL Query Benchmarker is licensed under a 3 Clause BSD License

----------------------------------------------------------------------

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

import com.hp.hpl.jena.query.DatasetAccessor;
import com.hp.hpl.jena.rdf.model.Model;

import net.sf.sparql.benchmarking.options.Options;
import net.sf.sparql.benchmarking.runners.Runner;

/**
 * A callable which runs the Graph Store Protocol PUT operation
 * 
 * @author rvesse
 * 
 * @param <T>
 *            Options type
 */
public class GSPPutCallable<T extends Options> extends AbstractGSPCallable<T> {

    private Model data;

    /**
     * Creates a new callable that operates on the default graph
     * 
     * @param runner
     *            Runner
     * @param options
     *            Options
     * @param data
     *            Data to be added
     */
    public GSPPutCallable(Runner<T> runner, T options, Model data) {
        this(runner, options, data, null);
    }

    /**
     * Creates a new callable that operates on a specific graph
     * 
     * @param runner
     *            Runner
     * @param options
     *            Options
     * @param data
     *            Data to be added
     * @param uri
     *            Graph URI
     */
    public GSPPutCallable(Runner<T> runner, T options, Model data, String uri) {
        super(runner, options, uri);
        this.data = data;
    }

    @Override
    protected long doOperation(DatasetAccessor accessor) {
        if (this.isDefaultGraphUri()) {
            accessor.putModel(this.data);
        } else {
            accessor.putModel(this.getGraphUri(), this.data);
        }
        return 0;
    }

}
