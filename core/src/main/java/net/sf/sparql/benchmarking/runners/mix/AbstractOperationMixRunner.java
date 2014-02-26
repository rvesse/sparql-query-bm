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

package net.sf.sparql.benchmarking.runners.mix;

import net.sf.sparql.benchmarking.operations.Operation;
import net.sf.sparql.benchmarking.options.Options;
import net.sf.sparql.benchmarking.runners.Runner;
import net.sf.sparql.benchmarking.runners.operations.DefaultOperationRunner;
import net.sf.sparql.benchmarking.runners.operations.OperationRunner;
import net.sf.sparql.benchmarking.stats.OperationRun;

/**
 * Abstract implementation of an operation mix runner
 * 
 * @author rvesse
 * 
 */
public abstract class AbstractOperationMixRunner implements OperationMixRunner {

    protected boolean asThread = false;
    private OperationRunner defaultRunner = new DefaultOperationRunner();

    @Override
    public void setRunAsThread(boolean asThread) {
        this.asThread = asThread;
    }

    /**
     * Runs an operation based on the configured {@link OperationRunner} using
     * the {@link DefaultOperationRunner} if none is configured
     * 
     * @param options
     *            Options
     * @param op
     *            Operation to run
     * @return Operation run information
     */
    protected <T extends Options> OperationRun runOp(Runner<T> runner, T options, Operation op) {
        OperationRunner opRunner = options.getOperationRunner();
        if (opRunner == null)
            opRunner = this.defaultRunner;
        return opRunner.run(runner, options, op);
    }

}