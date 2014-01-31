/*
 * Copyright 2014 YarcData LLC All Rights Reserved.
 */

package net.sf.sparql.query.benchmarking.updates;

import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
import com.hp.hpl.jena.sparql.modify.UpdateProcessRemoteBase;
import com.hp.hpl.jena.update.UpdateExecutionFactory;
import com.hp.hpl.jena.update.UpdateProcessor;
import com.hp.hpl.jena.update.UpdateRequest;

import net.sf.sparql.query.benchmarking.BenchmarkerUtils;
import net.sf.sparql.query.benchmarking.options.BenchmarkOptions;
import net.sf.sparql.query.benchmarking.options.Options;
import net.sf.sparql.query.benchmarking.runners.Runner;
import net.sf.sparql.query.benchmarking.stats.OperationRun;
import net.sf.sparql.query.benchmarking.stats.QueryRun;
import net.sf.sparql.query.benchmarking.stats.UpdateRun;

/**
 * @author rvesse
 * 
 * @param <T>
 */
public class UpdateRunner<T extends Options> implements Callable<UpdateRun> {

    private static final Logger logger = Logger.getLogger(UpdateRunner.class);
    private UpdateRequest update;
    private Runner<T> runner;
    private T options;
    private boolean cancelled = false;

    /**
     * Creates a new update runner
     * 
     * @param update
     *            Update to run
     * @param runner
     *            Runner
     * @param options
     *            Options
     */
    public UpdateRunner(UpdateRequest update, Runner<T> runner, T options) {
        this.update = update;
        this.runner = runner;
        this.options = options;
    }

    @Override
    public UpdateRun call() throws Exception {
        BenchmarkOptions bOps = null;
        if (this.options instanceof BenchmarkOptions) {
            bOps = (BenchmarkOptions) this.options;
        }

        // Create a remote update processor and configure it appropriately
        UpdateProcessRemoteBase processor = (UpdateProcessRemoteBase) UpdateExecutionFactory.createRemote(this.update,
                this.options.getUpdateEndpoint());
        if (this.options.getAuthenticator() != null) {
            processor.setAuthenticator(this.options.getAuthenticator());
        }
        long startTime = System.nanoTime();

        // Execute the update
        processor.execute();

        if (cancelled)
            return null;

        long endTime = System.nanoTime();
        return new UpdateRun(endTime - startTime);
    }

    /**
     * Cancels the runner
     */
    public void cancel() {
        cancelled = true;
    }

}
