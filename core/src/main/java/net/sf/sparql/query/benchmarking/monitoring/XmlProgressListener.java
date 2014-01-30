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

import java.io.File;
import java.io.PrintWriter;
import java.util.Iterator;

import net.sf.sparql.query.benchmarking.Benchmarker;
import net.sf.sparql.query.benchmarking.BenchmarkerUtils;
import net.sf.sparql.query.benchmarking.operations.BenchmarkOperation;
import net.sf.sparql.query.benchmarking.operations.BenchmarkOperationMix;
import net.sf.sparql.query.benchmarking.queries.BenchmarkQuery;
import net.sf.sparql.query.benchmarking.stats.OperationMixRun;
import net.sf.sparql.query.benchmarking.stats.OperationRun;
import net.sf.sparql.query.benchmarking.stats.QueryRun;


/**
 * A Progress Listener that generates a XML output file
 * @author rvesse
 */
public class XmlProgressListener implements ProgressListener 
{
	private Benchmarker b;
	private File file;
	private PrintWriter writer;
	private int indent = 0;
	
	/**
	 * Constants for Tag and Attribute Names
	 */
	public static final String TAG_SPARQL_BENCHMARK = "sparqlBenchmark",
							   TAG_CONFIGURATION = "configuration",
							   TAG_CONFIG_PROPERTY = "property",
							   ATTR_ID = "id",
							   ATTR_NAME = "name",
							   ATTR_VALUE = "value",
							   TAG_QUERIES = "queries",
							   TAG_QUERY = "query",
							   TAG_MIX_RUNS = "queryMixRuns",
							   TAG_MIX_RUN = "queryMixRun",
							   TAG_STATS = "statistics",
							   TAG_SUMMARY = "summary",
							   ATTR_RESPONSE_TIME = "responseTime",
							   ATTR_TOTAL_RESPONSE_TIME = "totalResponseTime",
							   ATTR_RUNTIME = "runtime",
							   ATTR_RESULT_COUNT = "resultCount",
							   ATTR_TOTAL_RUNTIME = "totalRuntime",
							   ATTR_ACTUAL_RUNTIME = "actualRuntime",
							   ATTR_ACTUAL_AVG_RUNTIME = "averageActualRuntime",
							   ATTR_MIN_QUERY_RUNTIME = "minQueryRuntime",
							   ATTR_MAX_QUERY_RUNTIME = "maxQueryRuntime",
							   ATTR_MIN_MIX_RUNTIME = "minMixRuntime",
							   ATTR_MAX_MIX_RUNTIME = "maxMixRuntime",
							   ATTR_AVG_RUNTIME = "averageRuntime",
							   ATTR_AVG_RUNTIME_GEOM = "averageRuntimeGeometric",
							   ATTR_AVG_RESPONSE_TIME = "averageResponseTime",
							   ATTR_AVG_RESPONSE_TIME_GEOM = "averageResponseTimeGeometric",
							   ATTR_VARIANCE = "variance",
							   ATTR_STD_DEV = "standardDeviation",
							   ATTR_QPS = "queriesPerSecond",
							   ATTR_ACTUAL_QPS = "actualQueriesPerSecond",
							   ATTR_QPH = "queriesPerHour",
							   ATTR_ACTUAL_QPH = "actualQueriesPerHour",
							   ATTR_QMPH = "queryMixesPerHour",
							   ATTR_ACTUAL_QMPH = "actualQueryMixesPerHour",
							   ATTR_FASTEST_QUERY = "fastestQuery",
							   ATTR_SLOWEST_QUERY = "slowestQuery",
							   ATTR_RUN_ORDER = "runOrder";
	
	/**
	 * Constructor to be called when the file to write to should be detected at benchmarking start time using the {@link Benchmarker#getXmlResultsFile()} method
	 */
	public XmlProgressListener()
	{
		
	}
	
	/**
	 * Constructor to be called when the file to write to is known in advance of benchmarking, assumes overwriting if forbidden
	 * @param outputPath Output File Path
	 */
	public XmlProgressListener(String outputPath)
	{
		this(outputPath, false);
	}
	
	/**
	 * Constructor to be called when the file to write to is known in advance of benchmarking
	 * @param outputPath Output File Path
	 * @param allowOverwrite Whether overwriting an existing file is permitted
	 */
	public XmlProgressListener(String outputPath, boolean allowOverwrite)
	{
		this.setup(outputPath, allowOverwrite);
	}
	
