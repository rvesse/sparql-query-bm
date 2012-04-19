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

package net.sf.sparql.query.benchmarking.stats;

/**
 * Represents a run of a single query
 * @author rvesse
 *
 */
public class QueryRun implements Comparable<QueryRun> {
	
	public static final long NOT_YET_RUN = -1;

	private long runtime = NOT_YET_RUN;
	private long responseTime = NOT_YET_RUN;
	private long resultCount = NOT_YET_RUN;
	private String errorMessage;
	private long order = NOT_YET_RUN;
	
	/**
	 * Creates a Query Run which represents that the running of a query resulted in an error
	 * @param error Error Message
	 * @param runtime Runtime, this is the amount of time elapsed until the error/timeout was reached
	 */
	public QueryRun(String error, long runtime)
	{
		this(runtime, 0);
		this.errorMessage = error;
	}
	
	/**
	 * Creates a Query run which represents the results of running a query
	 * @param runtime Runtime
	 * @param resultCount Result Count
	 */
	private QueryRun(long runtime, long resultCount)
	{
		this.runtime = runtime;
		this.resultCount = resultCount;
	}
	
	/**
	 * Creates a Query run which represents the results of running a query
	 * @param runtime Runtime
	 * @param responseTime Response Time
	 * @param resultCount Result Count
	 */
	public QueryRun(long runtime, long responseTime, long resultCount)
	{
		this(runtime, resultCount);
		this.responseTime = responseTime;
	}
	
	/**
	 * Gets the runtime in nanoseconds
	 * @return Runtime in nanoseconds
	 */
	public long getRuntime()
	{
		return this.runtime;
	}
	
	/**
	 * Gets the response time in nanoseconds
	 * <p>
	 * Response Time is an additional metric which currently is only calculated for SELECT queries, all other queries will report the same value as the {@link #getRuntime()}.
	 * This metric is the time taken for the first result to become available so for SELECT queries whose results are processed in a streaming fashion this will be much
	 * smaller than the runtime which is the total time to stream and count all results
	 * </p>
	 * @return Response Time in nanoseconds
	 */
	public long getResponseTime()
	{
		if (this.responseTime == NOT_YET_RUN)
		{
			return this.runtime;
		}
		else
		{
			return this.responseTime;
		}
	}
	
	/**
	 * Gets the number of results returned
	 * @return Number of results
	 */
	public long getResultCount()
	{
		return this.resultCount;
	}
	
	/**
	 * Returns whether this run represents a successful (i.e. non-error) run of the query
	 * @return True if the run was successful
	 */
	public boolean wasSuccessful()
	{
		return this.errorMessage == null;
	}
	
	/**
	 * Gets the error message associated with the query run, will be null if the query ran successfully
	 * @return Error Message or null
	 */
	public String getErrorMessage()
	{
		return this.errorMessage;
	}
	
	/**
	 * Gets the global run order for this query run
	 * @return
	 */
	public long getRunOrder()
	{
		return this.order;
	}
	
	/**
	 * Sets the run order for this query run
	 * <p>
	 * Only really used internally, trying to set this once it has been set will lead to an {@link IllegalAccessError}
	 * </p>
	 * @param order Order
	 */
	public void setRunOrder(long order) throws IllegalAccessError
	{
		if (this.order == NOT_YET_RUN)
		{
			this.order = order;
		}
		else
		{
			throw new IllegalAccessError("Cannot set the run order after it has been set");
		}
	}

	/**
	 * Compares a run to another
	 * <p>
	 * Used for sorting the runs so outliers can be trimmed
	 * </p>
	 */
	@Override
	public int compareTo(QueryRun other) {
		long otherRuntime = other.getRuntime();
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
