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
import net.sf.sparql.benchmarking.operations.OperationMix;
import net.sf.sparql.benchmarking.options.Options;
import net.sf.sparql.benchmarking.runners.Runner;
import net.sf.sparql.benchmarking.stats.OperationMixRun;
import net.sf.sparql.benchmarking.stats.OperationRun;

/**
 * Interface for Progress Listeners that can be used to monitor progress of test
 * runs
 * 
 * @author rvesse
 * 
 */
public interface ProgressListener {
    /**
     * Invoked when test runs start
     * 
     * @param runner
     *            Runner
     * @param options
     *            Options
     */
    <T extends Options> void start(Runner<T> runner, T options);

    /**
     * Invoked when test runs finish
     * 
     * @param runner
     *            Runner
     * @param options
     *            Options
     * @param ok
     *            Indicates whether running finished normally, if false then
     *            some error condition caused running to be halted
     */
    <T extends Options> void finish(Runner<T> runner, T options, boolean ok);

    /**
     * Invoked when an informational progress message is available
     * 
     * @param runner
     *            Runner
     * @param options
     *            Options
     * @param message
     *            Message
     */
    <T extends Options> void progress(Runner<T> runner, T options, String message);

    /**
     * Invoked before each run of an operation
     * 
     * @param runner
     *            Runner
     * @param options
     *            Options
     * @param operation
     *            Operation
     */
    <T extends Options> void beforeOperation(Runner<T> runner, T options, Operation operation);

    /**
     * Invoked after each run of an operation
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
    <T extends Options> void afterOperation(Runner<T> runner, T options, Operation operation, OperationRun run);

    /**
     * Invoked before each run of an operation mix
     * 
     * @param runner
     *            Runner
     * @param options
     *            Options
     * @param mix
     *            Operation Mix
     */
    <T extends Options> void beforeOperationMix(Runner<T> runner, T options, OperationMix mix);

    /**
     * Invoked after each run of an operation mix
     * 
     * @param runner
     *            Runner
     * @param options
     *            Options
     * @param mix
     *            TODO
     * @param run
     *            Mix run information
     */
    <T extends Options> void afterOperationMix(Runner<T> runner, T options, OperationMix mix, OperationMixRun run);
}
