/*
 * Copyright 2014 YarcData LLC All Rights Reserved.
 */

package net.sf.sparql.benchmarking.loader;

import java.util.Hashtable;
import java.util.Map;

import net.sf.sparql.benchmarking.loader.impl.QueryOperationLoader;
import net.sf.sparql.benchmarking.loader.impl.UpdateOperationLoader;

/**
 * Provides a registry of operation loaders
 * 
 * @author rvesse
 * 
 */
public class OperationLoaderRegistry {

    private static Map<String, OperationLoader> loaders = new Hashtable<String, OperationLoader>();

    /**
     * Private constructor to prevent instantiation
     */
    private OperationLoaderRegistry() {
    }

    static {
        init();
    }

    private synchronized static void init() {
        loaders.put("query", new QueryOperationLoader());
        loaders.put("update", new UpdateOperationLoader());
    }

    /**
     * Gets the loader with the specified name (if known)
     * 
     * @param name
     *            Name
     * @return Loader if available, null otherwise
     */
    public static OperationLoader getLoader(String name) {
        return loaders.get(name);
    }

    /**
     * Adds a loader using its preferred name
     * 
     * @param loader
     *            Loader
     */
    public static void addLoader(OperationLoader loader) {
        addLoader(loader.getPreferredName(), loader);
    }

    /**
     * Adds a loader
     * 
     * @param name
     *            Name
     * @param loader
     *            Loader
     */
    public static void addLoader(String name, OperationLoader loader) {
        loaders.put(name, loader);
    }

    /**
     * Removes the named loader
     * 
     * @param name
     *            Name
     */
    public static void removeLoader(String name) {
        loaders.remove(name);
    }

    /**
     * Resets to the default loader setup
     */
    public static synchronized void resetLoaders() {
        loaders.clear();
        init();
    }
}
