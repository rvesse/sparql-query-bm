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

package net.sf.sparql.benchmarking.options;

import net.sf.sparql.benchmarking.runners.mix.DefaultOperationMixRunner;

/**
 * Options for soak testing
 * 
 * @author rvesse
 * 
 */
public class SoakOptions extends OptionsImpl {

    /**
     * Default maximum number of runs for soak testing which is 0 indicating
     * that runs will continue until the specified runtime is exceeded
     */
    public static final int DEFAULT_MAX_RUNS = 0;
    /**
     * Default soak testing runtime threshold in minutes
     */
    public static final long DEFAULT_RUNTIME = 15;

    private int maxRuns = DEFAULT_MAX_RUNS;
    private long runtime = DEFAULT_RUNTIME;

    /**
     * Creates new soak options
     */
    public SoakOptions() {
        super.setMixRunner(new DefaultOperationMixRunner());
    }

    /**
     * Sets the maximum number of runs for soak testing
     * <p>
     * May be set to 0 or a negative value to indicate there is no maximum runs
     * and that instead the {@link #getMaxRuntime()} determines how long soak
     * testing will run for.
     * </p>
     * 
     * @param runs
     *            Maximum runs
     */
    public void setMaxRuns(int runs) {
        this.maxRuns = runs;
    }

    /**
     * Gets the maximum number of runs for soak testing
     * 
     * @return Maximum number of runs
     */
    public int getMaxRuns() {
        return this.maxRuns;
    }

    /**
     * Gets the maximum soak runtime in minutes
     * 
     * 
     * @return Soak runtime in minutes
     */
    public long getMaxRuntime() {
        return runtime;
    }

    /**
     * Sets the maximum soak runtime in minutes
     * <p>
     * May be set to 0 or a negative value to indicate that there is no maximum
     * runtime and instead the {@link #getMaxRuns()} determines how many runs
     * soak testing will consist of.
     * </p>
     * 
     * @param runtime
     *            Soak runtime in minutes
     */
    public void setMaxRuntime(long runtime) {
        this.runtime = runtime;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Options> T copy() {
        SoakOptions copy = new SoakOptions();
        this.copyStandardOptions(copy);
        copy.setMaxRuns(this.getMaxRuns());
        copy.setMaxRuntime(this.getMaxRuntime());
        return (T) copy;
    }
}
