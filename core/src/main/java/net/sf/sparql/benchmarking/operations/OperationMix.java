/** 
 * Copyright 2011-2014 Cray Inc. All Rights Reserved
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
package net.sf.sparql.benchmarking.operations;

import java.util.Iterator;

import net.sf.sparql.benchmarking.options.Options;
import net.sf.sparql.benchmarking.runners.Runner;
import net.sf.sparql.benchmarking.stats.OperationMixRun;
import net.sf.sparql.benchmarking.stats.OperationMixStats;

/**
 * Represents a mix of operations carried out as a single test run
 * 
 * @author rvesse
 * 
 */
public interface OperationMix {

    /**
     * Gets the operations in this mix
     * 
     * @return Operations
     */
    public abstract Iterator<Operation> getOperations();

    /**
     * Gets the operation with the specified ID
     * 
     * @param id
     * @return Operation
     */
    public abstract Operation getOperation(int id);

    /**
     * Gets the number of operations in the operation mix
     * 
     * @return Number of operations
     */
    public abstract int size();

    /**
     * Sets whether the operation mix is being run as a thread, if so it will
     * prefix thread identifier to its progress messages
     * 
     * @param asThread
     *            Whether the operation mix is being run as a thread
     */
    public abstract void setRunAsThread(boolean asThread);

    /**
     * Performs a operation mix run returning the statistics as a
     * {@link OperationMixRun}
     * <p>
     * Implementations are also expected to record the information within their
     * local {@link OperationMixStats} object prior to returning the statistics.
     * </p>
     * 
     * @param runner
     *            Runner
     * @param options
     *            Options
     * @return Operation Mix run details
     */
    public abstract <T extends Options> OperationMixRun run(Runner<T> runner, T options);

    /**
     * Gets the statistics for the operation mix
     * 
     * @return Statistics
     */
    public abstract OperationMixStats getStats();

}