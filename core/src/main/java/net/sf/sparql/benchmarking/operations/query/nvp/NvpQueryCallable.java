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

package net.sf.sparql.benchmarking.operations.query.nvp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;

import net.sf.sparql.benchmarking.operations.query.callables.RemoteQueryCallable;
import net.sf.sparql.benchmarking.options.Options;
import net.sf.sparql.benchmarking.runners.Runner;

/**
 * A query callable that adds custom NVPs to the request
 * 
 * @author rvesse
 * 
 * @param <T>
 *            Options type
 */
public class NvpQueryCallable<T extends Options> extends RemoteQueryCallable<T> {

    private Map<String, List<String>> nvps = new HashMap<String, List<String>>();

    /**
     * Creates a new callable
     * 
     * @param q
     *            Query
     * @param runner
     *            Runner
     * @param options
     *            Options
     * @param nvps
     *            Name value pairs
     */
    public NvpQueryCallable(Query q, Runner<T> runner, T options, Map<String, List<String>> nvps) {
        super(q, runner, options);
        this.nvps.putAll(nvps);
    }

    @Override
    protected void customizeRequest(QueryExecution qe) {
        super.customizeRequest(qe);
        if (qe instanceof QueryEngineHTTP) {
            QueryEngineHTTP remote = (QueryEngineHTTP) qe;
            for (Entry<String, List<String>> nvp : this.nvps.entrySet()) {
                for (String value : nvp.getValue()) {
                    remote.addParam(nvp.getKey(), value);
                }
            }
        }
    }

}
