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

import java.util.Iterator;

import net.sf.sparql.benchmarking.operations.AbstractOperation;
import net.sf.sparql.benchmarking.operations.Operation;
import net.sf.sparql.benchmarking.operations.OperationCallable;
import net.sf.sparql.benchmarking.operations.OperationMix;
import net.sf.sparql.benchmarking.options.Options;
import net.sf.sparql.benchmarking.runners.Runner;
import net.sf.sparql.benchmarking.stats.OperationRun;
import net.sf.sparql.benchmarking.stats.OperationRunImpl;

/**
 * An operation that runs an entire operation mix
 * 
 * @author rvesse
 * 
 */
public class MixOperation extends AbstractOperation {

    private OperationMix mix;
    private boolean randomOrder = false;

    /**
     * Creates a new mix operation
     * 
     * @param name
     *            Name
     * @param mix
     *            Mix
     */
    public MixOperation(String name, OperationMix mix) {
        this(name, mix, false);
    }

    /**
     * Creates a new mix operation
     * 
     * @param name
     *            Name
     * @param mix
     *            Mix
     * @param randomOrder
     *            Whether to randomize the order of operations
     */
    public MixOperation(String name, OperationMix mix, boolean randomOrder) {
        super(name);
        this.mix = mix;
        this.randomOrder = randomOrder;
    }

    @Override
    public <T extends Options> boolean canRun(Runner<T> runner, T options) {
        Iterator<Operation> ops = options.getOperationMix().getOperations();
        while (ops.hasNext()) {
            if (!ops.next().canRun(runner, options))
                return false;
        }
        return true;
    }

    @Override
    public String getType() {
        return "Operation Mix";
    }

    @Override
    public String getContentString() {
        return "Operation Mix containing " + this.mix.size() + " Operation(s)";
    }

    @Override
    public <T extends Options> OperationCallable<T> createCallable(Runner<T> runner, T options) {
        return new MixOperationCallable<T>(runner, options, this.mix, this.randomOrder);
    }

    @Override
    public OperationRun createErrorInformation(String message, int category, long runtime) {
        return new OperationRunImpl(message, category, runtime);
    }

}
