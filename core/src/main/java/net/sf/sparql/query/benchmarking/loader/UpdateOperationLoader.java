/*
 * Copyright 2014 YarcData LLC All Rights Reserved.
 */

package net.sf.sparql.query.benchmarking.loader;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.sparql.query.benchmarking.operations.BenchmarkOperation;
import net.sf.sparql.query.benchmarking.updates.BenchmarkUpdate;

/**
 * Query operation loader
 * 
 * @author rvesse
 * 
 */
public class UpdateOperationLoader extends AbstractOperationLoader {

    static final Logger logger = LoggerFactory.getLogger(UpdateOperationLoader.class);

    @Override
    public BenchmarkOperation load(File baseDir, String[] args) throws IOException {
        String queryFile = args[0];
        String name = queryFile;
        if (args.length > 1) {
            name = args[1];
        }

        String update = readFile(baseDir, queryFile);
        return new BenchmarkUpdate(name, update);
    }

    @Override
    public String getPreferredName() {
        return "update";
    }
}
