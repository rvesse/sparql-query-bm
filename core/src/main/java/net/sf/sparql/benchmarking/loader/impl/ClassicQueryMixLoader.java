/*
 * Copyright 2014 YarcData LLC All Rights Reserved.
 */

package net.sf.sparql.benchmarking.loader.impl;

import java.io.File;
import java.io.IOException;

import net.sf.sparql.benchmarking.loader.AbstractLineBasedMixLoader;
import net.sf.sparql.benchmarking.operations.Operation;

/**
 * A loader for the text based query mix format from the 1.x releases of the API
 * 
 * @author rvesse
 * 
 */
public class ClassicQueryMixLoader extends AbstractLineBasedMixLoader {

    private static final QueryOperationLoader loader = new QueryOperationLoader();

    @Override
    protected Operation parseLine(File baseDir, String line) throws IOException {
        return loader.load(baseDir, new String[] { line });
    }

    @Override
    public String getPreferredExtension() {
        return "txt";
    }
}
