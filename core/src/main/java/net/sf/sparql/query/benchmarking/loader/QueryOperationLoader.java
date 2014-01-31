/*
 * Copyright 2014 YarcData LLC All Rights Reserved.
 */

package net.sf.sparql.query.benchmarking.loader;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.sparql.query.benchmarking.operations.BenchmarkOperation;
import net.sf.sparql.query.benchmarking.queries.BenchmarkQuery;

/**
 * Query operation loader
 * 
 * @author rvesse
 * 
 */
public class QueryOperationLoader extends AbstractOperationLoader {

    static final Logger logger = LoggerFactory.getLogger(QueryOperationLoader.class);

    @Override
    public BenchmarkOperation load(File baseDir, String[] args) throws IOException {
        String queryFile = args[0];
        String name = queryFile;
        if (args.length > 1) {
            name = args[1];
        }

        String query = readFile(baseDir, queryFile);
        return new BenchmarkQuery(name, query);
    }

    @Override
    public String getPreferredName() {
        return "query";
    }
}
