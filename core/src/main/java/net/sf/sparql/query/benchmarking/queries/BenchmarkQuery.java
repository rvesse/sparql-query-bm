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

package net.sf.sparql.query.benchmarking.queries;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import net.sf.sparql.query.benchmarking.Benchmarker;
import net.sf.sparql.query.benchmarking.BenchmarkerUtils;
import net.sf.sparql.query.benchmarking.parallel.ParallelTimer;
import net.sf.sparql.query.benchmarking.stats.QueryRun;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import org.apache.commons.math.stat.descriptive.moment.GeometricMean;
import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math.stat.descriptive.moment.Variance;

/**
 * Represents a Query that will be run as part of a Benchmark
 * @author rvesse
 */
public class BenchmarkQuery {

	private static final Logger logger = Logger.getLogger(BenchmarkQuery.class);
	private Query query;
	private String name, origQueryStr;
	private List<QueryRun> runs = new ArrayList<QueryRun>();
	private ParallelTimer timer = new ParallelTimer();
	
	private static final StandardDeviation sdev = new StandardDeviation(false);
	private static final Variance var = new Variance(false);
	private static final GeometricMean gmean = new GeometricMean();
	
	/**
	 * Creates a new Query
	 * @param name Name of the query
	 * @param queryString Query string
	 */
	public BenchmarkQuery(String name, String queryString)
	{
		this.name = name;
		this.origQueryStr = queryString;
	    this.query = QueryFactory.create(this.origQueryStr);
	}
	
	/**
	 * Gets the Name of the Query (typically the filename)
	 * @return Name
	 */
	public String getName()
	{
		return this.name;
	}
	
	/**
	 * Gets the actual Query
	 * @return Query
	 */
	public Query getQuery()
	{
		return this.query;
	}
	
	/**
	 * Gets the Query String used to create this Query
	 * @return Query as a string
	 */
	public String getQueryString()
	{
		return this.origQueryStr;
	}
	
	/**
	 * Gets an iterator over the query runs
	 * @return Runs of the query
	 */
	public Iterator<QueryRun> getRuns()
	{
		return this.runs.iterator();
	}
	
	/**
	 * Gets the total runtime for the query over all runs
	 * @return Total Runtime in nanoseconds
	 */
	public long getTotalRuntime()
	{
		long total = 0;
		for (QueryRun r : this.runs)
		{
			if (r.getRuntime() == Long.MAX_VALUE) return Long.MAX_VALUE;
			total += r.getRuntime();
		}
		return total;
	}
	
	/**
	 * Gets the actual runtime for the query over all runs (takes into account queries that run in parallel)
	 * @return Actual Runtime in nanoseconds
	 */
	public long getActualRuntime()
	{
		return this.timer.getActualRuntime();
	}
	
	/**
	 * Gets the total response time for the query over all runs
	 * <p>
	 * For non-SELECT queries this is identical to {@link #getTotalRuntime()}, for SELECT queries this is the total of the time spent waiting to start getting the response from the HTTP endpoint
	 * </p>
	 * @return Total response time
	 */
	public long getTotalResponseTime()
	{
		long total = 0;
		for (QueryRun r : this.runs)
		{
			if (r.getResponseTime() == Long.MAX_VALUE) return Long.MAX_VALUE;
			total += r.getResponseTime();
		}
		return total;
	}
	
	/**
	 * Gets the average runtime for the query over all runs (arithmetic mean) based on the total runtime
	 * @return Arithmetic Average Runtime in nanoseconds
	 */
	public long getAverageRuntime()
	{
		if (this.runs.size() == 0) return 0;
		return this.getTotalRuntime() / this.runs.size();
	}
	
	/**
	 * Gets the average response time over all runs (arithmetic mean) based on the total response time
	 * <p>
	 * For non-SELECT queries this is identical to {@link #getAverageRuntime()}, for SELECT queries this is the average time spent waiting to start getting the response from the HTTP endpoint
	 * </p>
	 * @return Average Response Time in nanoseconds
	 */
	public long getAverageResponseTime()
	{
		if (this.runs.size() == 0) return 0;
		return this.getTotalResponseTime() / this.runs.size();
	}
	
	/**
	 * Gets the average runtime for the query over all runs (geometric mean) based on the total runtime
	 * @return Geometric Average Runtime in nanoseconds
	 */
	public double getGeometricAverageRuntime()
	{
		if (this.runs.size() == 0) return 0;
		double[] values = new double[this.runs.size()];
		int i = 0;
		for (QueryRun r : this.runs)
		{
			values[i] = (double)r.getRuntime();
			i++;
		}
		return BenchmarkQuery.gmean.evaluate(values);
	}
	
	/**
	 * Gets average runtime for the query over all runs (arithmetic mean) based on the actual runtime
	 * @return Arithmetic Average runtime in nanoseconds
	 */
	public long getActualAverageRuntime()
	{
		if (this.runs.size() == 0) return 0;
		return this.getActualRuntime() / this.runs.size();
	}
	
	/**
	 * Gets the minimum runtime for this query over all runs
	 * @return Minimum Runtime in nanoseconds
	 */
	public long getMinimumRuntime()
	{
		long min = Long.MAX_VALUE;
		for (QueryRun r : this.runs)
		{
			if (r.getRuntime() < min)
			{
				min = r.getRuntime();
			}
		}
		return min;
	}
	
