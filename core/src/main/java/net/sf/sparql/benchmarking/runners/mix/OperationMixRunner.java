/*
 * Copyright 2014 YarcData LLC All Rights Reserved.
 */

package net.sf.sparql.benchmarking.runners.mix;

import net.sf.sparql.benchmarking.operations.OperationMix;
import net.sf.sparql.benchmarking.options.Options;
import net.sf.sparql.benchmarking.runners.Runner;
import net.sf.sparql.benchmarking.stats.OperationMixRun;
import net.sf.sparql.benchmarking.stats.OperationMixStats;

/**
 * Interface for runners which can run operation mixes
 * 
 * @author rvesse
 * 
 */
public interface OperationMixRunner {

    /**
     * Sets whether the operation mix is being run as a thread, if so it should
     * prefix some thread identifier to its progress messages
     * 
     * @param asThread
     *            Whether the operation mix is being run as a thread
     */
    public abstract void setRunAsThread(boolean asThread);

    /**
     * Performs a operation mix run returning the statistics as a
     * {@link OperationMixRun}
     * <p>
     * Implementations are also expected to record the information within the
     * {@link OperationMixStats} object of the given {@link OperationMix}
     * instance prior to returning the statistics.
     * </p>
     * 
     * @param runner
     *            Runner
     * @param options
     *            Options
     * @param mix
     *            Operation mix to be run
     * @return Operation Mix run details
     */
    public abstract <T extends Options> OperationMixRun run(Runner<T> runner, T options, OperationMix mix);
}
