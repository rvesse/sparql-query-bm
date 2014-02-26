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

import net.sf.sparql.benchmarking.operations.AbstractOperationCallable;
import net.sf.sparql.benchmarking.operations.OperationMix;
import net.sf.sparql.benchmarking.options.Options;
import net.sf.sparql.benchmarking.runners.Runner;
import net.sf.sparql.benchmarking.runners.mix.DefaultOperationMixRunner;
import net.sf.sparql.benchmarking.runners.mix.InOrderOperationMixRunner;
import net.sf.sparql.benchmarking.runners.mix.OperationMixRunner;
import net.sf.sparql.benchmarking.stats.OperationMixRun;
import net.sf.sparql.benchmarking.stats.OperationRun;
import net.sf.sparql.benchmarking.stats.OperationRunImpl;
import net.sf.sparql.benchmarking.util.ErrorCategories;

/**
 * A callable for running an operation mix as an operation
 * 
 * @author rvesse
 * 
 * @param <T>
 */
public class MixOperationCallable<T extends Options> extends AbstractOperationCallable<T, OperationRun> {

    private OperationMix mix;
    private OperationMixRunner defaultRunner;

    /**
     * Creates a new callable
     * 
     * @param runner
     *            Runner
     * @param options
     *            Options
     * @param mix
     *            Mix
     * @param randomOrder
     *            Whether to randomize the order of operations
     */
    public MixOperationCallable(Runner<T> runner, T options, OperationMix mix, boolean randomOrder) {
        super(runner, options);
        this.mix = mix;
        this.defaultRunner = randomOrder ? new DefaultOperationMixRunner() : new InOrderOperationMixRunner();
    }

    @Override
    public OperationRun call() throws Exception {
        // Run the operation mix and then flatten its results appropriately
        OperationMixRun run = this.defaultRunner.run(this.getRunner(), this.getOptions(), this.mix);
        if (run.getTotalErrors() > 0) {
            return new OperationRunImpl(String.format("%d error(s) occurred in a child operation mix", run.getTotalErrors()),
                    ErrorCategories.CHILD_MIX, run.getTotalRuntime());
        } else {
            return new OperationRunImpl(run.getTotalRuntime(), run.getTotalResponseTime(), run.getTotalResults());
        }
    }

}
