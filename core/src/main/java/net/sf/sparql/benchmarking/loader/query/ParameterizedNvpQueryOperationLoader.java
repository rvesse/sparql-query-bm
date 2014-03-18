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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

import net.sf.sparql.benchmarking.loader.AbstractNvpOperationLoader;
import net.sf.sparql.benchmarking.loader.AbstractOperationLoader;
import net.sf.sparql.benchmarking.loader.OperationLoaderArgument;
import net.sf.sparql.benchmarking.operations.Operation;
import net.sf.sparql.benchmarking.operations.parameterized.nvp.ParameterizedNvpQueryOperation;

/**
 * Parameterized NVP query operation loader
 * 
 * @author rvesse
 * 
 */
public class ParameterizedNvpQueryOperationLoader extends AbstractNvpOperationLoader {

    static final Logger logger = LoggerFactory.getLogger(ParameterizedNvpQueryOperationLoader.class);

    @Override
    public Operation load(File baseDir, String[] args) throws IOException {
        if (args.length < 3)
            throw new IOException("Insufficient arguments to load a parameterized NVP query operation");

        String queryFile = args[0];
        String name = queryFile;
        String paramsFile = args[1];
        String nvpsFile = args[2];

        if (args.length > 3) {
            name = args[3];
        }

        String query = readFile(baseDir, queryFile);
        ResultSet rs = ResultSetFactory.fromTSV(getInputStream(baseDir, paramsFile));
        List<Binding> params = new ArrayList<Binding>();
        while (rs.hasNext()) {
            params.add(rs.nextBinding());
        }
        Map<String, List<String>> nvps = this.parseNvps(baseDir, nvpsFile);
        return new ParameterizedNvpQueryOperation(query, params, name, nvps);
    }

    @Override
    public String getPreferredName() {
        return "param-nvp-query";
    }

    @Override
    public String getDescription() {
        return "The param-nvp-query operation makes a parameterized SPARQL query against a remote SPARQL service via HTTP where parameters are drawn at random from a set of possible parameters.  Additionally it adds custom name value parameters to the HTTP request which can be used to test custom behaviour provided by a SPARQL endpoint";
    }

    @Override
    public OperationLoaderArgument[] getArguments() {
        OperationLoaderArgument[] args = new OperationLoaderArgument[4];
        args[0] = new OperationLoaderArgument("Query File", "Provides a file that contains the SPARQL query to be run.",
                OperationLoaderArgument.TYPE_FILE);
        args[1] = new OperationLoaderArgument(
                "Parameters File",
                "Provides a file that contains the parameters to be used.  Parameters files are expected to be in SPARQL TSV results format where each result row represents a set of parameters.",
                OperationLoaderArgument.TYPE_FILE);
        args[2] = AbstractNvpOperationLoader.getNvpsArgument();
        args[3] = AbstractOperationLoader.getNameArgument(true);
        return args;
    }
}
