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
import net.sf.sparql.query.benchmarking.BenchmarkerUtils;
import net.sf.sparql.query.benchmarking.operations.BenchmarkOperationMix;
import net.sf.sparql.query.benchmarking.queries.QueryMixTask;
import net.sf.sparql.query.benchmarking.stats.OperationMixRun;

import org.apache.log4j.Logger;


/**
 * Parallel Client for running multi-threaded benchmarks
 * @author rvesse
 */
public class ParallelClient implements Callable<Object> {
	
	private static final Logger logger = Logger.getLogger(ParallelClient.class);

	private ParallelClientManager manager;
	private int id;
	
	/**
	 * Creates a new Parallel Client
	 * @param manager Client Manager
	 * @param id Client ID
	 */
	public ParallelClient(ParallelClientManager manager, int id)
	{
		this.manager = manager;
		this.id = id;
	}
	
	/**
	 * Gets the ID of this client
	 * @return ID
	 */
	public int getID()
	{
		return id;
	}
	
	/**
	 * Runs query mixes while the Client Manager indicates there are still mixes to be run
	 */
	@Override
	public Object call() throws Exception {
		Benchmarker b = manager.getBenchmarker();
		BenchmarkOperationMix queryMix = b.getQueryMix();
		
		//Firstly wait for the manager to tell us it is ready, this is to ensure all clients launch near simultaneously
		while (!manager.isReady())
		{
			Thread.sleep(50);
		}
		
		//While there is work to do run benchmarks
		while (manager.shouldRun())
		{
			try
			{
				b.reportProgress("Client " + id + " starting new query mix run");
				
				//Run a query mix
				QueryMixTask task = new QueryMixTask(b);
				b.getExecutor().submit(task);
				OperationMixRun r = task.get();

				//Report completed run
				int completedRun = manager.completeRun();
				b.reportProgress("Query Mix Run " + completedRun + " of " + b.getRuns() + " by Client " + id);
				b.reportProgress(r);
				b.reportProgress();
				b.reportProgress("Total Response Time: " + BenchmarkerUtils.formatTime(r.getTotalResponseTime()));
				b.reportProgress("Total Runtime: " + BenchmarkerUtils.formatTime(r.getTotalRuntime()));
				int minQueryId = r.getMinimumRuntimeOperationID();
				int maxQueryId = r.getMaximumRuntimeOperationID();
				b.reportProgress("Minimum Query Runtime: " + BenchmarkerUtils.formatTime(r.getMinimumRuntime()) + " (Query " + queryMix.getQuery(minQueryId).getName() + ")");
				b.reportProgress("Maximum Query Runtime: " + BenchmarkerUtils.formatTime(r.getMaximumRuntime()) + " (Query " + queryMix.getQuery(maxQueryId).getName() + ")");
				b.reportProgress();
			}
			catch (Exception e)
			{
				//Inform manager it needs to halt other clients
				manager.halt();
				
				//Log Error
				logger.error(e.getMessage());
				if (b.getHaltOnError() || b.getHaltAny())
				{
					b.halt("Query Mix run failed in Client " + id + " - " + e.getMessage());
				}
			}
		}
		return null;
	}
}
