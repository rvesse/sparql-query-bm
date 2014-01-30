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

package net.sf.sparql.query.benchmarking.monitoring;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

import net.sf.sparql.query.benchmarking.Benchmarker;
import net.sf.sparql.query.benchmarking.BenchmarkerUtils;
import net.sf.sparql.query.benchmarking.operations.BenchmarkOperation;
import net.sf.sparql.query.benchmarking.operations.BenchmarkOperationMix;
import net.sf.sparql.query.benchmarking.queries.BenchmarkQuery;
import net.sf.sparql.query.benchmarking.stats.OperationMixRun;
import net.sf.sparql.query.benchmarking.stats.OperationRun;

import org.apache.log4j.Logger;


/**
 * A Progress Listener that generates a CSV output file
 * @author rvesse
 *
 */
public class CsvProgressListener implements ProgressListener 
{
	private static final Logger logger = Logger.getLogger(CsvProgressListener.class);
	
	private Benchmarker b;
	private StringBuffer buffer;
	private int run = 1;
	private boolean ready = false;
	
	/**
	 * Handles the started event by preparing a record of the run configuration which will eventually be printed to the CSV file
	 * @param b Benchmarker object
	 */
	@Override
	public void handleStarted(Benchmarker b) {
		this.b = b;
		this.buffer = new StringBuffer();
		this.run = 1;
		
		//Check whether File already exists
		if (!BenchmarkerUtils.checkFile(this.b.getCsvResultsFile(), this.b.getAllowOverwrite()))
		{
			throw new RuntimeException("CSV Results File is not a file, already exists or is not writable");
		}
		
		//Information on Benchmark Options
		this.buffer.append("Options Summary,\n");
		this.buffer.append("Endpoint," + this.b.getEndpoint() + "\n");
		this.buffer.append("Sanity Checking Level," + this.b.getSanityCheckLevel() + "\n");
		this.buffer.append("Warmups," + this.b.getWarmups() + "\n");
		this.buffer.append("Runs," + this.b.getRuns() + "\n");
		this.buffer.append("Random Query Order," + this.b.getRandomizeOrder() + "\n");
		this.buffer.append("Outliers," + this.b.getOutliers() + "\n");
		this.buffer.append("Timeout," + this.b.getTimeout() + "s\n");
		this.buffer.append("Max Delay between Queries," + this.b.getMaxDelay() + "s\n");
		this.buffer.append("Result Limit," + (this.b.getLimit() <= 0 ? "Query Specified" : this.b.getLimit()) + "\n");
		this.buffer.append("ASK Results Format," + this.b.getResultsAskFormat() + "\n");
		this.buffer.append("Graph Results Format," + this.b.getResultsGraphFormat() + "\n");
		this.buffer.append("SELECT Results Format," + this.b.getResultsSelectFormat() + "\n");
		this.buffer.append("Parallel Threads," + this.b.getParallelThreads() + "\n");
		this.buffer.append("Result Counting," + this.b.getNoCount() + "\n");
		this.buffer.append(",\n");
		
		//Header for Run Summary
		this.buffer.append("Run Summary,\n");
		this.buffer.append("Run,Total Response Time,Total Runtime,Min Query Runtime,Max Query Runtime\n");
		
		//Actual run summaries are printed by handleProgess(QuerySetRun run) during benchmarking
		this.ready = true;
	}

