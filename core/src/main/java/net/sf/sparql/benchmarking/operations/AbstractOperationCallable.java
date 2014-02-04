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

package net.sf.sparql.benchmarking.operations;

import net.sf.sparql.benchmarking.options.Options;
import net.sf.sparql.benchmarking.runners.Runner;
import net.sf.sparql.benchmarking.stats.OperationRun;

/**
 * Abstract implementation of an operation callable
 * 
 * @author rvesse
 * 
 * @param <T>
 *            Options type
 * @param <TRun>
 *            Operation run information type
 */
public abstract class AbstractOperationCallable<T extends Options, TRun extends OperationRun> implements OperationCallable<T, TRun> {

    private Runner<T> runner;
    private T options;
    private boolean cancelled = false;

    /**
     * Creates a new operation callable
     * 
     * @param runner
     *            Runner
     * @param options
     *            Options
     */
    public AbstractOperationCallable(Runner<T> runner, T options) {
        this.runner = runner;
        this.options = options;
    }

    /**
     * Gets the runner
     * 
     * @return Runner
     */
    protected final Runner<T> getRunner() {
        return runner;
    }

    /**
     * Gets the options
     * 
     * @return Options
     */
    protected final T getOptions() {
        return options;
    }

    /**
     * Gets whether the callable has been asked to cancel
     * 
     * @return
     */
    protected final boolean isCancelled() {
        return cancelled;
    }

    @Override
    public final void cancel() {
        cancelled = true;
    }

}