	/**
	 * Gets the maximum runtime for this query over all runs
	 * @return Maximum Runtime in nanoseconds
	 */
	public long getMaximumRuntime()
	{
		long max = Long.MIN_VALUE;
		for (QueryRun r : this.runs)
		{
			if (r.getRuntime() > max)
			{
				max = r.getRuntime();
			}
		}
		return max;
	}
	
	/**
	 * Gets the Variance for the Query runtimes
	 * @return Runtime Variance in nanoseconds
	 */
	public double getVariance()
	{
		double[] values = new double[this.runs.size()];
		int i = 0;
		for (QueryRun r : this.runs)
		{
			values[i] = BenchmarkerUtils.toSeconds(r.getRuntime());
			i++;
		}
		return BenchmarkQuery.var.evaluate(values);
	}
	
	/**
	 * Gets the Standard Deviation for Query runtime
	 * @return Runtime Standard Deviation in nanoseconds
	 */
	public double getStandardDeviation()
	{
		double[] values = new double[this.runs.size()];
		int i = 0;
		for (QueryRun r : this.runs)
		{
			values[i] = (double)r.getRuntime();
			i++;
		}
		return BenchmarkQuery.sdev.evaluate(values);
	}
	
	/**
	 * Calculates how many times this query could be executed single-threaded per second based upon the average runtime of the query
	 * @return Queries per Second
	 */
	public double getQueriesPerSecond()
	{
		double avgRuntime = BenchmarkerUtils.toSeconds(this.getAverageRuntime());
		if (avgRuntime == 0) return 0;
		return 1 / avgRuntime;
	}
	
	/**
	 * Calculates how many times this query could be executed multi-threaded per second based upon the {@link BenchmarkQuery#getActualAverageRuntime()}
	 * @return Actual Queries per Second
	 */
	public double getActualQueriesPerSecond()
	{
		double avgRuntime = BenchmarkerUtils.toSeconds(this.getActualAverageRuntime());
		if (avgRuntime == 0) return 0;
		return 1 / avgRuntime;
	}
	
	/**
	 * Calculates how many times this query could be executed single-threaded per hour based upon the average runtime of the query
	 * @return Queries per Hour
	 */
	public double getQueriesPerHour()
	{
		double avgRuntime = BenchmarkerUtils.toSeconds(this.getAverageRuntime());
		if (avgRuntime == 0) return 0;
		return BenchmarkerUtils.SECONDS_PER_HOUR / avgRuntime;
	}
	
	/**
	 * Calculates how many times this query could be executed multi-threaded per hour based upon the {@link BenchmarkQuery#getActualAverageRuntime()}
	 * @return Actual Queries per Hour
	 */
	public double getActualQueriesPerHour()
	{
		double avgRuntime = BenchmarkerUtils.toSeconds(this.getActualAverageRuntime());
		if (avgRuntime == 0) return 0;
		return BenchmarkerUtils.SECONDS_PER_HOUR / avgRuntime;
	}
	
	/**
	 * Runs the query recording the statistics as a {@link QueryRun}
	 * @param b Benchmarker
	 * @return Query Run statistics
	 */
	public QueryRun run(Benchmarker b)
	{
		timer.start();
		long order = b.getGlobalOrder();
		QueryRunner runner = new QueryRunner(this.query, b);
		QueryTask task = new QueryTask(runner);
		b.getExecutor().submit(task);
		QueryRun r;
		long startTime = System.nanoTime();
		try
		{
			r = task.get(b.getTimeout(), TimeUnit.SECONDS);
		}
		catch (TimeoutException tEx)
		{
			logger.error("Query Runner execeeded Timeout - " + tEx.getMessage());
			if (b.getHaltOnTimeout() || b.getHaltAny()) b.halt(tEx);
			r = new QueryRun("Query Runner execeeded Timeout - " + tEx.getMessage(), System.nanoTime() - startTime);
			
			//If the query times out but we aren't halting cancel further evaluation of the query
			task.cancel(true);
			runner.cancel();
		} 
		catch (InterruptedException e) 
		{
			logger.error("Query Runner was interrupted - " + e.getMessage());
			if (b.getHaltAny()) b.halt(e);
			r = new QueryRun("Query Runner was interrupted - " + e.getMessage(), System.nanoTime() - startTime);
		}
		catch (ExecutionException e)
		{
			logger.error("Query Runner encountered an error - " + e.getMessage());
			
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			logger.error(sw.toString());
			
			if (b.getHaltOnError() || b.getHaltAny()) b.halt(e);
			r = new QueryRun("Query Runner encountered an error - " + e.getMessage(), System.nanoTime() - startTime);
		}
		timer.stop();
		this.runs.add(r);
		r.setRunOrder(order);
		return r;
	}
	
	/**
	 * Clears all run statistics
	 */
	public void clear()
	{
		this.runs.clear();
	}
	
	/**
	 * Trims the best and worst N runs
	 * @param outliers
	 */
	public void trim(int outliers)
	{
		if (outliers <= 0) return;
		
		PriorityQueue<QueryRun> rs = new PriorityQueue<QueryRun>();
		rs.addAll(this.runs);
		//Discard Best N
		for (int i = 0; i < outliers; i++)
		{
			this.runs.remove(rs.remove());
		}
		//Discard Last N
		while (rs.size() > outliers)
		{
			rs.remove();
		}
		for (QueryRun r : rs)
		{
			this.runs.remove(r);
		}
	}

	/**
	 * Gets the string representation (i.e. the name) of the query
	 */
	@Override
	public String toString()
	{
		return this.name;
	}
}
