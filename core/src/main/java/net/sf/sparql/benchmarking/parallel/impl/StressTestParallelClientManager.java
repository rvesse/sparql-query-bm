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

import net.sf.sparql.benchmarking.options.StressOptions;
import net.sf.sparql.benchmarking.parallel.AbstractParallelClientManager;
import net.sf.sparql.benchmarking.parallel.ParallelClientTask;
import net.sf.sparql.benchmarking.runners.Runner;
import net.sf.sparql.benchmarking.util.ConvertUtils;

/**
 * A Callable uses to manage the running of parallel clients for stress testing
 * 
 * @author rvesse
 * 
 */
public class StressTestParallelClientManager extends
		AbstractParallelClientManager<StressOptions> {

	private int startedRuns = 0, completedRuns = 0;
	private long startTime = System.nanoTime();
	private long currentThreads = 0, currentTargetRuns = 0;

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
		super(runner, options);
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
		this.currentThreads = this.getOptions().getParallelThreads();
		this.currentTargetRuns = this.currentThreads;

		this.getRunner().reportProgress(
				this.getOptions(),
				"Spawned intial " + this.currentThreads
						+ " threads for stress testing");
		return super.call();
	}

	@Override
	public synchronized boolean shouldRun() {
		if (this.shouldHalt())
			return false;

		// Check max runtime first
		if (this.getOptions().getMaxRuntime() > 0) {
			double runtime = ConvertUtils.toMinutes(System.nanoTime()
					- this.startTime);
			if (runtime >= this.getOptions().getMaxRuntime())
				return false;
		}

		// Check if we need to spawn additional threads at this point
		// This includes the check for having reached the maximum number of
		// threads
		if (!spawnThreads())
			return false;

		// Otherwise good to go
		return true;
	}

	@Override
	public synchronized boolean startRun() {
		if (this.shouldHalt())
			return false;

		// Check max runtime first
		if (this.getOptions().getMaxRuntime() > 0) {
			double runtime = ConvertUtils.toMinutes(System.nanoTime()
					- this.startTime);
			if (runtime >= this.getOptions().getMaxRuntime())
				return false;
		}

		// Check if we need to spawn additional threads at this point
		// This includes the check for having reached the maximum number of
		// threads
		if (!spawnThreads())
			return false;

		// Otherwise good to go
		startedRuns++;
		return true;
	}

	/**
	 * Tries to spawn additional threads when necessary
	 * 
	 * @return True if no additional threads were necessary or they were
	 *         necessary and were spawned successfully, false if no additional
	 *         threads are necessary
	 */
	protected synchronized boolean spawnThreads() {
		// Check if we should halt
		if (this.shouldHalt())
			return false;

		// Not necessary to spawn additional threads at this time since we've
		// started fewer runs than our current target
		if (this.startedRuns < this.currentTargetRuns)
			return true;

		// Handle the case where max threads is set to unlimited appropriately
		long maxThreads = this.getOptions().getMaxThreads() > 0 ? this
				.getOptions().getMaxThreads() : Long.MAX_VALUE;
		if (this.currentThreads < maxThreads) {
			// Additional threads are needed
			long additionalThreads = Math.min(this.currentThreads
					* this.getOptions().getRampUpFactor(), maxThreads);
			additionalThreads = additionalThreads - this.currentThreads;

			int baseId = (int) this.currentThreads;
			this.currentThreads += additionalThreads;
			this.currentTargetRuns = this.currentTargetRuns
					+ this.currentThreads;

			// Spawn threads
			this.getRunner().reportProgress(
					this.getOptions(),
					"Spawning additional " + additionalThreads
							+ " to ramp up stress testing to a total of "
							+ this.currentThreads + " threads");
			for (int i = 1; i <= additionalThreads; i++) {
				ParallelClientTask<StressOptions> task = new ParallelClientTask<StressOptions>(
						this, baseId + i);
				this.getOptions().getExecutor().submit(task);
				this.getRunner().reportProgress(this.getOptions(),
						"Spawned additional Parallel Client ID " + i);
			}

			return true;
		}

		// Already at maximum threads
		return false;
	}

	@Override
	public synchronized int completeRun() {
		completedRuns++;
		int x = completedRuns;
		return x;
	}

	@Override
	public synchronized boolean hasFinished() {
		return !this.shouldRun() && this.completedRuns == this.startedRuns;
	}

}
