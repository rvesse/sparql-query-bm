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

package net.sf.sparql.benchmarking.parallel;

import java.util.concurrent.Callable;

import net.sf.sparql.benchmarking.operations.OperationMix;
import net.sf.sparql.benchmarking.options.Options;
import net.sf.sparql.benchmarking.runners.OperationMixTask;
import net.sf.sparql.benchmarking.runners.Runner;
import net.sf.sparql.benchmarking.stats.OperationMixRun;
import net.sf.sparql.benchmarking.util.FormatUtils;

import org.apache.log4j.Logger;

/**
 * Parallel Client for running multi-threaded benchmarks
 * 
 * @author rvesse
 * @param <T>
 *            Options type
 */
public class ParallelClient<T extends Options> implements Callable<Object> {

    // TODO Extract an interface and abstract class to make exact client run
    // behaviour configurable

    private static final Logger logger = Logger.getLogger(ParallelClient.class);

    private ParallelClientManager<T> manager;
    private int id;

    /**
     * Creates a new Parallel Client
     * 
     * @param manager
     *            Client Manager
     * @param id
     *            Client ID
     */
    public ParallelClient(ParallelClientManager<T> manager, int id) {
        this.manager = manager;
        this.id = id;
    }

    /**
     * Gets the ID of this client
     * 
     * @return ID
     */
    public int getID() {
        return id;
    }

    /**
     * Runs operation mixes while the Client Manager indicates there are still
     * mixes to be run
     */
    @Override
    public Object call() throws Exception {
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
            try {
                runner.reportProgress(options, "Client " + id + " starting new operation mix run");

                // Run a query mix
                OperationMixTask<T> task = new OperationMixTask<T>(runner, options);
                options.getExecutor().submit(task);
                OperationMixRun r = task.get();

                // Report completed run
                int completedRun = manager.completeRun();
                runner.reportProgress(options, "Operation Mix Run " + completedRun + " by Client " + id);
                runner.reportProgress(options, r);
                runner.reportProgress(options);
                runner.reportProgress(options, "Total Response Time: " + FormatUtils.formatSeconds(r.getTotalResponseTime()));
                runner.reportProgress(options, "Total Runtime: " + FormatUtils.formatSeconds(r.getTotalRuntime()));
                int minOperationId = r.getMinimumRuntimeOperationID();
                int maxOperationId = r.getMaximumRuntimeOperationID();
                runner.reportProgress(options,
                        "Minimum Operation Runtime: " + FormatUtils.formatSeconds(r.getMinimumRuntime()) + " (Operation "
                                + operationMix.getOperation(minOperationId).getName() + ")");
                runner.reportProgress(options,
                        "Maximum Operation Runtime: " + FormatUtils.formatSeconds(r.getMaximumRuntime()) + " (Operation "
                                + operationMix.getOperation(maxOperationId).getName() + ")");
                runner.reportProgress(options);
            } catch (Exception e) {
                // Inform manager it needs to halt other clients
                manager.halt();

                // Log Error
                logger.error(e.getMessage());
                if (options.getHaltOnError() || options.getHaltAny()) {
                    runner.halt(options, "Operation Mix run failed in Client " + id + " - " + e.getMessage());
                }
            }
        }
        return null;
    }
}
