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

package net.sf.sparql.benchmarking.loader.util;

import java.io.File;
import java.io.IOException;

import net.sf.sparql.benchmarking.loader.AbstractOperationLoader;
import net.sf.sparql.benchmarking.loader.OperationLoaderArgument;
import net.sf.sparql.benchmarking.loader.OperationMixLoader;
import net.sf.sparql.benchmarking.loader.OperationMixLoaderRegistry;
import net.sf.sparql.benchmarking.operations.Operation;
import net.sf.sparql.benchmarking.operations.OperationMix;
import net.sf.sparql.benchmarking.operations.util.MixOperation;
import net.sf.sparql.benchmarking.util.FileUtils;

/**
 * Loader for {@link MixOperation}
 * 
 * @author rvesse
 * 
 */
public class MixOperationLoader extends AbstractOperationLoader {

    @Override
    public Operation load(File baseDir, String[] args) throws IOException {
        if (args.length < 1)
            throw new IOException("Insufficient arguments to load a mix operation");
        String mixFile = args[0];
        String name = "Child Operation Mix";
        if (args.length > 1) {
            name = args[1];
        }
        boolean randomOrder = false;
        if (args.length > 2) {
            randomOrder = Boolean.parseBoolean(args[2]);
        }

        File f = this.resolveFile(baseDir, mixFile);
        OperationMixLoader mixLoader = OperationMixLoaderRegistry.getLoader(FileUtils.getExtension(f, true, false));
        if (mixLoader == null)
            throw new IOException("No mix loader is associated with files with the extension "
                    + FileUtils.getExtension(f, true, true));
        OperationMix mix = mixLoader.load(f);
        
        return new MixOperation(name, mix, randomOrder);
    }

    @Override
    public String getPreferredName() {
        return "mix";
    }

    @Override
    public String getDescription() {
        return "The mix operation runs another operation mix a single operation and so can be used to group together sets of operations that should run together in a predictable order.";
    }

    @Override
    public OperationLoaderArgument[] getArguments() {
        OperationLoaderArgument[] args = new OperationLoaderArgument[3];
        args[0] = new OperationLoaderArgument("Mix File",
                "Provides a file that contains another operation mix that will be run as a single operation.",
                OperationLoaderArgument.TYPE_FILE);
        args[1] = AbstractOperationLoader.getNameArgument(true);
        args[2] = new OperationLoaderArgument(
                "Randomize Order",
                "Sets whether the order of operations within the mix will be randomized.\n"
                        + "Typically you will not set this since the default behaviour is to run the given operation mixes operations precisely in the order they are given since the main usage of this operation is to group together operations that should run predictably together.",
                OperationLoaderArgument.TYPE_BOOLEAN, true);
        return args;
    }
}
