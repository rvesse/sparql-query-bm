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


/**
 * Helper class with utility methods related to formatting
 * 
 * @author rvesse
 * 
 */
public class FormatUtils {

    /**
     * Private constructor prevents direct instantiation
     */
    private FormatUtils() {
    }

    /**
     * Formats Time to show as seconds
     * 
     * @param time
     *            Time in nanoseconds
     * @return Time in seconds
     */
    public static String formatSeconds(long time) {
        return ConvertUtils.toSeconds(time) + "s";
    }

    /**
     * Formats Time to show as seconds
     * 
     * @param time
     *            Time in nanoseconds
     * @return Time in seconds
     */
    public static String formatSeconds(double time) {
        return ConvertUtils.toSeconds(time) + "s";
    }

    /**
     * Formats a string for CSV escaping it as a double quoted CSV string if
     * necessary
     * 
     * @param value
     * @return Sanitized string
     */
    public static String toCsv(String value) {
        if (value.contains(",") || value.startsWith("\"") || value.endsWith("\"")) {
            return "\"" + FormatUtils.escapeQuotesForCsv(value) + "\"";
        } else {
            return value;
        }
    }

    /**
     * Escapes quotes in a string for use in a double quoted CSV string
     * 
     * @param value
     * @return Sanitized string
     */
    public static String escapeQuotesForCsv(String value) {
        if (value.contains("\"")) {
            return value.replace("\"", "\"\"");
        } else {
            return value;
        }
    }
}
