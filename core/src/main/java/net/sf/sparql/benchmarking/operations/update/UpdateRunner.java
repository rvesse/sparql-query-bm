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

import java.util.concurrent.Callable;

import com.hp.hpl.jena.sparql.modify.UpdateProcessRemoteBase;
import com.hp.hpl.jena.update.UpdateExecutionFactory;
import com.hp.hpl.jena.update.UpdateRequest;

import net.sf.sparql.benchmarking.options.Options;
import net.sf.sparql.benchmarking.runners.Runner;
import net.sf.sparql.benchmarking.stats.UpdateRun;

/**
 * @author rvesse
 * 
 * @param <T>
 */
public class UpdateRunner<T extends Options> implements Callable<UpdateRun> {

    private UpdateRequest update;
    @SuppressWarnings("unused")
    private Runner<T> runner;
    private T options;
    private boolean cancelled = false;

    /**
     * Creates a new update runner
     * 
     * @param update
     *            Update to run
     * @param runner
     *            Runner
     * @param options
     *            Options
     */
    public UpdateRunner(UpdateRequest update, Runner<T> runner, T options) {
        this.update = update;
        this.runner = runner;
        this.options = options;
    }

    @Override
    public UpdateRun call() throws Exception {
        // Create a remote update processor and configure it appropriately
        UpdateProcessRemoteBase processor = (UpdateProcessRemoteBase) UpdateExecutionFactory.createRemote(this.update,
                this.options.getUpdateEndpoint());
        if (this.options.getAuthenticator() != null) {
            processor.setAuthenticator(this.options.getAuthenticator());
        }
        long startTime = System.nanoTime();

        // Execute the update
        processor.execute();

        if (cancelled)
            return null;

        long endTime = System.nanoTime();
        return new UpdateRun(endTime - startTime);
    }

    /**
     * Cancels the runner
     */
    public void cancel() {
        cancelled = true;
    }

}
