/*
Copyright 2011-2014 Cray Inc. All Rights Reserved

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:

 * Redistributions of source code must retain the above copyright
  notice, this list of conditions and the following disclaimer.

 * Redistributions in binary form must reproduce the above copyright
  notice, this list of conditions and the following disclaimer in the
  documentation and/or other materials provided with the distribution.

 * Neither the name Cray Inc. nor the names of its contributors may be
  used to endorse or promote products derived from this software
  without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
 */

package net.sf.sparql.benchmarking.monitoring;

import java.io.OutputStream;
import java.io.PrintStream;

import net.sf.sparql.benchmarking.operations.Operation;
import net.sf.sparql.benchmarking.operations.OperationMix;
import net.sf.sparql.benchmarking.options.Options;
import net.sf.sparql.benchmarking.runners.Runner;
import net.sf.sparql.benchmarking.stats.OperationMixRun;
import net.sf.sparql.benchmarking.stats.OperationRun;

/**
 * A Progress Listener that prints informational messages to a
 * {@link PrintStream}
 * 
 * @author rvesse
 * 
 */
public class StreamProgressListener implements ProgressListener {
	private PrintStream output;
	private boolean closeOnFinish = true;
	private volatile long lastThread = -1;
	private boolean forceThreadedOutput = false;

	/**
	 * Creates a Progress Listener without a stream
	 * <p>
	 * Usable only by derived classes, the {@link #openStream()} method will be
	 * called to open a stream at the time benchmarking starts
	 * </p>
	 * 
	 * @param closeOnFinish
	 *            Whether the Output Stream should be closed when the listener
	 *            receives the {@link #handleFinished(boolean)} call
	 */
	protected StreamProgressListener(boolean closeOnFinish) {
		this.closeOnFinish = true;
	}

	/**
	 * Creates a Progress Listener without a stream
	 * <p>
	 * Usable only by derived classes, the {@link #openStream()} method will be
	 * called to open a stream at the time benchmarking starts
	 * </p>
	 */
	protected StreamProgressListener() {
		this(true);
	}

	/**
	 * Creates a new Progress Listener for the given stream
	 * 
	 * @param output
	 *            Output Stream
	 * @param closeOnFinish
	 *            Whether the Output Stream should be closed when the listener
	 *            receives the {@link #finish(Runner, Options, boolean)} call
	 */
	public StreamProgressListener(PrintStream output, boolean closeOnFinish) {
		if (output == null)
			throw new IllegalArgumentException("Ouput Stream cannot be null");
		this.output = output;
		this.closeOnFinish = closeOnFinish;
	}

	/**
	 * Creates a new Progress Listener for the given stream
	 * 
	 * @param output
	 *            Output Stream
	 */
	public StreamProgressListener(PrintStream output) {
		this(output, true);
	}

	/**
	 * Creates a new Progress Listener for the given stream
	 * 
	 * @param output
	 *            Output Stream
	 */
	public StreamProgressListener(OutputStream output) {
		this(new PrintStream(output));
	}

	/**
	 * Creates a new Progress Listener for the given stream
	 * 
	 * @param output
	 *            Output Stream
	 * @param closeOnFinish
	 *            Whether the stream should be closed when the
	 *            {@link #finish(Runner, Options, boolean)} event is received
	 */
	public StreamProgressListener(OutputStream output, boolean closeOnFinish) {
		this(new PrintStream(output), closeOnFinish);
	}

	/**
	 * Sets whether to force threaded output i.e. output is prefixed with thread
	 * information to disambiguate output from different threads. The normal
	 * behaviour is that output is only threaded when the options explicitly
	 * declare a number of parallel threads > 1 but sometimes there may be many
	 * threads in use even when parallel threads is set to 1 due to the
	 * interaction of different components within the system.
	 * 
	 * @param force
	 *            True if thread disambiguated output should be forced
	 *            regardless of the parallel threads declared by the
	 *            {@link Options}
	 */
	public void setForceThreadedOutput(boolean force) {
		this.forceThreadedOutput = force;
	}

	/**
	 * Internal method called only when this class is derived from and the
	 * protected constructors which do not take a stream argument have been used
	 * 
	 * @return Output Stream
	 */
	protected OutputStream openStream() {
		return null;
	}

	@Override
	public <T extends Options> void progress(Runner<T> runner, T options,
			String message) {
		if (this.output != null) {
			if (options.getParallelThreads() == 1 && !this.forceThreadedOutput) {
				this.output.print(message);
			} else {
				synchronized (this.output) {
					// If switching threads we need to insert a new line as
					// otherwise the output becomes very confusing
					if (this.hasSwitchedThreads()) {
						System.out.println();
					}

					// Update thread and include current thread in output
					this.updateThread();
					this.output.print(String.format("[Thread %d] %s",
							this.lastThread, message));
				}
			}
		}
	}

	protected final boolean hasSwitchedThreads() {
		if (this.lastThread == Thread.currentThread().getId())
			return false;
		return true;
	}

	protected synchronized void updateThread() {
		this.lastThread = Thread.currentThread().getId();
	}

	@Override
	public <T extends Options> void beforeOperation(Runner<T> runner,
			T options, Operation operation) {
	}

	@Override
	public <T extends Options> void afterOperation(Runner<T> runner, T options,
			Operation operation, OperationRun run) {
	}

	@Override
	public <T extends Options> void beforeOperationMix(Runner<T> runner,
			T options, OperationMix mix) {
	}

	@Override
	public <T extends Options> void afterOperationMix(Runner<T> runner,
			T options, OperationMix mix, OperationMixRun run) {
	}

	@Override
	public <T extends Options> void start(Runner<T> runner, T options) {
		// If the stream is null then use the openStream() method to try and get
		// a valid stream
		if (this.output == null) {
			this.output = new PrintStream(this.openStream());
		}
	}

	@Override
	public <T extends Options> void finish(Runner<T> runner, T options,
			boolean ok) {
		if (this.closeOnFinish && this.output != null) {
			this.output.close();
		}
	}

}
