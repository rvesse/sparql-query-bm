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

package net.sf.sparql.query.benchmarking.stats;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a run of a Query Mix which is comprised of running each query once
 * @author rvesse
 *
 */
public class QueryMixRun implements Comparable<QueryMixRun> {

	private List<QueryRun> runs;
	private long order = 0;
	
	/**
	 * Creates a new Query Mix run which represents the results of running a mix of queries
	 * @param numQueries Number of Queries that will be executed in this run
	 * @param order Global Run Order
	 */
	public QueryMixRun(int numQueries, long order)
	{
		this.runs = new ArrayList<QueryRun>(numQueries);
		for (int i = 0; i < numQueries; i++)
		{
			this.runs.add(null);
		}
		this.order = order;
	}
	
	/**
	 * Sets the Stats for the run of a particular query within this mix run
	 * @param queryId Query ID
	 * @param run Run stats
	 */
	public void setRunStats(int queryId, QueryRun run)
	{
		this.runs.set(queryId, run);
	}

	/**
	 * Gets an iterator over the runs that make up this query mix
	 * <p>
	 * The runs are in the same order as the queries are in the originating mix i.e. the order does not reflect the execution order
	 * </p>
	 * @return Iterator over the runs
	 */
	public Iterator<QueryRun> getRuns()
	{
		return this.runs.iterator();
	}
	
	/**
	 * Gets the global run order that reflects the order in which the query mixes and queries were run
	 * @return
	 */
	public long getRunOrder()
	{
		return this.order;
	}
	
	/**
	 * Gets the total runtime for the Query Mix
	 * @return Total Runtime in nanoseconds
	 */
	public long getTotalRuntime()
	{
		Iterator<QueryRun> rs = this.getRuns();
		long total = 0;
		while (rs.hasNext())
		{
			QueryRun r = rs.next();
			if (r != null) 
			{
				if (r.getRuntime() == Long.MAX_VALUE) return Long.MAX_VALUE;
				total += r.getRuntime();
			}
		}
		return total;
	}
	
	/**
	 * Gets the total response time for the Query Mix
	 * @return Total Response Time in nanoseconds
	 */
	public long getTotalResponseTime()
	{
		Iterator<QueryRun> rs = this.getRuns();
		long total = 0;
		while (rs.hasNext())
		{
			QueryRun r = rs.next();
			if (r != null) 
			{
				if (r.getResponseTime() == Long.MAX_VALUE) return Long.MAX_VALUE;
				total += r.getResponseTime();
			}
		}
		return total;
	}
	
	/**
	 * Gets the runtime of the query from the set that took the shortest time to run
	 * @return Minimum Runtime in nanoseconds
	 */
	public long getMinimumRuntime()
	{
		Iterator<QueryRun> rs = this.getRuns();
		long min = Long.MAX_VALUE;
		while (rs.hasNext())
		{
			QueryRun r = rs.next();
			if (r != null) 
			{
				if (r.getRuntime() < min)
				{
					min = r.getRuntime();
				}
			}
		}
		return min;
	}
	
	/**
	 * Gets the ID of the Query that took the shortest time to run
	 * @return ID of the Query with the Minimum Runtime
	 */
	public int getMinimumRuntimeQueryID()
	{
		Iterator<QueryRun> rs = this.getRuns();
		long min = Long.MAX_VALUE;
		int id = 0;
		int i = -1;
		while (rs.hasNext())
		{
			QueryRun r = rs.next();
			i++;
			if (r != null) 
			{
				if (r.getRuntime() < min)
				{
					id = i;
					min = r.getRuntime();
				}
			}
		}
		return id;
	}
	
	/**
	 * Gets the runtime of the query from the set that took the longest time to run
	 * @return Maximum Runtime in nanoseconds
	 */
	public long getMaximumRuntime()
	{
		Iterator<QueryRun> rs = this.getRuns();
		long max = Long.MIN_VALUE;
		while (rs.hasNext())
		{
			QueryRun r = rs.next();
			if (r != null) 
			{
				if (r.getRuntime() > max)
				{
					max = r.getRuntime();
				}
			}
		}
		return max;
	}
	
	/**
	 * Gets the ID of the Query that took the longest time to run
	 * @return ID of the query with Maximum Runtime
	 */
	public int getMaximumRuntimeQueryID()
	{
		Iterator<QueryRun> rs = this.getRuns();
		long max = Long.MIN_VALUE;
		int id = 0;
		int i = -1;
		while (rs.hasNext())
		{
			QueryRun r = rs.next();
			i++;
			if (r != null) 
			{
				if (r.getRuntime() > max)
				{
					id = i;
					max = r.getRuntime();
				}
			}
		}
		return id;
	}

	/**
	 * Compares one run to another
	 * <p>
	 * Used for sorting the runs so that outliers can be trimmed
	 * </p>
	 */
	@Override
	public int compareTo(QueryMixRun other) {
		long runtime = this.getTotalRuntime();
		long otherRuntime = other.getTotalRuntime();
		if (runtime < otherRuntime)
		{
			return -1;
		}
		else if (runtime > otherRuntime)
		{
			return 1;
		}
		else
		{
			return 0;
		}
	}
}
