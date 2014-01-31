/*
 * Copyright 2014 YarcData LLC All Rights Reserved.
 */

package net.sf.sparql.query.benchmarking.loader;

import java.io.File;
import java.io.IOException;

import net.sf.sparql.query.benchmarking.operations.BenchmarkOperation;

/**
 * Interface for operation loaders
 * 
 * @author rvesse
 * 
 */
public interface OperationLoader {

    /**
     * Load an operation described by the given arguments
     * 
     * @param baseDir
     *            Base directory
     * @param args
     *            Arguments
     * @return Operation
     * @throws IOException
     */
    public BenchmarkOperation load(File baseDir, String[] args) throws IOException;

    /**
     * Gets the name that this loader prefers to be referenced by
     * <p>
     * This is the preferred name used in certain circumstances such as when
     * registering using
     * {@link OperationLoaderRegistry#addLoader(OperationLoader)} but users may
     * register a loader with any name they want.
     * </p>
     * 
     * @return Preferred name
     */
    public String getPreferredName();
}
