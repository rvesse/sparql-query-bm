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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

import net.sf.sparql.benchmarking.loader.AbstractOperationLoader;
import net.sf.sparql.benchmarking.operations.Operation;
import net.sf.sparql.benchmarking.operations.parameterized.ParameterizedQueryOperation;

/**
 * Parameterized query operation loader
 * 
 * @author rvesse
 * 
 */
public class ParameterizedQueryOperationLoader extends AbstractOperationLoader {

    static final Logger logger = LoggerFactory.getLogger(ParameterizedQueryOperationLoader.class);

    @Override
    public Operation load(File baseDir, String[] args) throws IOException {
        if (args.length < 2)
            throw new IOException("Insufficient arguments to load a parameterized query operation");

        String queryFile = args[0];
        String name = queryFile;
        String paramsFile = args[1];

        if (args.length > 2) {
            name = args[2];
        }

        String query = readFile(baseDir, queryFile);
        ResultSet rs = ResultSetFactory.fromTSV(getInputStream(baseDir, paramsFile));
        List<Binding> params = new ArrayList<Binding>();
        while (rs.hasNext()) {
            params.add(rs.nextBinding());
        }
        return new ParameterizedQueryOperation(query, params, name);
    }

    @Override
    public String getPreferredName() {
        return "param-query";
    }
}
