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

package net.sf.sparql.query.benchmarking.monitoring;

import java.io.OutputStream;
import java.io.PrintStream;

import net.sf.sparql.query.benchmarking.Benchmarker;
import net.sf.sparql.query.benchmarking.queries.BenchmarkQuery;
import net.sf.sparql.query.benchmarking.stats.QueryMixRun;
import net.sf.sparql.query.benchmarking.stats.QueryRun;


/**
 * A Progress Listener that prints informational messages to a {@link PrintStream}
 * @author rvesse
 *
 */
public class StreamProgressListener implements ProgressListener 
{
	private PrintStream output;
	private boolean closeOnFinish = true;
	
	/**
	 * Creates a Progress Listener without a stream
	 * <p>
	 * Usable only by derived classes, the {@link #openStream()} method will be called to open a stream at the time benchmarking starts
	 * </p>
	 * @param closeOnFinish Whether the Output Stream should be closed when the listener receives the {@link #handleFinished()} call
	 */
	protected StreamProgressListener(boolean closeOnFinish)
	{
		this.closeOnFinish = true;
	}
	
	/**
	 * Creates a Progress Listener without a stream
	 * <p>
	 * Usable only by derived classes, the {@link #openStream()} method will be called to open a stream at the time benchmarking starts
	 * </p>
	 */
	protected StreamProgressListener()
	{
		this(true);
	}
	
	/**
	 * Creates a new Progress Listener for the given stream
	 * @param output Output Stream
	 * @param closeOnFinish Whether the Output Stream should be closed when the listener receives the {@link #handleFinished()} call
	 */
	public StreamProgressListener(PrintStream output, boolean closeOnFinish)
	{
		if (output == null) throw new IllegalArgumentException("Ouput Stream cannot be null");
		this.output = output;
		this.closeOnFinish = closeOnFinish;
	}
	
	/**
	 * Creates a new Progress Listener for the given stream
	 * @param output Output Stream
	 */
	public StreamProgressListener(PrintStream output)
	{
		this(output, true);
	}
	
	/**
	 * Creates a new Progress Listener for the given stream
	 * @param output Output Stream
	 * @param closeOnFinish Whether the Ouput Stream should be closed when the listener receives the handleFinished() call
	 */
	public StreamProgressListener(OutputStream output)
	{
		this(new PrintStream(output));
	}
	
	/**
	 * Creates a new Progress Listener for the given stream
	 * @param output Output Stream
	 */
	public StreamProgressListener(OutputStream output, boolean closeOnFinish)
	{
		this(new PrintStream(output), closeOnFinish);
	}
	
	/**
	 * Internal method called only when this class is derived from and the protected constructors which do not take a stream argument have been used
	 * @return Output Stream
	 */
	protected OutputStream openStream() {
		return null;
	}
	
	/**
	 * Writes the message to the underlying {@link PrintStream}
	 * @param message Informational Message
	 */
	@Override
	public void handleProgress(String message) 
	{
		if (this.output != null) this.output.print(message);
	}

	/**
	 * Does nothing, you may wish to override in derived classes
	 */
	@Override
	public void handleProgress(BenchmarkQuery query, QueryRun run) { }

	/**
	 * Does nothing, you may wish to override in derived classes
	 */
	@Override
	public void handleProgress(QueryMixRun run) { }

	/**
	 * Starts benchmarking
	 * <p>
	 * If this class was derived from and you instantiated it without an explicit stream it will call the {@link #openStream()} method to try and open the stream for output.  Otherwise this function does nothing by default, if overriding this function you should ensure to always call <strong>super()</strong> unless you know that you will always have an explicit stream
	 * </p>
	 */
	@Override
	public void handleStarted(Benchmarker b)
	{ 
		//If the stream is null then use the openStream() method to try and get 
		if (this.output == null)
		{
			this.output = new PrintStream(this.openStream());
		}
	}

	/**
	 * Closes the stream if the closeOnFinish parameter was true when this listener was instantiated
	 */
	@Override
	public void handleFinished(boolean ok)
	{
		if (this.closeOnFinish && this.output != null)
		{
			this.output.close();
		}
	}

}
