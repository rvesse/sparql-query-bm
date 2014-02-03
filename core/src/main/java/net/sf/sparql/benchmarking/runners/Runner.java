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

package net.sf.sparql.benchmarking.runners;

import net.sf.sparql.benchmarking.monitoring.ProgressListener;
import net.sf.sparql.benchmarking.operations.Operation;
import net.sf.sparql.benchmarking.options.Options;
import net.sf.sparql.benchmarking.stats.OperationMixRun;
import net.sf.sparql.benchmarking.stats.OperationRun;

/**
 * Interface for test runners
 * 
 * @author rvesse
 * 
 * @param <T>
 *            Options type
 */
public interface Runner<T extends Options> {

    /**
     * Runs with the given options
     * 
     * @param options
     */
    public abstract void run(T options);

    /**
     * Reports progress with the information from a single run of the operation
     * mix
     * 
     * @param options
     *            Options
     * @param run
     *            Operation mix run information
     */
    public abstract void reportProgress(T options, OperationMixRun run);

    /**
     * Reports progress with the information from a single run of a specific
     * operation
     * 
     * @param options
     *            Options
     * @param operation
     *            Operation
     * @param run
     *            Run information
     */
    public abstract void reportProgress(T options, Operation operation, OperationRun run);

    /**
     * Reports progress with an informational message
     * <p>
     * Messages passed to this function will always have a terminating newline
     * character added to them before being sent to listeners
     * </p>
     * <p>
     * You can configure what happens to the reporting messages by adding
     * {@link ProgressListener} instances with the
     * {@link Options#addListener(ProgressListener)} method
     * </p>
     * 
     * @param options
     *            Options
     * @param message
     *            Informational Message
     */
    public abstract void reportProgress(T options, String message);

    /**
     * Reports progress with an informational message
     * <p>
     * Messages passed to this function are sent to listeners as-is
     * </p>
     * 
     * @param options
     *            Options
     * @param message
     *            Informational Message
     */
    public abstract void reportPartialProgress(T options, String message);

    /**
     * Reports a newline as a progress message
     * 
     * @param options
     *            Options
     */
    public abstract void reportProgress(T options);

    /**
     * Requests that the run be halted, exact halting conditions and behaviour
     * is specified by the given options
     * 
     * @param options
     *            Options
     * @param e
     *            Exception
     */
    public abstract void halt(T options, Exception e);

    /**
     * Requests that the run be halted, exact halting conditions and behaviour
     * is specified by the given options
     * 
     * @param options
     *            Options
     * @param message
     *            Halting Message
     */
    public abstract void halt(T options, String message);
}
