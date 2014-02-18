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

package net.sf.sparql.benchmarking.loader.update;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import net.sf.sparql.benchmarking.loader.AbstractNvpOperationLoader;
import net.sf.sparql.benchmarking.loader.AbstractOperationLoader;
import net.sf.sparql.benchmarking.loader.OperationLoaderArgument;
import net.sf.sparql.benchmarking.operations.Operation;
import net.sf.sparql.benchmarking.operations.update.nvp.FixedNvpUpdateOperation;

/**
 * An operation loader for fixed query with NVP operations
 * 
 * @author rvesse
 * 
 */
public class FixedNvpUpdateOperationLoader extends AbstractNvpOperationLoader {

    @Override
    public Operation load(File baseDir, String[] args) throws IOException {
        if (args.length < 2)
            throw new IOException("Insufficient arguments to load a NVP update operation");

        String updateFile = args[0];
        String nvpFile = args[1];
        String name = updateFile;
        if (args.length > 2) {
            name = args[2];
        }

        // Read in data files
        String update = readFile(baseDir, updateFile);
        Map<String, List<String>> nvps = parseNvps(baseDir, nvpFile);
        return new FixedNvpUpdateOperation(name, update, nvps);
    }

    @Override
    public String getPreferredName() {
        return "nvp-update";
    }

    @Override
    public String getDescription() {
        return "The nvp-update operation makes a fixed SPARQL update adding custom name value parameters to the HTTP request which can be used to test custom behaviour provided by a SPARQL endpoint";
    }

    @Override
    public OperationLoaderArgument[] getArguments() {
        OperationLoaderArgument[] args = new OperationLoaderArgument[3];
        args[0] = new OperationLoaderArgument("Update File", "Provides a file that contains the SPARQL updates to be run.", OperationLoaderArgument.TYPE_FILE);
        args[1] = this.getNvpsArgument();
        args[2] = AbstractOperationLoader.getNameArgument(true);
        return args;
    }
}
