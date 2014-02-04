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

package net.sf.sparql.benchmarking.runners;

import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;

import net.sf.sparql.benchmarking.monitoring.ProgressListener;
import net.sf.sparql.benchmarking.operations.Operation;
import net.sf.sparql.benchmarking.operations.query.QueryCallable;
import net.sf.sparql.benchmarking.options.Options;
import net.sf.sparql.benchmarking.stats.OperationMixRun;
import net.sf.sparql.benchmarking.stats.OperationRun;
import net.sf.sparql.benchmarking.stats.QueryRun;

/**
 * Abstract implementation of a runner providing common halting and progress
 * reporting functionality
 * 
 * @author rvesse
 * 
 * @param <T>
 */
public abstract class AbstractRunner<T extends Options> implements Runner<T> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractRunner.class);

    private boolean halted = false;

    @Override
    public void halt(T options, String message) {
        System.err.println("Benchmarking Aborted - Halting due to " + message);
        if (!halted) {
            // Make sure we only reallyHalt once, otherwise, we infinite loop
            // with bad behavior from a listener.
            halted = true;
            reallyHalt(options, message);
        }
    }

    /**
     * Helper method that ensures we really halt without going into an infinite
     * loop
     * 
     * @param options
     *            Options
     * @param message
     *            Message
     */
    private void reallyHalt(T options, String message) {
        // Inform Listeners that Benchmarking Finished with a halt condition
        for (ProgressListener l : options.getListeners()) {
            try {
                l.handleFinished(this, options, false);
            } catch (Exception e) {
                System.err.println(l.getClass().getName() + " encountered an error during handleFinish() - " + e.getMessage());
                if (options.getHaltOnError() || options.getHaltAny()) {
                    halt(options, l.getClass().getName() + " encountering an error during finish");
                }
            }
        }

        // Then perform actual halting depending on configured behaviour
        switch (options.getHaltBehaviour()) {
        case EXIT:
            System.exit(2);
        case THROW_EXCEPTION:
            throw new RuntimeException("Benchmarking Aborted - Halting due to " + message);
        }
    }

    @Override
    public void halt(T options, Exception e) {
        halt(options, e.getMessage());
    }

    @Override
    public void reportProgress(T options) {
        this.reportPartialProgress(options, "\n");
    }

    @Override
    public void reportPartialProgress(T options, String message) {
        for (ProgressListener l : options.getListeners()) {
            try {
                l.handleProgress(this, options, message);
            } catch (Exception e) {
                System.err.println(l.getClass().getName() + " encountered an error during handleProgress() - " + e.getMessage());
                if (options.getHaltAny() || options.getHaltOnError()) {
                    halt(options, l.getClass().getName() + " encountering an error in progress reporting");
                }
            }
        }
    }

    @Override
    public void reportProgress(T options, String message) {
        this.reportPartialProgress(options, message + '\n');
    }

    @Override
    public void reportProgress(T options, Operation operation, OperationRun run) {
        for (ProgressListener l : options.getListeners()) {
            try {
                l.handleProgress(this, options, operation, run);
            } catch (Exception e) {
                System.err.println(l.getClass().getName() + " encountered an error during handleProgress() - " + e.getMessage());
                if (options.getHaltAny() || options.getHaltOnTimeout()) {
                    halt(options, l.getClass().getName() + " encountering an error in progress reporting");
                }
            }
        }
    }

    @Override
    public void reportProgress(T options, OperationMixRun run) {
        for (ProgressListener l : options.getListeners()) {
            try {
                l.handleProgress(this, options, run);
            } catch (Exception e) {
                System.err.println(l.getClass().getName() + " encountered an error during handleProgress() - " + e.getMessage());
                if (options.getHaltAny() || options.getHaltOnError()) {
                    halt(options, l.getClass().getName() + " encountering an error in progress reporting");
                }
            }
        }
    }

    /**
     * Checks that the query endpoint being used passes some basic queries to
     * see if it is up and running
     * <p>
     * May be overridden by runner implementations to change the sanity checking
     * constraints
     * </p>
     * 
     * @param options
     *            Options
     * 
     * @return Whether the endpoint passed some basic sanity checks
     */
    protected boolean checkSanity(T options) {
        reportProgress(options, "Sanity checking the user specified endpoint...");
        String[] checks = new String[] { "ASK WHERE { }", "SELECT * WHERE { }", "SELECT * WHERE { ?s a ?type } LIMIT 1" };

        int passed = 0;
        for (int i = 0; i < checks.length; i++) {
            Query q = QueryFactory.create(checks[i]);
            FutureTask<QueryRun> task = new FutureTask<QueryRun>(new QueryCallable<T>(q, this, options));
            reportPartialProgress(options, "Sanity Check " + (i + 1) + " of " + checks.length + "...");
            try {
                options.getExecutor().submit(task);
                task.get(options.getTimeout(), TimeUnit.SECONDS);
                reportProgress(options, "OK");
                passed++;
            } catch (TimeoutException tEx) {
                logger.error("Query Runner execeeded Timeout - " + tEx.getMessage());
                reportProgress(options, "Failed");
            } catch (InterruptedException e) {
                logger.error("Query Runner was interrupted - " + e.getMessage());
                reportProgress(options, "Failed");
            } catch (ExecutionException e) {
                logger.error("Query Runner encountered an error - " + e.getMessage());
                reportProgress(options, "Failed");
            }
        }

        return (passed >= options.getSanityCheckLevel());
    }

    protected void checkOperations(T options) {
        Iterator<Operation> ops = options.getOperationMix().getOperations();
        while (ops.hasNext()) {
            Operation op = ops.next();
            if (!op.canRun(this, options)) {
                System.err.println("A specified operation cannot run with the available options");
                halt(options, "Operation " + op.getName() + " of type " + op.getType()
                        + " cannot run with the available options");
            }
        }
    }

}