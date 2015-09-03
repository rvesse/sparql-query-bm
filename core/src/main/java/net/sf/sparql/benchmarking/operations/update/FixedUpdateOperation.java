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

package net.sf.sparql.benchmarking.operations.update;

import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;

import net.sf.sparql.benchmarking.operations.AbstractOperation;
import net.sf.sparql.benchmarking.operations.OperationCallable;
import net.sf.sparql.benchmarking.operations.update.callables.RemoteUpdateCallable;
import net.sf.sparql.benchmarking.options.Options;
import net.sf.sparql.benchmarking.runners.Runner;
import net.sf.sparql.benchmarking.stats.OperationRun;
import net.sf.sparql.benchmarking.stats.impl.UpdateRun;

/**
 * An operation that makes a fixed SPARQL Update against a remote SPARQL service via HTTP
 * 
 * @author rvesse
 * 
 */
public class FixedUpdateOperation extends AbstractOperation implements UpdateOperation {

    private UpdateRequest update;
    private String origUpdateStr;

    /**
     * Creates a new update operation
     * 
     * @param name
     *            Name
     * @param updateString
     *            SPARQL Update
     */
    public FixedUpdateOperation(String name, String updateString) {
        super(name);
        this.origUpdateStr = updateString;
        this.update = UpdateFactory.create(updateString);
    }

    @Override
    public <T extends Options> boolean canRun(Runner<T> runner, T options) {
        if (options.getUpdateEndpoint() == null) {
            runner.reportProgress(options, "Remote updates cannot run with no update endpoint specified");
            return false;
        }
        return true;
    }

    @Override
    public <T extends Options> OperationCallable<T> createCallable(Runner<T> runner, T options) {
        return new RemoteUpdateCallable<T>(this.update, runner, options);
    }

    @Override
    public OperationRun createErrorInformation(String message, int category, long runtime) {
        return new UpdateRun(message, category, runtime);
    }

    @Override
    public String getType() {
        return "Remote SPARQL Update";
    }

    @Override
    public String getContentString() {
        return this.getUpdateString();
    }

    @Override
    public UpdateRequest getUpdate() {
        return this.update;
    }

    @Override
    public String getUpdateString() {
        return this.origUpdateStr;
    }

}
