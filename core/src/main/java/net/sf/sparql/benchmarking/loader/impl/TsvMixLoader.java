/*
 * Copyright 2014 YarcData LLC All Rights Reserved.
 */

package net.sf.sparql.benchmarking.loader.impl;

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
    protected Operation parseLine(File baseDir, String line) throws IOException {
        String[] fields = line.split("\t");
        OperationLoader loader = OperationLoaderRegistry.getLoader(fields[0]);
        if (loader == null)
            throw new IOException("No OperationLoader is registered for the operation type " + fields[0]);
        String[] args = Arrays.copyOfRange(fields, 1, fields.length);
        return loader.load(baseDir, args);
    }

}
