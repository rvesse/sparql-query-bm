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

package net.sf.sparql.benchmarking.stats;

import net.sf.sparql.benchmarking.util.ErrorCategories;

/**
 * Represents statistics about a single run of an operation
 * 
 * @author rvesse
 * 
 */
public interface OperationRun extends Comparable<OperationRun> {

    /**
     * Constant used to indicate that an operation has not yet been run and thus
     * the statistic retrieved is not available
     */
    public static final long NOT_YET_RUN = Long.MIN_VALUE;

    /**
     * Constant used to indicate that an operation has some unknown statistic
     */
    public static final long UNKNOWN = -1;

    /**
     * Gets the runtime in nanoseconds
     * 
     * @return Runtime in nanoseconds
     */
    public abstract long getRuntime();

    /**
     * Gets the response time in nanoseconds
     * <p>
     * Response Time is an additional metric which currently is only calculated
     * for certain operations. This metric is the time taken for the first
     * results of the operation to become available so where the operation
     * receives a streamed response this may be much smaller than the
     * {@link #getRuntime()} statistic.
     * </p>
     * 
     * @return Response Time in nanoseconds
     */
    public abstract long getResponseTime();

    /**
     * Gets the number of results returned for operations to which this is
     * applicable
     * 
     * @return Number of results
     */
    public abstract long getResultCount();

    /**
     * Returns whether this run represents a successful (i.e. non-error) run of
     * the operation
     * 
     * @return True if the run was successful
     */
    public abstract boolean wasSuccessful();

    /**
     * Gets the error message associated with the operation run, will be null if
     * the operation ran successfully
     * 
     * @return Error Message or null
     */
    public abstract String getErrorMessage();

    /**
     * Gets an integer identifying the category of error
     * <p>
     * Some basic categories are provided by {@link ErrorCategories} and the
     * return type of this method is explicitly an {@code int} rather than some
     * category {@code enum} in order to allow users of the API to define their
     * own error categories for their custom operations.
     * 
     * @return Error category
     */
    public abstract int getErrorCategory();

    /**
     * Gets the global run order for this operation run
     * <p>
     * This is the order the operation was run in relative to all other
     * operations run in the benchmark and is primarily useful when looking at
     * the results of multi-threaded benchmarks.
     * </p>
     * 
     * @return Global Order
     */
    public abstract long getRunOrder();

    /**
     * Sets the global run order for this operation run
     * <p>
     * Only for internal use to allow setting the order of this operation
     * relative to all other operations run in the benchmark, this is primarily
     * useful when tracking multi-threaded benchmarks. Trying to set this once
     * it has been set will lead to an {@link IllegalAccessError}
     * </p>
     * 
     * @param order
     *            Order
     * @throws IllegalAccessError
     *             Thrown if you try to set the run order after it has been set
     */
    public abstract void setRunOrder(long order) throws IllegalAccessError;

}