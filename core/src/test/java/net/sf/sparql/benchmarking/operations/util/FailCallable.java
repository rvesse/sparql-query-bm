package net.sf.sparql.benchmarking.operations.util;

import net.sf.sparql.benchmarking.operations.AbstractOperationCallable;
import net.sf.sparql.benchmarking.options.Options;
import net.sf.sparql.benchmarking.runners.Runner;
import net.sf.sparql.benchmarking.stats.OperationRun;

public class FailCallable<T extends Options> extends AbstractOperationCallable<T> {

    public FailCallable(Runner<T> runner, T options) {
        super(runner, options);
    }

    @Override
    public OperationRun call() throws Exception {
        throw new RuntimeException("Failed");
    }

}
