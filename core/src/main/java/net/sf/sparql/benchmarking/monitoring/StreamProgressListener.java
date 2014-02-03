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
     *            receives the {@link #handleFinished(Runner, Options, boolean)}
     *            call
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
     *            {@link #handleFinished(Runner, Options, boolean)} event is
     *            received
     */
    public StreamProgressListener(OutputStream output, boolean closeOnFinish) {
        this(new PrintStream(output), closeOnFinish);
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

    /**
     * Writes the message to the underlying {@link PrintStream}
     * 
     * @param message
     *            Informational Message
     */
    @Override
    public <T extends Options> void handleProgress(Runner<T> runner, T options, String message) {
        if (this.output != null)
            this.output.print(message);
    }

    /**
     * Does nothing, you may wish to override in derived classes
     */
    @Override
    public <T extends Options> void handleProgress(Runner<T> runner, T options, Operation operation, OperationRun run) {
    }

    /**
     * Does nothing, you may wish to override in derived classes
     */
    @Override
    public <T extends Options> void handleProgress(Runner<T> runner, T options, OperationMixRun run) {
    }

    /**
     * Starts benchmarking
     * <p>
     * If this class was derived from and you instantiated it without an
     * explicit stream it will call the {@link #openStream()} method to try and
     * open the stream for output. Otherwise this function does nothing by
     * default, if overriding this function you should ensure to always call
     * <strong>super()</strong> unless you know that you will always have an
     * explicit stream
     * </p>
     */
    @SuppressWarnings("javadoc")
    @Override
    public <T extends Options> void handleStarted(Runner<T> runner, T options) {
        // If the stream is null then use the openStream() method to try and get
        if (this.output == null) {
            this.output = new PrintStream(this.openStream());
        }
    }

    /**
     * Closes the stream if the closeOnFinish parameter was true when this
     * listener was instantiated
     */
    @Override
    public <T extends Options> void handleFinished(Runner<T> runner, T options, boolean ok) {
        if (this.closeOnFinish && this.output != null) {
            this.output.close();
        }
    }

}
