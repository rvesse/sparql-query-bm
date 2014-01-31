/*
 * Copyright 2014 YarcData LLC All Rights Reserved.
 */ 

package net.sf.sparql.query.benchmarking.updates;

import java.util.concurrent.FutureTask;

import net.sf.sparql.query.benchmarking.options.Options;
import net.sf.sparql.query.benchmarking.stats.UpdateRun;

public class UpdateTask<T extends Options> extends FutureTask<UpdateRun> {

    public UpdateTask(UpdateRunner<T> runner) {
        super(runner);
    }
}
