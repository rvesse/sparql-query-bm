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

package net.sf.sparql.benchmarking.loader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.sf.sparql.benchmarking.operations.Operation;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract implementation of operation mix loader that uses a line based input
 * format. The parsing of individual lines is left to derived implementations.
 * 
 * @author rvesse
 * 
 */
public abstract class AbstractLineBasedMixLoader extends AbstractOperationMixLoader implements OperationMixLoader {

    static final Logger logger = LoggerFactory.getLogger(AbstractLineBasedMixLoader.class);

    @Override
    protected List<Operation> parseFile(File file) throws FileNotFoundException, IOException {
        List<Operation> ops = new ArrayList<Operation>();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        try {
            int lineNum = 1;
            String line = reader.readLine();
            while (StringUtils.isNotBlank(line)) {
                ops.add(this.parseLine(file.getParentFile(), line, lineNum));
                line = reader.readLine();
                lineNum++;
            }
        } finally {
            reader.close();
        }
        return ops;
    }

    /**
     * Parses a line into an operation
     * 
     * @param baseDir
     *            Base directory for resolving relative paths against where
     *            necessary
     * @param line
     *            Line to parse
     * @param lineNum
     *            Line number, can be used to provide better error messages
     * @return Operation
     * @throws IOException
     *             Thrown if the line does not represent a valid operation
     */
    protected abstract Operation parseLine(File baseDir, String line, int lineNum) throws IOException;

}