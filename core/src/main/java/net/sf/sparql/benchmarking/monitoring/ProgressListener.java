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

import net.sf.sparql.benchmarking.operations.Operation;
import net.sf.sparql.benchmarking.options.Options;
import net.sf.sparql.benchmarking.runners.Runner;
import net.sf.sparql.benchmarking.stats.OperationMixRun;
import net.sf.sparql.benchmarking.stats.OperationRun;

/**
 * Interface for Progress Listeners that can be used to monitor progress of
 * benchmarking
 * 
 * @author rvesse
 * 
 */
public interface ProgressListener {
    /**
     * Handles starting of running
     * 
     * @param runner
     *            Runner
     * @param options
     *            Options
     */
    <T extends Options> void handleStarted(Runner<T> runner, T options);

    /**
     * Handles finishing of running
     * 
     * @param runner
     *            Runner
     * @param options
     *            Options
     * @param ok
     *            Indicates whether running finished normally, if false then
     *            some error condition caused running to be halted
     */
    <T extends Options> void handleFinished(Runner<T> runner, T options, boolean ok);

    /**
     * Handles an informational progress message
     * 
     * @param runner
     *            Runner
     * @param options
     *            Options
     * @param message
     *            Message
     */
    <T extends Options> void handleProgress(Runner<T> runner, T options, String message);

    /**
     * Handles statistics for a single run of an operation
     * 
     * @param runner
     *            Runner
     * @param options
     *            Options
     * @param operation
     *            Operation
     * @param run
     *            Run information
     */
    <T extends Options> void handleProgress(Runner<T> runner, T options, Operation operation, OperationRun run);

    /**
     * Handles statistics for a single run of the operation mix
     * 
     * @param runner
     *            Runner
     * @param options
     *            Options
     * @param run
     *            Mix run information
     */
    <T extends Options> void handleProgress(Runner<T> runner, T options, OperationMixRun run);
}
