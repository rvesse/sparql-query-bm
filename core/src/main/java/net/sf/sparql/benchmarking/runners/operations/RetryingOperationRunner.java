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

package net.sf.sparql.benchmarking.runners.operations;

import net.sf.sparql.benchmarking.operations.Operation;
import net.sf.sparql.benchmarking.options.Options;
import net.sf.sparql.benchmarking.runners.Runner;
import net.sf.sparql.benchmarking.stats.OperationRun;

/**
 * An operation runner that will retry the given operation if it produces an
 * error
 * 
 * @author rvesse
 * 
 */
public class RetryingOperationRunner extends DefaultOperationRunner {

    private int maxRetries;

    /**
     * Creates a new runner
     * 
     * @param maxRetries
     *            Maximum number of retries, a value of 0 means no retries in
     *            which case behaviour is equivalent to the base class
     *            {@link DefaultOperationRunner}
     */
    public RetryingOperationRunner(int maxRetries) {
        if (maxRetries < 0)
            throw new IllegalArgumentException("maxRetries must be >= 0");
        this.maxRetries = maxRetries;
    }

    @Override
    public <T extends Options> OperationRun run(Runner<T> runner, T options, Operation op) {
        if (this.maxRetries == 0) {
            return super.run(runner, options, op);
        } else {
            OperationRun r = null;
            for (int attempt = 1; attempt <= this.maxRetries + 1; attempt++) {
                r = super.run(runner, options, op);

                // Stop as soon as successful
                if (r.wasSuccessful())
                    break;

                // If not successful and we still have retries available to us
                // report that we are retrying
                if (attempt < this.maxRetries) {
                    runner.reportProgress(options, String.format("Operation %s errored on attempt %d of %d, retrying...",
                            op.getName(), attempt, this.maxRetries + 1));
                } else {
                    runner.reportProgress(options,
                            String.format("Operation %s errored on all %d attempts", op.getName(), this.maxRetries + 1));
                }
            }
            return r;
        }
    }
}
