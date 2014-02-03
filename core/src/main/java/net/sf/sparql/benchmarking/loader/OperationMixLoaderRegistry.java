/*
 * Copyright 2014 YarcData LLC All Rights Reserved.
 */

package net.sf.sparql.benchmarking.loader;

import java.util.Hashtable;
import java.util.Map;

import net.sf.sparql.benchmarking.loader.impl.ClassicQueryMixLoader;

/**
 * Provides a registry of operation mix loader
 * 
 * @author rvesse
 * 
 */
public class OperationMixLoaderRegistry {

    private static Map<String, OperationMixLoader> loaders = new Hashtable<String, OperationMixLoader>();

    /**
     * Private constructor to prevent instantiation
     */
    private OperationMixLoaderRegistry() {
    }

    static {
        init();
    }

    private synchronized static void init() {
        loaders.put(".txt", new ClassicQueryMixLoader());
    }

    /**
     * Gets the loader with the specified extension (if known)
     * 
     * @param ext
     *            Extension
     * @return Loader if available, null otherwise
     */
    public static OperationMixLoader getLoader(String ext) {
        return loaders.get(ext);
    }

    /**
     * Adds a loader using its preferred extension
     * 
     * @param loader
     *            Loader
     */
    public static void addLoader(OperationMixLoader loader) {
        addLoader(loader.getPreferredExtension(), loader);
    }

    /**
     * Adds a loader
     * 
     * @param ext
     *            Extension
     * @param loader
     *            Loader
     */
    public static void addLoader(String ext, OperationMixLoader loader) {
        loaders.put(ext, loader);
    }

    /**
     * Removes the loader for the given extension
     * 
     * @param ext
     *            Extension
     */
    public static void removeLoader(String ext) {
        loaders.remove(ext);
    }

    /**
     * Resets to the default loader setup
     */
    public static synchronized void resetLoaders() {
        loaders.clear();
        init();
    }
}
