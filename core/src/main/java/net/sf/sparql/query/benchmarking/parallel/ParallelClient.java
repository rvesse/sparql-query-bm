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
import net.sf.sparql.query.benchmarking.BenchmarkerUtils;
import net.sf.sparql.query.benchmarking.queries.BenchmarkQueryMix;
import net.sf.sparql.query.benchmarking.queries.QueryMixTask;
import net.sf.sparql.query.benchmarking.stats.QueryMixRun;

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
		BenchmarkQueryMix queryMix = b.getQueryMix();
		
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
				QueryMixRun r = task.get();

				//Report completed run
				int completedRun = manager.completeRun();
				b.reportProgress("Query Mix Run " + completedRun + " of " + b.getRuns() + " by Client " + id);
				b.reportProgress(r);
				b.reportProgress();
				b.reportProgress("Total Response Time: " + BenchmarkerUtils.formatTime(r.getTotalResponseTime()));
				b.reportProgress("Total Runtime: " + BenchmarkerUtils.formatTime(r.getTotalRuntime()));
				int minQueryId = r.getMinimumRuntimeQueryID();
				int maxQueryId = r.getMaximumRuntimeQueryID();
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
