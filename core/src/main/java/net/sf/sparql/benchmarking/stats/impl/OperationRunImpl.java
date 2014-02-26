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

package net.sf.sparql.benchmarking.stats.impl;


/**
 * A general purpose operation run implementation
 * 
 * @author rvesse
 * 
 */
public final class OperationRunImpl extends AbstractOperationRun {

    /**
     * Creates an operation which represents the results of successfully running
     * an operation
     * 
     * @param runtime
     *            Runtime
     */
    public OperationRunImpl(long runtime) {
        super(runtime, 0);
    }

    /**
     * Creates a operation run which represents that the failed running of an
     * operation
     * 
     * @param error
     *            Error Message
     * @param category
     *            Error category
     * @param runtime
     *            Runtime, this is the amount of time elapsed until the error
     *            was reached
     */
    public OperationRunImpl(String error, int category, long runtime) {
        super(error, category, runtime);
    }

    /**
     * Creates an operation run which represents the results of successfully
     * running an operation
     * 
     * @param runtime
     *            Runtime
     * @param resultCount
     *            Result Count
     */
    public OperationRunImpl(long runtime, long resultCount) {
        super(runtime, resultCount);
    }

    /**
     * Creates an operation run which represents the results of successfully
     * running an operation
     * 
     * @param runtime
     *            Runtime
     * @param responseTime
     *            Response Time
     * @param resultCount
     *            Result Count
     */
    public OperationRunImpl(long runtime, long responseTime, long resultCount) {
        super(runtime, responseTime, resultCount);
    }
}
