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

package net.sf.sparql.benchmarking.operations;

import net.sf.sparql.benchmarking.options.Options;
import net.sf.sparql.benchmarking.runners.Runner;
import net.sf.sparql.benchmarking.stats.OperationRun;
import net.sf.sparql.benchmarking.stats.OperationStats;

/**
 * Represents a single operation within a testing run
 * 
 * @author rvesse
 * 
 */
public interface Operation {

    /**
     * Gets the name of the operation
     * <p>
     * In the 1.x releases this was almost always the filename but in the 2.x
     * code base this is often a friendly user defined name
     * </p>
     * 
     * @return Name
     */
    public abstract String getName();

    /**
     * Report whether the operation can run based on the available options
     * 
     * @param runner
     *            Runner
     * @param options
     *            Options
     * @return True if the operation can run, false otherwise
     */
    public abstract <T extends Options> boolean canRun(Runner<T> runner, T options);

    /**
     * Creates the callable for running the operation in a background thread
     * 
     * @param runner
     *            Runner
     * @param options
     *            Options
     * @return Callable
     */
    public abstract <T extends Options> OperationCallable<T> createCallable(Runner<T> runner, T options);

    /**
     * Creates run information for an error
     * 
     * @param message
     *            Error message
     * @param category
     *            Error category
     * @param runtime
     *            Runtime
     * @return Error information
     */
    public abstract OperationRun createErrorInformation(String message, int category, long runtime);

    /**
     * Gets a descriptive type string for the operation e.g. SPARQL Query
     * 
     * @return Type string
     */
    public abstract String getType();

    /**
     * Gets a string that shows the content of the operation e.g. SPARQL Query
     * string, SPARQL Update string etc.
     * 
     * @return Content string
     */
    public abstract String getContentString();

    /**
     * Gets statistics for the operation
     * 
     * @return Operation statistics
     */
    public abstract OperationStats getStats();

    /**
     * Gets the ID used to identify the operation within an operation mix (if
     * known)
     * 
     * @return ID or -1 if no ID unknown
     */
    public int getId();

    /**
     * Sets the ID used to identify the operation within an operation mix
     * 
     * @param id
     *            ID
     */
    public void setId(int id);

}