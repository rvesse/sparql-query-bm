/*
 * Copyright 2014 YarcData LLC All Rights Reserved.
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
 * An operation mix runner which runs all the operations in the mix in their
 * exact order
 * 
 * @author rvesse
 * 
 */
public class InOrderOperationMixRunner implements OperationMixRunner {

    private boolean asThread = false;

    @Override
    public void setRunAsThread(boolean asThread) {
        this.asThread = asThread;
    }

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

        // Now run each query recording its run details
        for (int id = 0; id < mix.size(); id++) {
            Operation op = mix.getOperation(id);
            runner.reportPartialProgress(options, prefix + "Running Operation " + op.getName() + "...");

            runner.reportBeforeOperation(options, op);
            mix.getStats().getTimer().start();
            OperationRun r = op.run(runner, options);
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

        // Report statistics
        OperationMixRunImpl r = new OperationMixRunImpl(runs, runOrder);
        mix.getStats().add(r);
        return r;
    }
}
