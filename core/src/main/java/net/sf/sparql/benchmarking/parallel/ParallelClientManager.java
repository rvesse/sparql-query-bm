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

package net.sf.sparql.benchmarking.parallel;

import java.util.concurrent.Callable;

import net.sf.sparql.benchmarking.options.Options;
import net.sf.sparql.benchmarking.runners.Runner;

/**
 * Interface for parallel client managers
 * 
 * @author rvesse
 * 
 * @param <T>
 *            Options type
 */
public interface ParallelClientManager<T extends Options> extends Callable<Object> {

    /**
     * Method that will be called by parallel clients to determine if they
     * should continue to run, calls to this are thread safe
     * 
     * @return True if a client should continue to run, false if they should
     *         terminate
     */
    public abstract boolean shouldRun();

    /**
     * Method that will be called by parallel clients to indicate they have
     * started a new run
     * <p>
     * A boolean is returned indicating whether the client should actually go
     * ahead with the run, this is to help avoid race conditions where multiple
     * threads check {@link #shouldRun()} to see if they should proceed and then
     * attempt to start more runs than actually necessary.
     * </p>
     * 
     * @return True if the run should actually run, false otherwise
     */
    public abstract boolean startRun();

    /**
     * Method that will be called by parallel clients to indicate they have
     * completed a run and to obtain what run completion number it is
     * 
     * @return Run completion number
     */
    public abstract int completeRun();

    /**
     * Returns whether the parallel clients have finished all necessary runs
     * 
     * @return True if all runs have finished, false otherwise
     */
    public abstract boolean hasFinished();

    /**
     * Method called by parallel clients to tell the manager that they
     * encountered a halting condition and thus all clients should halt
     */
    public abstract void halt();

    /**
     * Gets whether the manager is ready for clients to begin executing
     * 
     * @return True if the manager is ready, false otherwise
     */
    public abstract boolean isReady();

    /**
     * Gets the runner
     * 
     * @return Runner
     */
    public abstract Runner<T> getRunner();

    /**
     * Gets the options
     * 
     * @return Options
     */
    public abstract T getOptions();

    /**
     * Creates a new parallel client
     * 
     * @param id
     *            Client ID
     * 
     * @return Parallel client
     */
    public abstract ParallelClient<T> createClient(int id);

}