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

package net.sf.sparql.benchmarking.parallel.impl;

import java.util.concurrent.Callable;

import net.sf.sparql.benchmarking.options.Options;
import net.sf.sparql.benchmarking.runners.Runner;
import net.sf.sparql.benchmarking.runners.mix.DefaultOperationMixRunner;
import net.sf.sparql.benchmarking.runners.mix.OperationMixRunner;
import net.sf.sparql.benchmarking.stats.OperationMixRun;

/**
 * A callable for operation mixes so we can execute them in parallel to do
 * multi-threaded benchmarks
 * 
 * @author rvesse
 * @param <T>
 *            Options type
 * 
 */
public class OperationMixCallable<T extends Options> implements Callable<OperationMixRun> {

    private T options;
    private Runner<T> runner;
    private OperationMixRunner defaultRunner = new DefaultOperationMixRunner();

    /**
     * Creates a new operation mix runner
     * 
     * @param runner
     *            Runner
     * @param options
     *            Options
     */
    public OperationMixCallable(Runner<T> runner, T options) {
        this.runner = runner;
        this.options = options;
    }

    /**
     * Runs the operation mix returning the results of the run
     */
    @Override
    public OperationMixRun call() {
        OperationMixRunner runner = this.options.getMixRunner();
        if (runner == null)
            runner = this.defaultRunner;
        return runner.run(this.runner, this.options, this.options.getOperationMix());
    }

}
