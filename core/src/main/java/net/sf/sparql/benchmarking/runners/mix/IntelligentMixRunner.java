package net.sf.sparql.benchmarking.runners.mix;

import java.util.Iterator;
import java.util.Set;

import net.sf.sparql.benchmarking.operations.Operation;
import net.sf.sparql.benchmarking.operations.OperationMix;
import net.sf.sparql.benchmarking.options.Options;
import net.sf.sparql.benchmarking.runners.Runner;
import net.sf.sparql.benchmarking.runners.mix.ordering.MixOrderProvider;
import net.sf.sparql.benchmarking.stats.OperationMixRun;
import net.sf.sparql.benchmarking.stats.OperationRun;
import net.sf.sparql.benchmarking.util.ConvertUtils;
import net.sf.sparql.benchmarking.util.ErrorCategories;

public class IntelligentMixRunner extends AbstractOperationMixRunner {

    private boolean inWarmups = false;
    private ThreadLocal<Boolean> runningWarmup = new ThreadLocal<>();
    private final long failureThreshold;
    private final double timeoutTuningFactor;
    private final MixOrderProvider orderProvider;

    public IntelligentMixRunner(MixOrderProvider provider, long failureThreshold, double timeoutTuningFactor) {
        super(provider);
        this.orderProvider = provider;
        this.failureThreshold = failureThreshold;
        if (timeoutTuningFactor <= 1.0d)
            throw new IllegalArgumentException("timeoutTuningFactor must be > 1.0");
        this.timeoutTuningFactor = timeoutTuningFactor;
    }

    @Override
    public <T extends Options> OperationMixRun run(Runner<T> runner, T options, OperationMix mix) {
        boolean calledFromWarmup = this.runningWarmup.get() != null && this.runningWarmup.get();
        if (this.inWarmups && !calledFromWarmup) {
            // Moved from warm-ups to actual runs so apply timeout tuning if
            // applicable
            if (options.getTimeout() > 0) {

                double maxRuntime = 0d;
                Set<Integer> excludes = this.orderProvider.getOperationExcludes(options);
                Iterator<Operation> ops = mix.getOperations();
                while (ops.hasNext()) {
                    Operation op = ops.next();
                    if (excludes.contains(op.getId()))
                        continue;
                    maxRuntime = Math.max(maxRuntime, op.getStats().getMaximumRuntime());
                }

                int tunedTimeout = (int) ConvertUtils.toSeconds(maxRuntime * this.timeoutTuningFactor);
                if (tunedTimeout > 0 && tunedTimeout < options.getTimeout()) {
                    runner.reportProgress(options, String
                            .format("Intelligent mix runner tuned the Operation Timeout to %,d seconds", tunedTimeout));
                    options.setTimeout(tunedTimeout);
                }
            }

            // No longer in warm-ups
            this.inWarmups = false;
        }
        OperationMixRun run = super.run(runner, options, mix);

        if (!calledFromWarmup) {
            // Adjust mix accordingly
            Iterator<OperationRun> iter = run.getRuns();
            Set<Integer> excludes = this.orderProvider.getOperationExcludes(options);
            while (iter.hasNext()) {
                OperationRun opRun = iter.next();

                // Don't bother assessing operations we've already marked for
                // exclusion
                if (excludes.contains(opRun.getId()))
                    continue;

                if (!opRun.wasSuccessful()) {
                    // Can't remove operations based on failures if the failure
                    // threshold was disabled
                    if (this.failureThreshold < 0)
                        continue;

                    // For any other error has it exceeded the error
                    // threshold
                    checkFailureThreshold(runner, options, mix, excludes, opRun);
                }
            }
        }

        return run;
    }

    @Override
    public <T extends Options> OperationMixRun warmup(Runner<T> runner, T options, OperationMix mix) {
        if (!this.inWarmups) {
            // Starting warm-ups
            // Clear any previously determined excludes
            this.orderProvider.getOperationExcludes(options).clear();
            this.inWarmups = true;
        }

        OperationMixRun run;
        try {
            this.runningWarmup.set(true);
            run = super.warmup(runner, options, mix);
        } finally {
            this.runningWarmup.set(false);
        }

        // Adjust mix accordingly
        Iterator<OperationRun> iter = run.getRuns();
        Set<Integer> excludes = this.orderProvider.getOperationExcludes(options);
        while (iter.hasNext()) {
            OperationRun opRun = iter.next();

            // Don't bother assessing operations we've already marked for
            // exclusion
            if (excludes.contains(opRun.getId()))
                continue;

            if (!opRun.wasSuccessful()) {
                switch (opRun.getErrorCategory()) {
                case ErrorCategories.TIMEOUT:
                    // Remove
                    runner.reportProgress(options,
                            String.format("Intelligent Mix Runner removed Operation ID %d (%s) as it timed out",
                                    opRun.getId(), mix.getOperation(opRun.getId()).getName()));
                    excludes.add(opRun.getId());
                    break;
                default:
                    // Can't remove operations based on failures if the failure
                    // threshold was disabled
                    if (this.failureThreshold < 0)
                        continue;

                    // For any other error has it exceeded the error
                    // threshold
                    checkFailureThreshold(runner, options, mix, excludes, opRun);
                }
            }
        }

        return run;
    }

    protected <T extends Options> void checkFailureThreshold(Runner<T> runner, T options, OperationMix mix,
            Set<Integer> excludes, OperationRun opRun) {
        long failures = mix.getOperation(opRun.getId()).getStats().getTotalErrors();
        if (failures >= this.failureThreshold) {
            runner.reportProgress(options,
                    String.format(
                            "Intelligent Mix Runner removed Operation ID %d (%s) as it exceeded the failure rate of %d",
                            opRun.getId(), mix.getOperation(opRun.getId()).getName(), this.failureThreshold));
            excludes.add(opRun.getId());
        }
    }

}