	/**
	 * Sets up the file for output throwing an error if the file cannot be written to
	 * @param outputPath Output File Path
	 */
	private void setup(String outputPath, boolean allowOverwrite)
	{
		if (!BenchmarkerUtils.checkFile(outputPath, allowOverwrite))
		{
			throw new RuntimeException("XML Output File is not a file, already exists or is not writable");
		}
		file = new File(outputPath);
	}
	
	/**
	 * Handles the started event by printing run configuration to the XML file
	 * @param b Benchmarker
	 */
	@Override
	public void handleStarted(Benchmarker b) 
	{
		if (file == null)
		{
			this.setup(b.getXmlResultsFile(), b.getAllowOverwrite());
		}
		
		try
		{
			this.b = b;
			
			//Open Print Writer
			writer = new PrintWriter(file);
			
			//Generate XML Header
			writer.println("<?xml version=\"1.0\"?>");
			openTag(TAG_SPARQL_BENCHMARK);
			
			//Generate an <configuration> element detailing configuration
			openTag(TAG_CONFIGURATION);
			printProperty("endpoint", b.getEndpoint());
			printProperty("sanityChecking", b.getSanityCheckLevel());
			printProperty("warmups", b.getWarmups());
			printProperty("runs", b.getRuns());
			printProperty("randomOrder", b.getRandomizeOrder());
			printProperty("outliers", b.getOutliers());
			printProperty("timeout", b.getTimeout());
			printProperty("maxDelay", b.getMaxDelay());
			printProperty("askFormat", b.getResultsAskFormat());
			printProperty("graphFormat", b.getResultsGraphFormat());
			printProperty("selectFormat", b.getResultsSelectFormat());
			printProperty("threads", b.getParallelThreads());
			printProperty("counting", !b.getNoCount());
			printProperty("limit", b.getLimit());
			printProperty("gzip", b.getAllowGZipEncoding());
			printProperty("deflate", b.getAllowDeflateEncoding());
			
			//Print Queries
			openTag(TAG_QUERIES);
			BenchmarkOperationMix mix = b.getQueryMix();
			Iterator<BenchmarkQuery> qs = mix.getQueries();
			int id = 0;
			while (qs.hasNext())
			{
				BenchmarkOperation q = qs.next();
				openTag(TAG_QUERY, true);
				addAttribute(ATTR_ID, id);
				addAttribute(ATTR_NAME, q.getName());
				id++;
				finishAttributes();
				
				openCData();
				writer.print(q.getQuery().toString());
				closeCData();
				closeTag(TAG_QUERY);
			}
			closeTag(TAG_QUERIES);
			
			closeTag(TAG_CONFIGURATION);
			
			//Open Tag for Mix Run stats
			openTag(TAG_MIX_RUNS);		
			
			writer.flush();
		}
		catch (Exception e) 
		{
			System.err.println("Unexpected error writing XML stats");
			b.halt(e.getMessage());
		}
	}
	
	protected void printProperty(String name, int value)
	{
		printProperty(name, Integer.toString(value));
	}
	
	protected void printProperty(String name, long value)
	{
		printProperty(name, Long.toString(value));
	}
	
	protected void printProperty(String name, boolean value)
	{
		printProperty(name, Boolean.toString(value));
	}
	
	protected void printProperty(String name, String value)
	{
		openTag(TAG_CONFIG_PROPERTY, true);
		addAttribute(ATTR_NAME, name);
		if (value == null) value = "";
		addAttribute(ATTR_VALUE, value);
		finishAttributes(true);
	}

