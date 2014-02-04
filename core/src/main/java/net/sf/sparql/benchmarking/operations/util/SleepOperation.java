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

package net.sf.sparql.benchmarking.operations.util;

import net.sf.sparql.benchmarking.operations.AbstractOperation;
import net.sf.sparql.benchmarking.operations.OperationCallable;
import net.sf.sparql.benchmarking.options.Options;
import net.sf.sparql.benchmarking.runners.Runner;
import net.sf.sparql.benchmarking.stats.OperationRun;
import net.sf.sparql.benchmarking.stats.OperationRunImpl;

/**
 * Creates a new sleep operation
 * 
 * @author rvesse
 * 
 */
public class SleepOperation extends AbstractOperation<OperationRun> {

    private long sleep;

    /**
     * Creates a new sleep operation
     * 
     * @param sleep
     *            Sleep time in seconds
     */
    public SleepOperation(long sleep) {
        this(String.format("Sleep %d Seconds", sleep), sleep);
    }

    /**
     * Creates a new sleep operation
     * 
     * @param name
     *            Name
     * @param sleep
     *            Sleep time in seconds
     */
    public SleepOperation(String name, long sleep) {
        super(name);
        if (sleep < 0)
            throw new IllegalArgumentException("Sleep time must be >= 0");
        this.sleep = sleep;
    }

    @Override
    public <T extends Options> boolean canRun(Runner<T> runner, T options) {
        if (this.sleep > options.getTimeout()) {
            runner.reportProgress(options, "Sleep operation cannot sleep for longer than operation timeout");
            return false;
        }
        return true;
    }

    @Override
    public String getType() {
        return "Sleep";
    }

    @Override
    public String getContentString() {
        return String.format("Sleep %d Seconds", this.sleep);
    }

    @Override
    protected <T extends Options> OperationCallable<T, OperationRun> createCallable(Runner<T> runner, T options) {
        return new SleepCallable<T>(sleep, runner, options);
    }

    @Override
    protected OperationRun createErrorInformation(String message, long runtime) {
        return new OperationRunImpl(message, runtime);
    }
}
