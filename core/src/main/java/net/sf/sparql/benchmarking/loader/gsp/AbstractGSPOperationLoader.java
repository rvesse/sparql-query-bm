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

package net.sf.sparql.benchmarking.loader.gsp;

import java.io.File;
import java.io.IOException;

import net.sf.sparql.benchmarking.loader.AbstractOperationLoader;
import net.sf.sparql.benchmarking.loader.OperationLoaderArgument;
import net.sf.sparql.benchmarking.operations.Operation;

/**
 * Abstract operation loader for basic GSP operations
 * 
 * @author rvesse
 * 
 */
public abstract class AbstractGSPOperationLoader extends AbstractOperationLoader {

    @Override
    public Operation load(File baseDir, String[] args) throws IOException {
        switch (args.length) {
        case 1:
            return createOperation(args[0]);
        case 2:
            return createOperation(args[0], args[1]);
        default:
            throw new IOException("Insufficient arguments to load a GSP operation");
        }
    }

    /**
     * Create a GSP operation that runs on the default graph
     * 
     * @param name
     *            Name
     * @return GSP operation
     */
    protected abstract Operation createOperation(String name);

    /**
     * Creates a GSP operation that runs on a specified graph
     * 
     * @param name
     *            Name
     * @param graphUri
     *            Graph URI
     * @return GSP operation
     */
    protected abstract Operation createOperation(String name, String graphUri);

    @Override
    public OperationLoaderArgument[] getArguments() {
        OperationLoaderArgument[] args = new OperationLoaderArgument[2];
        args[0] = AbstractOperationLoader.getNameArgument(false);
        args[1] = new OperationLoaderArgument("Graph URI", "Provides a Graph URI for the operation to operate over",
                OperationLoaderArgument.TYPE_STRING, true);
        return args;
    }
}
