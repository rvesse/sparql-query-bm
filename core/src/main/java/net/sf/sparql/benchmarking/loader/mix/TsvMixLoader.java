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

package net.sf.sparql.benchmarking.loader.mix;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import net.sf.sparql.benchmarking.loader.AbstractLineBasedMixLoader;
import net.sf.sparql.benchmarking.loader.OperationLoader;
import net.sf.sparql.benchmarking.loader.OperationLoaderRegistry;
import net.sf.sparql.benchmarking.operations.Operation;

/**
 * An operation mix loader that provides support for the new tab separated mix
 * file format introduced in the 2.x releases
 * 
 * @author rvesse
 * 
 */
public class TsvMixLoader extends AbstractLineBasedMixLoader {

    @Override
    public String getPreferredExtension() {
        return "tsv";
    }

    @Override
    protected Operation parseLine(File baseDir, String line, int lineNum) throws IOException {
        String[] fields = line.split("\t");
        if (fields.length == 0)
            throw new IOException(String.format("Line %d: Expected a tab separated line but no tabs present", lineNum));
        OperationLoader loader = OperationLoaderRegistry.getLoader(fields[0]);
        if (loader == null)
            throw new IOException(String.format("Line %d: No OperationLoader is registered for the operation type %s",
                    lineNum, fields[0]));
        String[] args = Arrays.copyOfRange(fields, 1, fields.length);
        try {
            return loader.load(baseDir, args);
        } catch (IOException e) {
            // Add offending line number to any loading errors
            throw new IOException(String.format("Line %d: %s", lineNum, e.getMessage()));
        }
    }

}