	/**
	 * Handles the finished event by printing statistics to the XML file
	 * @param ok Whether benchmarking finished OK
	 */
	@Override
	public void handleFinished(boolean ok) 
	{
		if (writer == null) throw new RuntimeException("handleFinished() on XmlProgressListener was called but it appears handleStarted() was never called, another listener may have caused handleStarted() to be bypassed for this listener");
		try 
		{	
			closeTag(TAG_MIX_RUNS);
			
			openTag(TAG_STATS);
			
			boolean wasMultithreaded = b.getParallelThreads() > 1;
			
			//Query Summary
			openTag(TAG_QUERIES);		
			BenchmarkOperationMix queryMix = this.b.getQueryMix();
			Iterator<BenchmarkQuery> qs = queryMix.getQueries();
			int id = 0;
			while (qs.hasNext())
			{
				BenchmarkOperation q = qs.next();
				openTag(TAG_QUERY, true);
				
				//CSV Summary
				addAttribute(ATTR_ID, id);
				addAttribute(ATTR_NAME, q.getName());
				addAttribute(ATTR_TOTAL_RESPONSE_TIME, q.getTotalResponseTime());
				addAttribute(ATTR_AVG_RESPONSE_TIME, q.getAverageResponseTime());
				addAttribute(ATTR_TOTAL_RUNTIME, q.getTotalRuntime());
				if (wasMultithreaded) addAttribute(ATTR_ACTUAL_RUNTIME, q.getActualRuntime());
				addAttribute(ATTR_AVG_RUNTIME, q.getAverageRuntime());
				if (wasMultithreaded) addAttribute(ATTR_ACTUAL_AVG_RUNTIME, q.getActualAverageRuntime());
				addAttribute(ATTR_AVG_RUNTIME_GEOM, q.getGeometricAverageRuntime());
				addAttribute(ATTR_MIN_QUERY_RUNTIME, q.getMinimumRuntime());
				addAttribute(ATTR_MAX_QUERY_RUNTIME, q.getMaximumRuntime());
				addAttribute(ATTR_VARIANCE, q.getVariance());
				addAttribute(ATTR_STD_DEV, q.getStandardDeviation());
				addAttribute(ATTR_QPS, q.getOperationsPerSecond());
				if (wasMultithreaded) addAttribute(ATTR_ACTUAL_QPS, q.getActualOperationsPerSecond());
				addAttribute(ATTR_QPH, q.getOperationsPerHour());		
				if (wasMultithreaded) addAttribute(ATTR_ACTUAL_QPH, q.getActualOperationsPerHour());
				finishAttributes(true);
				
				id++;
			}
			closeTag(TAG_QUERIES);
			
			//Overall Summary
			openTag(TAG_SUMMARY, true);
			addAttribute(ATTR_TOTAL_RESPONSE_TIME, queryMix.getTotalResponseTime());
			addAttribute(ATTR_AVG_RESPONSE_TIME, queryMix.getAverageResponseTime());
			addAttribute(ATTR_TOTAL_RUNTIME, queryMix.getTotalRuntime());
			if (wasMultithreaded) addAttribute(ATTR_ACTUAL_RUNTIME, queryMix.getActualRuntime());
			addAttribute(ATTR_AVG_RUNTIME, queryMix.getAverageRuntime());	
			if (wasMultithreaded) addAttribute(ATTR_ACTUAL_AVG_RUNTIME, queryMix.getActualAverageRuntime());
			addAttribute(ATTR_AVG_RUNTIME_GEOM, queryMix.getGeometricAverageRuntime());
			addAttribute(ATTR_MIN_MIX_RUNTIME, queryMix.getMinimumRuntime());
			addAttribute(ATTR_MAX_MIX_RUNTIME, queryMix.getMaximumRuntime());
			addAttribute(ATTR_VARIANCE, queryMix.getVariance());
			addAttribute(ATTR_STD_DEV, queryMix.getStandardDeviation());
			addAttribute(ATTR_QMPH, queryMix.getOperationMixesPerHour());
			if (wasMultithreaded) addAttribute(ATTR_ACTUAL_QMPH, queryMix.getActualOperationMixesPerHour());
			finishAttributes(true);
			
			closeTag(TAG_STATS);
			closeTag(TAG_SPARQL_BENCHMARK);
			
			writer.flush();
			writer.close();
		} 
		catch (Exception e) 
		{
			System.err.println("Unexpected error writing XML stats " + e);
			e.printStackTrace();
			if (b.getHaltAny() || b.getHaltOnError()) b.halt(e.getMessage());
		}
	}

	@Override
	public void handleProgress(String message) 
	{
		//Not relevant for XML output
	}

	@Override
	public void handleProgress(BenchmarkOperation query, OperationRun run) 
	{
		//We don't handle individual query run stats, we only handle mix and aggregate stats
	}

