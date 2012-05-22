/** 
 * Copyright 2011-2012 Cray Inc. All Rights Reserved
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name Cray Inc. nor the names of its contributors may be
 *   used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 **/

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
