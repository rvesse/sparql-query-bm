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

package net.sf.sparql.benchmarking.loader.query;

import java.io.File;
import java.io.IOException;

import net.sf.sparql.benchmarking.loader.AbstractOperationLoader;
import net.sf.sparql.benchmarking.loader.OperationLoaderArgument;
import net.sf.sparql.benchmarking.operations.Operation;
import net.sf.sparql.benchmarking.operations.query.DatasetSizeOperation;

/**
 * Loader for dataset size operation
 * 
 * @author rvesse
 * 
 */
public class DatasetSizeOperationLoader extends AbstractOperationLoader {

    @Override
    public Operation load(File baseDir, String[] args) throws IOException {
        if (args.length == 0) {
            return new DatasetSizeOperation();
        } else {
            return new DatasetSizeOperation(args[0]);
        }
    }

    @Override
    public String getPreferredName() {
        return "dataset-size";
    }

    @Override
    public String getDescription() {
        return "The dataset-size operation makes a SPARQL query that counts all the quads in the dataset.  This operation can be used on both remote SPARQL servers and on in-memory datasets.";
    }

    @Override
    public OperationLoaderArgument[] getArguments() {
        OperationLoaderArgument[] args = new OperationLoaderArgument[1];
        args[0] = AbstractOperationLoader.getNameArgument(true);
        return args;
    }

}
