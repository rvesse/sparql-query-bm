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

package net.sf.sparql.benchmarking.parallel.impl;

import net.sf.sparql.benchmarking.operations.OperationMix;
import net.sf.sparql.benchmarking.options.Options;
import net.sf.sparql.benchmarking.parallel.AbstractParallelClient;
import net.sf.sparql.benchmarking.parallel.ParallelClientManager;
import net.sf.sparql.benchmarking.runners.Runner;
import net.sf.sparql.benchmarking.stats.OperationMixRun;
import net.sf.sparql.benchmarking.util.FormatUtils;

import org.joda.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default parallel client for running multi-threaded testing
 * 
 * @author rvesse
 * @param <T>
 *            Options type
 */
public class DefaultParallelClient<T extends Options> extends AbstractParallelClient<T> {

    private static final Logger logger = LoggerFactory.getLogger(DefaultParallelClient.class);

    /**
     * Creates a new Parallel Client
     * 
     * @param manager
     *            Client Manager
     * @param id
     *            Client ID
     */
    public DefaultParallelClient(ParallelClientManager<T> manager, int id) {
        super(manager, id);
    }

    @Override
    public Object call() throws Exception {
    	Thread.currentThread().setName("Parallel Client " + this.getID());
    	
        ParallelClientManager<T> manager = this.getManager();
        T options = manager.getOptions();
        Runner<T> runner = manager.getRunner();
        OperationMix operationMix = options.getOperationMix();

        // Firstly wait for the manager to tell us it is ready, this is to
        // ensure all clients launch near simultaneously
        while (!manager.isReady()) {
            Thread.sleep(50);
        }

        // While there is work to do run benchmarks
        while (manager.shouldRun()) {
            // Check we should actually start a run
            if (!manager.startRun())
                continue;

            try {
                runner.reportProgress(options, "Client " + this.getID() + " starting new operation mix run");
                runner.reportProgress(options, "Current Time: " + FormatUtils.formatInstant(Instant.now()));

                // Run a query mix
                runner.reportBeforeOperationMix(options, operationMix);
                OperationMixTask<T> task = new OperationMixTask<T>(runner, options);
                options.getExecutor().submit(task);
                OperationMixRun r = task.get();

                // Report completed run
                int completedRun = manager.completeRun();
                runner.reportProgress(options, "Operation Mix Run " + completedRun + " by Client " + this.getID());
                runner.reportAfterOperationMix(options, operationMix, r);
                runner.reportProgress(options);
                runner.reportProgress(options, "Total Response Time: " + FormatUtils.formatSeconds(r.getTotalResponseTime()));
                runner.reportProgress(options, "Total Runtime: " + FormatUtils.formatSeconds(r.getTotalRuntime()));
                int minOperationId = r.getMinimumRuntimeOperationID();
                int maxOperationId = r.getMaximumRuntimeOperationID();
                runner.reportProgress(options, "Minimum Operation Runtime: " + FormatUtils.formatSeconds(r.getMinimumRuntime())
                        + " (Operation " + operationMix.getOperation(minOperationId).getName() + ")");
                runner.reportProgress(options, "Maximum Operation Runtime: " + FormatUtils.formatSeconds(r.getMaximumRuntime())
                        + " (Operation " + operationMix.getOperation(maxOperationId).getName() + ")");
                runner.reportProgress(options);
            } catch (Exception e) {
                // Log Error
                logger.error(FormatUtils.formatException(e));
                if (options.getHaltOnError() || options.getHaltAny()) {
                    // Inform manager it needs to halt other clients
                    manager.halt();
                	
                    runner.halt(options, "Operation Mix run failed in Client " + this.getID() + " - " + e.getMessage());
                }
            }
        }
        return null;
    }
}
