/*
 * Copyright 2014 YarcData LLC All Rights Reserved.
 */

package net.sf.sparql.benchmarking.operations.update;

import java.util.concurrent.FutureTask;

import net.sf.sparql.benchmarking.options.Options;
import net.sf.sparql.benchmarking.stats.UpdateRun;

/**
 * A Update task that can be executed
 * 
 * @author rvesse
 * 
 * @param <T>
 *            Options type
 */
public class UpdateTask<T extends Options> extends FutureTask<UpdateRun> {

    /**
     * Creates an update task
     * 
     * @param runner
     *            Update runner
     */
    public UpdateTask(UpdateRunner<T> runner) {
        super(runner);
    }
}
