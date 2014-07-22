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

package net.sf.sparql.benchmarking.util;

import java.util.concurrent.TimeUnit;

/**
 * Helper class with utility methods for converting between different units
 * <p>
 * Note some of these functions mimic the functionality of
 * {@link TimeUnit#convert(long, TimeUnit)} except they keep the calculations as
 * doubles which is important for us because we want as much precision in our
 * measurements as possible
 * </p>
 * 
 * @author rvesse
 * 
 */
public class ConvertUtils {

    /**
     * Nanoseconds per millisecond
     */
    public static final double NANOSECONDS_PER_MILLISECONDS = 1000000;
    /**
     * Milliseconds per second
     */
    public static final double MILLISECONDS_PER_SECONDS = 1000;
    /**
     * Seconds per minute
     */
    public static final double SECONDS_PER_MINUTE = 60;
    /**
     * Seconds per hour
     */
    public static final double SECONDS_PER_HOUR = 3600;

    /**
     * Private constructor prevents direct instantiation
     */
    private ConvertUtils() {
    }

    /**
     * Converts nanoseconds to milliseconds
     * 
     * @param nanoseconds
     *            Nanoseconds
     * @return Milliseconds
     */
    public static double toMilliseconds(double nanoseconds) {
        return nanoseconds / ConvertUtils.NANOSECONDS_PER_MILLISECONDS;
    }

    /**
     * Converts nanoseconds to milliseconds
     * 
     * @param nanoseconds
     *            Nanoseconds
     * @return Milliseconds
     */
    public static double toMilliseconds(long nanoseconds) {
        return toMilliseconds((double) nanoseconds);
    }

    /**
     * Converts nanoseconds squared to milliseconds squared
     * 
     * @param nanosecondsSquared
     *            Nanoseconds squared
     * @return Milliseconds squared
     */
    public static double toMillisecondsSquared(long nanosecondsSquared) {
        return toMillisecondsSquared((double) nanosecondsSquared);
    }

    /**
     * Converts nanoseconds squared to milliseconds squared
     * 
     * @param nanosecondsSquared
     *            Nanoseconds squared
     * @return Milliseconds squared
     */
    public static double toMillisecondsSquared(double nanosecondsSquared) {
        return nanosecondsSquared / Math.pow(ConvertUtils.NANOSECONDS_PER_MILLISECONDS, 2);
    }

    /**
     * Converts nanoseconds to seconds
     * 
     * @param nanoseconds
     *            Nanoseconds
     * @return Seconds
     */
    public static double toSeconds(long nanoseconds) {
        return ConvertUtils.toSeconds((double) nanoseconds);
    }

    /**
     * Converts nanoseconds to seconds
     * 
     * @param nanoseconds
     *            Nanoseconds
     * @return Seconds
     */
    public static double toSeconds(double nanoseconds) {
        double ms = toMilliseconds(nanoseconds);
        return ms / ConvertUtils.MILLISECONDS_PER_SECONDS;
    }

    /**
     * Converts nanoseconds squared to seconds squared
     * 
     * @param nanoseconds
     *            Nanoseconds squared
     * @return Seconds squared
     */
    public static double toSecondsSquared(long nanosecondsSquared) {
        return ConvertUtils.toSecondsSquared((double) nanosecondsSquared);
    }

    /**
     * Converts nanoseconds squared to seconds squared
     * 
     * @param nanoseconds
     *            Nanoseconds squared
     * @return Seconds squared
     */
    public static double toSecondsSquared(double nanosecondsSquared) {
        double ms = toMillisecondsSquared(nanosecondsSquared);
        return ms / Math.pow(ConvertUtils.MILLISECONDS_PER_SECONDS, 2);
    }

    /**
     * Converts nanoseconds to minutes
     * 
     * @param nanoseconds
     *            Nanoseconds
     * @return Minutes
     */
    public static double toMinutes(long nanoseconds) {
        return ConvertUtils.toMinutes((double) nanoseconds);
    }

    /**
     * Converts nanoseconds to minutes
     * 
     * @param nanoseconds
     *            Nanoseconds
     * @return Minutes
     */
    public static double toMinutes(double nanoseconds) {
        double seconds = toSeconds(nanoseconds);
        return seconds / ConvertUtils.SECONDS_PER_MINUTE;
    }
}
