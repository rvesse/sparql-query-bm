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

package net.sf.sparql.query.benchmarking.queries;

import java.util.concurrent.Callable;

import net.sf.sparql.query.benchmarking.Benchmarker;
import net.sf.sparql.query.benchmarking.BenchmarkerUtils;
import net.sf.sparql.query.benchmarking.stats.QueryRun;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

/**
 * A Callable for queries so we can execute them asynchronously with timeouts
 * @author rvesse
 *
 */
public class QueryRunner implements Callable<QueryRun> {

	private static final Logger logger = Logger.getLogger(QueryRunner.class);
	private Query query;
	private Benchmarker b;
	
	/**
	 * Creates a new Query Runner
	 * @param q Query to run
	 * @param b Benchmarker the query is run for
	 */
	public QueryRunner(Query q, Benchmarker b)
	{
		this.query = q;
		this.b = b;
	}

	/**
	 * Runs the Query counting the number of Results
	 */
	@Override
	public QueryRun call()
	{		
		//Impose Limit if applicable
		if (this.b.getLimit() > 0)
		{
			if (!this.query.isAskType())
			{
				if (this.query.getLimit() == Query.NOLIMIT || this.query.getLimit() > this.b.getLimit())
				{
					this.query.setLimit(this.b.getLimit());
				}
			}
		}
		
		//Create a QueryEngineHTTP directly as we want to set a bunch of parameters on it
		QueryEngineHTTP exec = new QueryEngineHTTP(this.b.getEndpoint(), this.query);
		exec.setSelectContentType(b.getResultsSelectFormat());
		exec.setAskContentType(b.getResultsAskFormat());
		exec.setModelContentType(b.getResultsGraphFormat());
		exec.setAllowDeflate(b.getAllowDeflateEncoding());
		exec.setAllowGZip(b.getAllowGZipEncoding());
		if (this.b.getUsername() != null && this.b.getPassword() != null)
		{
			exec.setBasicAuthentication(this.b.getUsername(), this.b.getPassword().toCharArray());
		}
		
		try
		{
			long numResults = 0;
			long responseTime = QueryRun.NOT_YET_RUN;
			long startTime = System.nanoTime();
			if (this.query.isAskType())
			{
				exec.execAsk();
				numResults = 1;
			}
			else if (this.query.isConstructType())
			{
				Model m = exec.execConstruct();
				numResults = m.size();
			}
			else if (this.query.isDescribeType())
			{
				Model m = exec.execDescribe();
				numResults = m.size();
			}
			else if (this.query.isSelectType())
			{
				ResultSet rset = exec.execSelect();
				responseTime = System.nanoTime() - startTime;
				this.b.reportPartialProgress("started responding in " + BenchmarkerUtils.toSeconds(responseTime) + "s...");
				
				//Result Counting may be skipped depending on user options
				if (!this.b.getNoCount()) 
				{
					//Count Results
					while (rset.hasNext())
					{
						numResults++;
						rset.next();
					}
				}
			}
			else
			{
				logger.warn("Query is not of a recognised type and so was not run");
				if (this.b.getHaltAny()) this.b.halt("Unrecognized Query Type");
			}
			long endTime = System.nanoTime();
			return new QueryRun(endTime - startTime, responseTime, numResults);
		}
		finally
		{
			//Clean up query execution
			if (exec != null) exec.close();
		}
	}
}
