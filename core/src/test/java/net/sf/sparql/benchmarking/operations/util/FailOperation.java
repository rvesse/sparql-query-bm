package net.sf.sparql.benchmarking.operations.util;

import net.sf.sparql.benchmarking.operations.AbstractOperation;
import net.sf.sparql.benchmarking.operations.OperationCallable;
import net.sf.sparql.benchmarking.options.Options;
import net.sf.sparql.benchmarking.runners.Runner;
import net.sf.sparql.benchmarking.stats.OperationRun;
import net.sf.sparql.benchmarking.stats.impl.OperationRunImpl;

public class FailOperation extends AbstractOperation {

    private final int maxFails;
    private int fails = 0;

    public FailOperation(String name, int maxFails) {
        super(name);
        this.maxFails = maxFails;
    }

    @Override
    public <T extends Options> boolean canRun(Runner<T> runner, T options) {
        return true;
    }

    @Override
    public <T extends Options> OperationCallable<T> createCallable(Runner<T> runner, T options) {
        if (this.fails < this.maxFails) {
            this.fails++;
            return new FailCallable<T>(runner, options);
        } else {
            return new NoOpCallable<T>(runner, options, new OperationRunImpl(0));
        }
    }

    @Override
    public OperationRun createErrorInformation(String message, int category, long runtime) {
        return new OperationRunImpl(message, category, runtime);
    }

    @Override
    public String getType() {
        return "Failure";
    }

    @Override
    public String getContentString() {
        return "Failure";
    }

}
