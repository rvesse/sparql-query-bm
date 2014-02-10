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
 * Abstract implementation of an operation run
 * 
 * @author rvesse
 * 
 */
public abstract class AbstractOperationRun implements OperationRun {

    private long runtime = NOT_YET_RUN;
    private long responseTime = NOT_YET_RUN;
    private long resultCount = NOT_YET_RUN;
    private String errorMessage;
    private int errorCategory = ErrorCategories.NONE;
    private long order = NOT_YET_RUN;

    /**
     * Creates a operation run which represents that the running of an operation
     * resulted in an error
     * 
     * @param error
     *            Error Message
     * @param runtime
     *            Runtime, this is the amount of time elapsed until the
     *            error/timeout was reached
     */
    protected AbstractOperationRun(String error, int errorCategory, long runtime) {
        this(runtime, UNKNOWN);
        this.errorMessage = error;
        this.errorCategory = errorCategory;
    }

    /**
     * Creates an operation run which represents the results of running an
     * operation
     * 
     * @param runtime
     *            Runtime
     * @param resultCount
     *            Result Count
     */
    protected AbstractOperationRun(long runtime, long resultCount) {
        this.runtime = runtime;
        this.resultCount = resultCount;
    }

    /**
     * Creates an operation run which represents the results of running an
     * operation
     * 
     * @param runtime
     *            Runtime
     * @param responseTime
     *            Response Time
     * @param resultCount
     *            Result Count
     */
    protected AbstractOperationRun(long runtime, long responseTime, long resultCount) {
        this(runtime, resultCount);
        this.responseTime = responseTime;
    }

    @Override
    public long getRuntime() {
        return this.runtime;
    }

    @Override
    public long getResponseTime() {
        if (this.responseTime == NOT_YET_RUN) {
            return this.runtime;
        } else {
            return this.responseTime;
        }
    }

    @Override
    public long getResultCount() {
        return this.resultCount;
    }

    @Override
    public boolean wasSuccessful() {
        return this.errorMessage == null;
    }

    @Override
    public String getErrorMessage() {
        return this.errorMessage;
    }
    
    @Override
    public int getErrorCategory() {
        return this.errorCategory;
    }

    @Override
    public long getRunOrder() {
        return this.order;
    }

    @Override
    public void setRunOrder(long order) throws IllegalAccessError {
        if (this.order == NOT_YET_RUN) {
            this.order = order;
        } else {
            throw new IllegalAccessError("Cannot set the run order after it has been set");
        }
    }

    /**
     * Compares a run to another
     * <p>
     * Used for sorting the runs so outliers can be trimmed
     * </p>
     */
    @Override
    public int compareTo(OperationRun other) {
        long otherRuntime = other.getRuntime();
        if (runtime < otherRuntime) {
            return -1;
        } else if (runtime > otherRuntime) {
            return 1;
        } else {
            return 0;
        }
    }

}