	/**
	 * Handles the finished event by printing relevant statistics to the CSV file
	 * @param ok Whether benchmarking finished OK
	 */
	@Override
	public void handleFinished(boolean ok) 
	{
		if (!this.ready) throw new RuntimeException("handleFinished() was called on CsvProgressListener but it appears handleStarted() was not called or encountered an error, another listener may be the cause of this issue");
		
		boolean wasMultithreaded = b.getParallelThreads() > 1;
		
		//Query Summary
		this.buffer.append(",\nQuery Summary,\n");
		if (wasMultithreaded)
		{
			this.buffer.append("Query,Total Response Time,Average Response Time (Arithmetic),Total Runtime,Actual Runtime,Average Runtime (Arithmetic),Actual Average Runtime (Arithmetic),Average Runtime (Geometric),Min Runtime,Max Runtime,Variance,Standard Deviation,Queries per Second,Actual Queries per Second,Queries per Hour,Actual Queries per Hour\n");
		}
		else
		{
			this.buffer.append("Query,Total Response Time,Average Response Time (Arithmetic),Total Runtime,Average Runtime (Arithmetic),Average Runtime (Geometric),Min Runtime,Max Runtime,Variance,Standard Deviation,Queries per Second,Queries per Hour\n");
		}
		
		BenchmarkOperationMix queryMix = this.b.getQueryMix();
		Iterator<BenchmarkQuery> qs = queryMix.getQueries();
		while (qs.hasNext())
		{
			BenchmarkOperation q = qs.next();
			//CSV Summary
			this.buffer.append(BenchmarkerUtils.toCsv(q.getName()) + ",");
			this.buffer.append(BenchmarkerUtils.toSeconds(q.getTotalResponseTime()) + ",");
			this.buffer.append(BenchmarkerUtils.toSeconds(q.getAverageResponseTime()) + ",");
			this.buffer.append(BenchmarkerUtils.toSeconds(q.getTotalRuntime()) + ",");
			if (wasMultithreaded) this.buffer.append(BenchmarkerUtils.toSeconds(q.getActualRuntime()) + ",");
			this.buffer.append(BenchmarkerUtils.toSeconds(q.getAverageRuntime()) + ",");
			if (wasMultithreaded) this.buffer.append(BenchmarkerUtils.toSeconds(q.getActualAverageRuntime()) + ",");
			this.buffer.append(q.getGeometricAverageRuntime() + ",");
			this.buffer.append(BenchmarkerUtils.toSeconds(q.getMinimumRuntime()) + ",");
			this.buffer.append(BenchmarkerUtils.toSeconds(q.getMaximumRuntime()) + ",");
			this.buffer.append(BenchmarkerUtils.toSeconds(q.getVariance()) + ",");
			this.buffer.append(BenchmarkerUtils.toSeconds(q.getStandardDeviation()) + ",");
			this.buffer.append(q.getOperationsPerSecond() + ",");
			if (wasMultithreaded) this.buffer.append(q.getActualOperationsPerSecond() + ",");
			this.buffer.append(q.getOperationsPerHour());
			if (wasMultithreaded) this.buffer.append("," + q.getActualOperationsPerHour());
			this.buffer.append("\n");
		}
		
		//Benchmark Summary
		try 
		{	
			//CSV Summary
			FileWriter results = new FileWriter(this.b.getCsvResultsFile());
			
			if (wasMultithreaded)
			{
				results.append("Total Response Time,Average Response Time (Arithmetic),Total Runtime,Actual Runtime,Average Runtime (Arithmetic),Actual Average Runtime (Arithmetic),Average Runtime (Geometric),Minimum Mix Runtime,Maximum Mix Runtime,Variance,Standard Deviation,Query Mixes per Hour,Actual Query Mixes per Hour\n");
			}
			else
			{
				results.append("Total Response Time,Average Response Time (Arithmetic),Total Runtime,Average Runtime (Arithmetic),Average Runtime (Geometric),Minimum Mix Runtime,Maximum Mix Runtime,Variance,Standard Deviation,Query Mixes per Hour\n");
			}
			results.append(BenchmarkerUtils.toSeconds(queryMix.getTotalResponseTime()) + ",");
			results.append(BenchmarkerUtils.toSeconds(queryMix.getAverageResponseTime()) + ",");
			results.append(BenchmarkerUtils.toSeconds(queryMix.getTotalRuntime()) + ",");
			if (wasMultithreaded) results.append(BenchmarkerUtils.toSeconds(queryMix.getActualRuntime()) + ",");
			results.append(BenchmarkerUtils.toSeconds(queryMix.getAverageRuntime()) + ",");
			if (wasMultithreaded) results.append(BenchmarkerUtils.toSeconds(queryMix.getActualAverageRuntime()) + ",");
			results.append(BenchmarkerUtils.toSeconds(queryMix.getGeometricAverageRuntime()) + ",");
			results.append(BenchmarkerUtils.toSeconds(queryMix.getMinimumRuntime()) + ",");
			results.append(BenchmarkerUtils.toSeconds(queryMix.getMaximumRuntime()) + ",");
			results.append(BenchmarkerUtils.toSeconds(queryMix.getVariance()) + ",");
			results.append(BenchmarkerUtils.toSeconds(queryMix.getStandardDeviation()) + ",");
			results.append(Double.toString(queryMix.getOperationMixesPerHour()));
			if (wasMultithreaded) results.append("," + queryMix.getActualOperationMixesPerHour());
			results.append("\n");
			results.append(buffer.toString());
			results.close();
			this.b.reportProgress("Results for this run output to " + this.b.getCsvResultsFile());
		} 
		catch (IOException e) 
		{
			System.err.println("Error created CSV results file " + this.b.getCsvResultsFile());
			logger.error("Error creating CSV results file" + this.b.getCsvResultsFile(), e);
			if (b.getHaltAny() || b.getHaltOnError()) this.b.halt(e.getMessage());
		}
	}

	/**
	 * Does nothing as this listener discards informational messages
	 * @param message Informational Message
	 */
	@Override
	public void handleProgress(String message) {
		//We don't handle informational messages		
	}

	/**
	 * Does nothing as this listener discards inidividual query run statistics
	 * @param query Benchmark Query
	 * @param run Query Run statistics
	 */
	@Override
	public void handleProgress(BenchmarkOperation query, OperationRun run) {
		//We don't handle query run stats as they are produced, we're only interested in aggregate stats at the end		
	}

	/**
	 * Handles the Mix progress event by recording the run statistics for later printing to the CSV file
	 */
	@Override
	public synchronized void handleProgress(OperationMixRun run) {
		this.buffer.append(this.run + ",");
		this.buffer.append(BenchmarkerUtils.toSeconds(run.getTotalResponseTime()) + ",");
		this.buffer.append(BenchmarkerUtils.toSeconds(run.getTotalRuntime()) + ",");
		this.buffer.append(BenchmarkerUtils.toSeconds(run.getMinimumRuntime()) + ",");
		this.buffer.append(BenchmarkerUtils.toSeconds(run.getMaximumRuntime()) + "\n");
		this.run++;
	}

}
