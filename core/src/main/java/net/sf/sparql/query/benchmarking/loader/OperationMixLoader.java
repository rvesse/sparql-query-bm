/*
 * Copyright 2014 YarcData LLC All Rights Reserved.
 */

package net.sf.sparql.query.benchmarking.loader;

import java.io.File;
import java.io.IOException;

import net.sf.sparql.query.benchmarking.operations.BenchmarkOperationMix;

/**
 * Interface for operation mix loaders
 * 
 * @author rvesse
 * 
 */
public interface OperationMixLoader {

    /**
     * Loads an operation mix from the given file
     * 
     * @param file
     *            File
     * @return Operation mix
     */
    public abstract BenchmarkOperationMix load(File file) throws IOException;
}
