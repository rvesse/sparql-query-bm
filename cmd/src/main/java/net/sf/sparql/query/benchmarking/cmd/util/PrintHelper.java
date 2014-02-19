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

package net.sf.sparql.query.benchmarking.cmd.util;

/**
 * Helper for pretty printing
 * 
 * @author rvesse
 * 
 */
public class PrintHelper {

    /**
     * Prints an indent using spaces
     * 
     * @param indent
     *            Amount of indent
     */
    public static void printIndent(int indent) {
        if (indent <= 0)
            return;
        printChars(' ', indent);
    }

    /**
     * Prints the given character a given number of times
     * 
     * @param c
     *            Character
     * @param repeats
     *            Number of times to print
     */
    public static void printChars(char c, int repeats) {
        if (repeats <= 0)
            return;
        for (int i = 0; i < repeats; i++) {
            System.out.print(c);
        }
    }

    /**
     * Prints a string wrapping it as necessary while respecting any explicit
     * line breaks
     * 
     * @param value
     *            Value to print
     * @param indent
     *            Indent
     * @param columnWidth
     *            Column width to wrap to, this excludes any indent so callers
     *            should take their desired indent into account when calculating
     *            their desired column width.
     */
    public static void print(String value, int indent, int columnWidth) {
        int newLineIndex = value.indexOf('\n');
        if (newLineIndex == -1) {
            // No line breaks so just print with wrapping
            printWithWrapping(value, indent, columnWidth);
        } else {
            // Some line breaks present
            int start = 0;
            while (newLineIndex > -1) {
                // Print the portion up to the line break with wrapping
                String partialValue = value.substring(start, newLineIndex);
                printWithWrapping(partialValue, indent, columnWidth);
                printIndent(indent);

                // Find next line break
                start = newLineIndex + 1;
                newLineIndex = value.indexOf('\n', start);
            }
            // Print the remaining portion with wrapping
            printWithWrapping(value.substring(start), indent, columnWidth);
        }
    }

    private static void printWithWrapping(String value, int indent, int columnWidth) {
        if (columnWidth < 1)
            throw new IllegalArgumentException("Column width must be >= 1");

        if (value.length() <= columnWidth) {
            // Fits within column so no wrapping required
            System.out.println(value);
        } else {
            // Wrapping required
            int start = 0;
            while (true) {
                // Once we can fit within the column width print remaining
                // portion and exit
                if (value.length() - start <= columnWidth) {
                    printWithWrapping(value.substring(start), indent, columnWidth);
                    return;
                }

                // Find next place to break and print relevant portion with
                // wrapping
                int spaceIndex = findPrecedingBreak(value, start + columnWidth);
                if (spaceIndex <= start) {
                    // It may be possible that there is no suitable break point
                    // in the line in which case we have to introduce a hyphen
                    String partialValue = value.substring(start, start + columnWidth - 1) + "-";
                    printWithWrapping(partialValue, indent, columnWidth);
                    printIndent(indent);
                    start = start + columnWidth - 1;
                    continue;
                }

                // Successfully broke the line at some whitespace
                String partialValue = value.substring(start, spaceIndex);
                printWithWrapping(partialValue, indent, columnWidth);
                printIndent(indent);
                start = spaceIndex + 1;
            }
        }
    }

    /**
     * Finds the nearest place prior to the given index break up the value
     * 
     * @param value
     *            Value
     * @param index
     *            Index
     * @return Break index
     */
    private static int findPrecedingBreak(String value, int index) {
        char c = value.charAt(index);
        if (Character.isWhitespace(c))
            return index;
        while (!Character.isWhitespace(c) && index >= 0) {
            index--;
            c = value.charAt(index);
        }
        return index;
    }
}
