/**
 * Copyright 2012 Robert Vesse
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * you may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.sparql.query.benchmarking.parallel;

import java.util.concurrent.Callable;

import net.sf.sparql.query.benchmarking.Benchmarker;

/**
 * A Callable uses to manage the running of parallel clients for multi-threaded testing
 * @author rvesse
 *
 */
public class ParallelClientManager implements Callable<Object> {

	private Benchmarker b;
	private int startedRuns = 0, completedRuns = 0;
	private boolean ready = false, halt = false;
	
	/**
	 * Creates a new Parallel Client Manager
	 * @param b Benchmarker
	 */
	public ParallelClientManager(Benchmarker b)
	{
		this.b = b;
	}
	
	/**
	 * Runs the parallel clients
	 */
	@Override
	public Object call() throws Exception {
		startedRuns = 0;
		completedRuns = 0;
		ready = false;
		b.reportProgress("Parallel Client manager starting...");
		
		//Start the required number of clients, they won't start doing any work until we finish this as they
		//rely on the isReady() method to determine when to start work and it will return false until
		//after this loop
		for (int i = 1; i <= b.getParallelThreads(); i++)
		{
			ParallelClientTask task = new ParallelClientTask(this, i);
			b.getExecutor().submit(task);
			b.reportProgress("Created Parallel Client ID " + i);
		}	
		b.reportProgress("Parallel Client manager is starting clients...");
		ready = true;
		
		//Now the manager should wait until all runs have completed
		while (completedRuns < b.getRuns())
		{
			Thread.sleep(100);
		}
		
		return null;
	}
	
	/**
	 * Gets the Benchmarker
	 * @return
	 */
	public Benchmarker getBenchmarker()
	{
		return b;
	}
	
	/**
	 * Gets whether the manager is ready for clients to begin executing
	 * @return
	 */
	public boolean isReady()
	{
		return ready;
	}
	
	/**
	 * Method that will be called by parallel clients to determine if they should continue to run, calls to this are thread safe
	 * @return
	 */
	public synchronized boolean shouldRun()
	{
		if (halt) return false;
		if (startedRuns < b.getRuns())
		{
			startedRuns++;
			return true;
		}
		else
		{
			return false;
		}
	}
	
	/**
	 * Method that will be called by parallel clients to indicate they have completed a run and to obtain what run completion it is
	 * @return
	 */
	public synchronized int completeRun()
	{
		completedRuns++;
		int x = completedRuns;
		return x;
	}
	
	/**
	 * Method called by parallel clients to tell the manager that they encountered a halting condition and thus all clients should halt
	 */
	public void halt()
	{
		halt = true;
	}

}
