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

package net.sf.sparql.benchmarking.runners.mix;

import net.sf.sparql.benchmarking.runners.mix.ordering.SamplingMixOrderProvider;

/**
 * An operation mix runner that runs a sample of the operations in the mix
 * respecting whether random order has been selected.
 * <p>
 * The sample is configurable and may be larger/smaller than the size of the
 * mixes to be run, optionally the sample may include repeats of operations.
 * </p>
 * <p>
 * In the case where random order is disabled and the sample size is smaller
 * than the mix size only the first {@code N} operations (where {@code N} is the
 * sample size) will be run and the remaining operations will never be run.
 * </p>
 * <p>
 * In the case where the desired sample size is larger than the mix size but
 * repeats are not allowed then the actual sample size will be the same as the
 * mix size and every operation will be run precisely once.
 * </p>
 * 
 * @author rvesse
 * 
 */
public class SamplingOperationMixRunner extends AbstractOperationMixRunner {

    /**
     * Creates a sampling mix runner with the given sample size
     * 
     * @param sampleSize
     *            Sample size, if <= 0 then sample size will always be the mix
     *            size
     */
    public SamplingOperationMixRunner(int sampleSize) {
        this(sampleSize, false);
    }

    /**
     * Creates a sampling mix runner which optionally allows repeats, the sample
     * size will always be the mix size
     * 
     * @param allowRepeats
     *            Whether to allow repeats
     */
    public SamplingOperationMixRunner(boolean allowRepeats) {
        this(0, allowRepeats);
    }

    /**
     * Creates a sampling mix runner with the given sample size that optionally
     * allows repeats
     * 
     * @param sampleSize
     *            Sample size, if <= 0 then sample size will always be the mix
     *            size
     * @param allowRepeats
     *            Whether to allow repeats
     */
    public SamplingOperationMixRunner(int sampleSize, boolean allowRepeats) {
        super(new SamplingMixOrderProvider(sampleSize, allowRepeats));
    }

}
