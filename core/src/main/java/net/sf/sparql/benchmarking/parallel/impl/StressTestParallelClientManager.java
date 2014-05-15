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

import java.util.HashSet;
import java.util.Set;

import net.sf.sparql.benchmarking.options.StressOptions;
import net.sf.sparql.benchmarking.parallel.ParallelClient;
import net.sf.sparql.benchmarking.parallel.ParallelClientManager;
import net.sf.sparql.benchmarking.parallel.ParallelClientTask;
import net.sf.sparql.benchmarking.runners.Runner;
import net.sf.sparql.benchmarking.util.ConvertUtils;

/**
 * A Callable uses to manage the running of parallel clients for stress testing
 * 
 * @author rvesse
 * 
 */
public class StressTestParallelClientManager implements
		ParallelClientManager<StressOptions> {

	private Runner<StressOptions> runner;
	private StressOptions options;
	private long startTime = System.nanoTime();
	private int currentThreads = 0;
	private volatile boolean ready = false;
	private boolean halt = false;
	private Set<Long> runningClients = new HashSet<Long>();
	private int completedRuns = 0;

	/**
	 * Creates a new Parallel Client Manager
	 * 
	 * @param runner
	 *            Benchmark runner
	 * @param options
	 *            Options
	 */
	public StressTestParallelClientManager(Runner<StressOptions> runner,
			StressOptions options) {
		this.runner = runner;
		this.options = options;
	}

	/**
	 * Gets the current number of parallel clients in-use
	 * 
	 * @return Current number of parallel clients
	 */
	public long getCurrentClientCount() {
		return this.currentThreads;
	}

	@Override
	public Object call() throws Exception {
		this.runner.reportProgress(this.options,
				"Parallel Client manager starting...");

		this.currentThreads = this.options.getParallelThreads();
		int maxThreads = this.options.getMaxThreads() > 0 ? this.options
				.getMaxThreads() : Integer.MAX_VALUE;

		while (this.currentThreads <= maxThreads && !this.exceededMaxRuntime()) {
			this.runner
					.reportProgress(
							this.options,
							"Starting a run with "
									+ Math.min(this.currentThreads, maxThreads)
									+ " clients...");

			// Start the required number of clients, they won't start doing any
			// work until we finish this as they rely on the isReady() method to
			// determine when to start work and it will return false until after
			// this loop
			for (int i = 1; i <= Math.min(this.currentThreads, maxThreads); i++) {
				ParallelClientTask<StressOptions> task = new ParallelClientTask<StressOptions>(
						this, i);
				this.options.getExecutor().submit(task);
				this.runner.reportProgress(this.options,
						"Created Parallel Client ID " + i);
			}
			this.runner.reportProgress(this.options,
					"Parallel Client manager is starting clients...");
			this.ready = true;

			// And then wait until all runs have finished
			while (!this.hasFinished() && !this.halt) {
				Thread.sleep(100);
			}
			this.runner.reportProgress(this.options, "Completed a run with "
					+ this.currentThreads + " clients...");

			// Now increase the amount of threads appropriately
			this.currentThreads *= this.options.getRampUpFactor();
			synchronized (this.runningClients) {
				this.runningClients.clear();
			}
		}

		return null;
	}

	/**
	 * Gets whether the maximum run time has been exceeded
	 * 
	 * @return True if maximum runtime has been exceeded
	 */
	protected boolean exceededMaxRuntime() {
		double runtime = ConvertUtils.toMinutes(System.nanoTime()
				- this.startTime);
		return runtime >= this.getOptions().getMaxRuntime();
	}

	@Override
	public boolean shouldRun() {
		if (this.halt)
			return false;
		if (this.exceededMaxRuntime())
			return false;
		synchronized (this.runningClients) {
			if (this.runningClients.contains(Thread.currentThread().getId()))
				return false;
		}
		return true;
	}

	@Override
	public boolean startRun() {
		return shouldRun();
	}

	@Override
	public int completeRun() {
		synchronized (this.runningClients) {
			int completionId = ++this.completedRuns;
			this.runningClients.add(Thread.currentThread().getId());
			return completionId;
		}
	}

	@Override
	public boolean hasFinished() {
		return this.exceededMaxRuntime()
				|| this.runningClients.size() == this.currentThreads;
	}

	@Override
	public void halt() {
		this.halt = true;
	}

	@Override
	public boolean isReady() {
		return this.ready;
	}

	@Override
	public Runner<StressOptions> getRunner() {
		return this.runner;
	}

	@Override
	public StressOptions getOptions() {
		return this.options;
	}

	@Override
	public ParallelClient<StressOptions> createClient(int id) {
		return new DefaultParallelClient<StressOptions>(this, id);
	}

}