	@Override
	public synchronized void handleProgress(OperationMixRun run) 
	{
		//Print run information
		openTag(TAG_MIX_RUN, true);
		
		this.addAttribute(ATTR_RUN_ORDER, run.getRunOrder());
		this.addAttribute(ATTR_TOTAL_RESPONSE_TIME, run.getTotalResponseTime());
		this.addAttribute(ATTR_TOTAL_RUNTIME, run.getTotalRuntime());
		this.addAttribute(ATTR_MIN_QUERY_RUNTIME, run.getMinimumRuntime());
		this.addAttribute(ATTR_MAX_QUERY_RUNTIME, run.getMaximumRuntime());
		this.addAttribute(ATTR_FASTEST_QUERY, run.getMinimumRuntimeOperationID());
		this.addAttribute(ATTR_SLOWEST_QUERY, run.getMaximumRuntimeOperationID());
		
		finishAttributes();
		
		Iterator<QueryRun> rs = run.getRuns();
		int id = 0;
		while (rs.hasNext())
		{
			OperationRun r = rs.next();
			openTag(TAG_QUERY, true);
			addAttribute(ATTR_ID, id);
			id++;
			addAttribute(ATTR_RUN_ORDER, r.getRunOrder());
			addAttribute(ATTR_RESPONSE_TIME, r.getResponseTime());
			addAttribute(ATTR_RUNTIME, r.getRuntime());
			addAttribute(ATTR_RESULT_COUNT, r.getResultCount());
			
			if (r.getErrorMessage() != null)
			{
				finishAttributes();
				writer.print(escape(r.getErrorMessage()));
				closeTag(TAG_QUERY);
			}
			else
			{
				finishAttributes(true);
			}
		}
		
		closeTag(TAG_MIX_RUN);
		
		writer.flush();
	}
	
	private void openTag(String tagname)
	{
		openTag(tagname, false);
	}
	
	private void openTag(String tagname, boolean allowAttributes)
	{
		if (allowAttributes)
		{
			writer.print(indent() + "<" + tagname);
		}
		else
		{
			writer.println(indent() + "<" + tagname + ">");
			indent++;
		}
	}
	
	private void closeTag(String tagname)
	{
		indent--;
		writer.println(indent() + "</" + tagname + ">");
	}
	
	private void openCData()
	{
		indent++;
		writer.println(indent() + "<![CDATA[");
	}
	
	private void closeCData()
	{
		writer.println("]]>");
		indent--;
	}
	
	private void addAttribute(String attr, int value)
	{
		addAttribute(attr, Integer.toString(value));
	}
	
	private void addAttribute(String attr, long value)
	{
		addAttribute(attr, Long.toString(value));
	}
	
	private void addAttribute(String attr, double value)
	{
		addAttribute(attr, Double.toString(value));
	}
	
	private void addAttribute(String attr, String value)
	{
		writer.print(' ');
		writer.print(attr);
		writer.print('=');
		writer.print('"');
		writer.print(escapeForAttribute(value));
		writer.print('"');
	}
	
	private void finishAttributes()
	{
		finishAttributes(false);
	}
	
	private void finishAttributes(boolean closeTag)
	{
		if (closeTag) writer.print(" /");
		writer.println(">");
		if (!closeTag) indent++;
	}
	
	@SuppressWarnings("unused")
	private void printTag(String tagname, String value)
	{
		writer.println(indent() + "<" + tagname + ">" + escape(value) + "</" + tagname + ">");
	}
	
	private String indent()
	{
		if (indent == 0) return "";
		StringBuffer buffer = new StringBuffer();
		int i = 0;
		while (i < indent)
		{
			buffer.append(' ');
			i++;
		}
		return buffer.toString();
	}
	
	private String escape(String string)
	{
		final StringBuilder sb = new StringBuilder(string);

		int offset = 0;
		String replacement;
		char found;
		for (int i = 0; i < string.length(); i++) {
			found = string.charAt(i);

			switch (found) {
			case '&' : replacement = "&amp;"; break;
			case '<' : replacement = "&lt;"; break;
			case '>' : replacement = "&gt;"; break;
			case '\r': replacement = "&#x0D;"; break;
			case '\n': replacement = "&#x0A;"; break;
			default  : replacement = null;
			}

			if (replacement != null) {
				sb.replace(offset + i, offset + i + 1, replacement);
				offset += replacement.length() - 1; // account for added chars
			}
		}

		return sb.toString();
	}
	
	private String escapeForAttribute(String string)
	{
		string = escape(string);
		return string.replace("\"", "&quot;");
	}
}
