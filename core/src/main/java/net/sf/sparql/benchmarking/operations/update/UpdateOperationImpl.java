/*
 * Copyright 2014 YarcData LLC All Rights Reserved.
 */

package net.sf.sparql.benchmarking.operations.update;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateRequest;

import net.sf.sparql.benchmarking.operations.AbstractOperation;
import net.sf.sparql.benchmarking.operations.query.QueryOperationImpl;
import net.sf.sparql.benchmarking.options.Options;
import net.sf.sparql.benchmarking.runners.Runner;
import net.sf.sparql.benchmarking.stats.OperationRun;
import net.sf.sparql.benchmarking.stats.QueryRun;
import net.sf.sparql.benchmarking.stats.UpdateRun;

/**
 * An operation that makes a SPARQL Update
 * 
 * @author rvesse
 * 
 */
public class UpdateOperationImpl extends AbstractOperation implements UpdateOperation {

    private static final Logger logger = Logger.getLogger(QueryOperationImpl.class);
    private UpdateRequest update;
    private String origUpdateStr;

    /**
     * Creates a new update operation
     * 
     * @param name
     *            Name
     * @param updateString
     *            SPARQL Update
     */
    public UpdateOperationImpl(String name, String updateString) {
        super(name);
        this.origUpdateStr = updateString;
        this.update = UpdateFactory.create(updateString);
    }

    @Override
    public <T extends Options> boolean canRun(Runner<T> runner, T options) {
        if (options.getQueryEndpoint() == null) {
            runner.reportProgress(options, "Benchmark Updates cannot run with no query endpoint specified");
            return false;
        }
        return true;
    }

    @Override
    public <T extends Options> OperationRun run(Runner<T> runner, T options) {
        timer.start();
        long order = options.getGlobalOrder();
        UpdateRunner<T> updateRunner = new UpdateRunner<T>(this.update, runner, options);
        UpdateTask<T> task = new UpdateTask<T>(updateRunner);
        options.getExecutor().submit(task);
        OperationRun r;
        long startTime = System.nanoTime();
        try {
            r = task.get(options.getTimeout(), TimeUnit.SECONDS);
        } catch (TimeoutException tEx) {
            logger.error("Update Runner execeeded Timeout - " + tEx.getMessage());
            if (options.getHaltOnTimeout() || options.getHaltAny())
                runner.halt(options, tEx);
            r = new UpdateRun("Update Runner execeeded Timeout - " + tEx.getMessage(), System.nanoTime() - startTime);

            // If the query times out but we aren't halting cancel further
            // evaluation of the query
            task.cancel(true);
            updateRunner.cancel();
        } catch (InterruptedException e) {
            logger.error("Update Runner was interrupted - " + e.getMessage());
            if (options.getHaltAny())
                runner.halt(options, e);
            r = new UpdateRun("Update Runner was interrupted - " + e.getMessage(), System.nanoTime() - startTime);
        } catch (ExecutionException e) {
            logger.error("Update Runner encountered an error - " + e.getMessage());

            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            logger.error(sw.toString());

            if (options.getHaltOnError() || options.getHaltAny())
                runner.halt(options, e);
            r = new QueryRun("Update Runner encountered an error - " + e.getMessage(), System.nanoTime() - startTime);
        }
        timer.stop();
        this.addRun(r);
        r.setRunOrder(order);
        return r;
    }

    @Override
    public String getType() {
        return "SPARQL Update";
    }

    @Override
    public String getContentString() {
        return this.getUpdateString();
    }

    @Override
    public UpdateRequest getUpdate() {
        return this.update;
    }

    @Override
    public String getUpdateString() {
        return this.origUpdateStr;
    }
}
