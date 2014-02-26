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

import java.util.ArrayList;
import java.util.List;

import net.sf.sparql.benchmarking.operations.Operation;
import net.sf.sparql.benchmarking.operations.OperationMix;
import net.sf.sparql.benchmarking.options.Options;
import net.sf.sparql.benchmarking.runners.Runner;
import net.sf.sparql.benchmarking.stats.OperationMixRun;
import net.sf.sparql.benchmarking.stats.OperationRun;
import net.sf.sparql.benchmarking.stats.impl.OperationMixRunImpl;
import net.sf.sparql.benchmarking.util.ConvertUtils;
import net.sf.sparql.benchmarking.util.FormatUtils;

/**
 * Default operation mix runner which runs all the operations in a mix either in
 * random/non-random order depending on the {@link Options} provided.
 * 
 * @author rvesse
 * 
 */
public class DefaultOperationMixRunner extends AbstractOperationMixRunner {

    @Override
    public <T extends Options> OperationMixRun run(Runner<T> runner, T options, OperationMix mix) {
        long runOrder = options.getGlobalOrder();
        List<OperationRun> runs = new ArrayList<OperationRun>();
        for (int i = 0; i < options.getOperationMix().size(); i++) {
            runs.add(null);
        }

        // If running as thread then we prefix all our progress messages with a
        // Thread ID
        String prefix = this.asThread ? "[Thread " + Thread.currentThread().getId() + "] " : "";

        // Generate a random sequence of integers so we execute the queries in a
        // random order
        // each time the query set is run
        List<Integer> ids = new ArrayList<Integer>();
        if (options.getRandomizeOrder()) {
            // Randomize the Order
            List<Integer> unallocatedIds = new ArrayList<Integer>();
            for (int i = 0; i < mix.size(); i++) {
                unallocatedIds.add(i);
            }
            while (unallocatedIds.size() > 0) {
                int id = (int) (Math.random() * unallocatedIds.size());
                ids.add(unallocatedIds.get(id));
                unallocatedIds.remove(id);
            }
        } else {
            // Fixed Order
            for (int i = 0; i < mix.size(); i++) {
                ids.add(i);
            }
        }
        StringBuffer operationOrder = new StringBuffer();
        operationOrder.append(prefix + "Operation Order for this Run is ");
        for (int i = 0; i < ids.size(); i++) {
            operationOrder.append(ids.get(i).toString());
            if (i < ids.size() - 1)
                operationOrder.append(", ");
        }
        runner.reportProgress(options, operationOrder.toString());

        // Now run each query recording its run details
        for (Integer id : ids) {
            Operation op = mix.getOperation(id);
            runner.reportPartialProgress(options, prefix + "Running Operation " + op.getName() + "...");

            runner.reportBeforeOperation(options, op);
            mix.getStats().getTimer().start();
            OperationRun r = this.runOp(runner, options, op);
            mix.getStats().getTimer().stop();
            runner.reportAfterOperation(options, op, r);
            runs.set(id, r);
            if (r.wasSuccessful()) {
                runner.reportProgress(options, prefix + "got " + FormatUtils.formatResultCount(r.getResultCount())
                        + " result(s) in " + ConvertUtils.toSeconds(r.getRuntime()) + "s");
            } else {
                runner.reportProgress(options,
                        prefix + "got error after " + ConvertUtils.toSeconds(r.getRuntime()) + "s: " + r.getErrorMessage());
            }

            // Apply delay between operations
            if (options.getMaxDelay() > 0) {
                try {
                    long delay = (long) (Math.random() * options.getMaxDelay());
                    runner.reportProgress(
                            options,
                            prefix + "Sleeping for "
                                    + ConvertUtils.toSeconds((long) (delay * ConvertUtils.NANOSECONDS_PER_MILLISECONDS))
                                    + "s before next operation");
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    // We don't care if we get interrupted while delaying
                    // between operations
                }
            }
        }
        OperationMixRunImpl r = new OperationMixRunImpl(runs, runOrder);
        mix.getStats().add(r);
        return r;
    }
}