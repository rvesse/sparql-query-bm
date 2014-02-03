/*
 * Copyright 2014 YarcData LLC All Rights Reserved.
 */

package net.sf.sparql.benchmarking.loader;

import java.io.File;
import java.io.IOException;

import net.sf.sparql.benchmarking.operations.OperationMix;

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
     * @throws IOException
     */
    public abstract OperationMix load(File file) throws IOException;

    /**
     * Gets the file extension without the leading {@code .} that this loader
     * prefers to use for its mix files
     * <p>
     * This is the preferred extension used in certain circumstances such as
     * when registering using
     * {@link OperationMixLoaderRegistry#addLoader(OperationMixLoader)} but
     * users may register a mix loader with any extension they want.
     * </p>
     * 
     * @return Preferred extension
     */
    public abstract String getPreferredExtension();
}
