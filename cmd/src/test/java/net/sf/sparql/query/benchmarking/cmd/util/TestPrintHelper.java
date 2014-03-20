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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import net.sf.sparql.benchmarking.commands.util.PrintHelper;
import net.sf.sparql.benchmarking.loader.AbstractNvpOperationLoader;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the {@link PrintHelper}
 * 
 * @author rvesse
 * 
 */
public class TestPrintHelper {

    private void test(String input, int indent, int columnWidth, String expected) {
        PrintStream stdout = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));
        try {
            PrintHelper.print(input, indent, columnWidth);
            String actual = new String(output.toByteArray());
            actual = actual.trim();

            // stdout.println("Actual:");
            // stdout.println(actual);
            // stdout.println("Expected:");
            // stdout.println(expected);

            Assert.assertEquals(expected, actual);
        } finally {
            System.setOut(stdout);
        }
    }

    @Test
    public void print_wrapping_01() {
        test("test", 0, 100, "test");
    }

    @Test
    public void print_wrapping_02() {
        test("test", 4, 100, "test");
    }

    @Test
    public void print_wrapping_03() {
        test("test wrapping", 4, 5, "test\n    wrap-\n    ping");
    }

    @Test
    public void print_wrapping_04() {
        test("test wrapping", 0, 5, "test\nwrap-\nping");
    }

    @Test
    public void print_wrapping_05() {
        test("test wrapping", 0, 10, "test\nwrapping");
    }

    @Test
    public void print_wrapping_06() {
        test(AbstractNvpOperationLoader.getNvpsArgument().getDescription(),
                2,
                100,
                "Provides the path to a file containing NVPs.  This file may in either TSV or Java Properties format.\n"
                        + "  In TSV format the first column of each line is taken to be the property name and all subsequent\n"
                        + "  columns are treated as values associated with that name.\n"
                        + "  In Java properties format the standard Java properties text based format is supported, each property\n"
                        + "  name is treated as a name and its value associated with that name.  When using Java properties\n"
                        + "  format only a single value may be associated with each name due to the way that Java parses the\n"
                        + "  format.");
    }
}
