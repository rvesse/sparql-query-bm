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

package net.sf.sparql.benchmarking.runners.operations;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.sparql.benchmarking.operations.Operation;
import net.sf.sparql.benchmarking.operations.OperationCallable;
import net.sf.sparql.benchmarking.options.Options;
import net.sf.sparql.benchmarking.runners.Runner;
import net.sf.sparql.benchmarking.stats.OperationRun;
import net.sf.sparql.benchmarking.util.ErrorCategories;

/**
 * Default implementation of an operation runner
 * 
 * @author rvesse
 * 
 */
public class DefaultOperationRunner implements OperationRunner {

    private static final Logger logger = LoggerFactory.getLogger(DefaultOperationRunner.class);

    @Override
    public <T extends Options> OperationRun run(Runner<T> runner, T options, Operation op) {
        op.getStats().getTimer().start();
        long order = options.getGlobalOrder();

        // Prepare and submit the task
        OperationCallable<T> callable = op.createCallable(runner, options);
        FutureTask<OperationRun> task = new FutureTask<OperationRun>(callable);
        options.getExecutor().submit(task);

        OperationRun r;
        long startTime = System.nanoTime();
        try {
            // Wait for the operation to complete
            if (options.getTimeout() > 0) {
                // Enforce a timeout on the operation
                r = task.get(options.getTimeout(), TimeUnit.SECONDS);
            } else {
                // No timeout on the operations
                r = task.get();
            }
        } catch (TimeoutException tEx) {
            // Handle timeout error
            logger.error("Operation Callable execeeded Timeout - " + tEx.getMessage());
            if (options.getHaltOnTimeout() || options.getHaltAny())
                runner.halt(options, tEx);
            r = op.createErrorInformation("Operation Callable execeeded Timeout - " + tEx.getMessage(), ErrorCategories.TIMEOUT,
                    System.nanoTime() - startTime);

            // If the query times out but we aren't halting cancel further
            // evaluation of the operation
            callable.cancel();
            task.cancel(true);
        } catch (InterruptedException e) {
            // Handle interrupted error
            logger.error("Operation Callable was interrupted - " + e.getMessage());
            if (options.getHaltAny())
                runner.halt(options, e);
            r = op.createErrorInformation("Operation Callable was interrupted - " + e.getMessage(), ErrorCategories.INTERRUPT,
                    System.nanoTime() - startTime);
        } catch (ExecutionException e) {
            // Handle unexpected execution error
            logger.error("Operation Callable encountered an error - " + e.getMessage());

            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            logger.error(sw.toString());

            if (options.getHaltOnError() || options.getHaltAny())
                runner.halt(options, e);
            r = op.createErrorInformation("Operation Callable encountered an error - " + e.getMessage(),
                    ErrorCategories.EXECUTION, System.nanoTime() - startTime);
        }
        op.getStats().getTimer().stop();

        // In the event that an authentication error has been reported force an
        // invalidation of the authenticator
        if (!r.wasSuccessful() && r.getErrorCategory() == ErrorCategories.AUTHENTICATION) {
            if (options.getAuthenticator() != null) {
                options.getAuthenticator().invalidate();
            }
        }

        // Return the results
        r.setRunOrder(order);
        op.getStats().add(r);
        return r;
    }

}
