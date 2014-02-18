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

package net.sf.sparql.benchmarking.operations.parameterized.nvp;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.sparql.engine.binding.Binding;
import net.sf.sparql.benchmarking.operations.OperationCallable;
import net.sf.sparql.benchmarking.operations.parameterized.ParameterizedUpdateOperation;
import net.sf.sparql.benchmarking.operations.update.nvp.NvpUpdateCallable;
import net.sf.sparql.benchmarking.options.Options;
import net.sf.sparql.benchmarking.runners.Runner;
import net.sf.sparql.benchmarking.stats.UpdateRun;

/**
 * An operation that runs a parameterized update with custom NVPs added to the
 * request
 * 
 * @author rvesse
 * 
 */
public class ParameterizedNvpUpdateOperation extends ParameterizedUpdateOperation {

    private Map<String, List<String>> nvps = new HashMap<String, List<String>>();

    /**
     * Creates a new parameterized query operation
     * 
     * @param sparqlString
     *            Update string
     * @param parameters
     *            Parameters
     * @param name
     *            Name
     * @param nvps
     *            Name value pairs
     */
    public ParameterizedNvpUpdateOperation(String sparqlString, Collection<Binding> parameters, String name,
            Map<String, List<String>> nvps) {
        super(sparqlString, parameters, name);
        this.nvps.putAll(nvps);
    }

    @Override
    protected <T extends Options> OperationCallable<T, UpdateRun> createCallable(Runner<T> runner, T options) {
        return new NvpUpdateCallable<T>(this.getUpdate(), runner, options, this.nvps);
    }
    
    @Override
    public String getType() {
        return "Parameterized SPARQL Update with NVPs";
    }
}
