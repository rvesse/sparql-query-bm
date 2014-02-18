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

package net.sf.sparql.benchmarking.loader;

import java.util.Hashtable;
import java.util.Map;

import net.sf.sparql.benchmarking.loader.gsp.GSPDeleteOperationLoader;
import net.sf.sparql.benchmarking.loader.gsp.GSPGetOperationLoader;
import net.sf.sparql.benchmarking.loader.gsp.GSPHeadOperationLoader;
import net.sf.sparql.benchmarking.loader.query.DatasetSizeOperationLoader;
import net.sf.sparql.benchmarking.loader.query.FixedNvpQueryOperationLoader;
import net.sf.sparql.benchmarking.loader.query.ParameterizedNvpQueryOperationLoader;
import net.sf.sparql.benchmarking.loader.query.ParameterizedQueryOperationLoader;
import net.sf.sparql.benchmarking.loader.query.FixedQueryOperationLoader;
import net.sf.sparql.benchmarking.loader.update.FixedNvpUpdateOperationLoader;
import net.sf.sparql.benchmarking.loader.update.ParameterizedNvpUpdateOperationLoader;
import net.sf.sparql.benchmarking.loader.update.ParameterizedUpdateOperationLoader;
import net.sf.sparql.benchmarking.loader.update.FixedUpdateOperationLoader;
import net.sf.sparql.benchmarking.loader.util.SleepOperationLoader;

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
        // Query operations
        addLoader(new FixedQueryOperationLoader());
        addLoader(new FixedNvpQueryOperationLoader());
        addLoader(new ParameterizedQueryOperationLoader());
        addLoader(new ParameterizedNvpQueryOperationLoader());
        addLoader(new DatasetSizeOperationLoader());
        
        // Update operations
        addLoader(new FixedUpdateOperationLoader());
        addLoader(new FixedNvpUpdateOperationLoader());
        addLoader(new ParameterizedUpdateOperationLoader());
        addLoader(new ParameterizedNvpUpdateOperationLoader());
        
        // GSP operations
        addLoader(new GSPGetOperationLoader());
        addLoader(new GSPHeadOperationLoader());
        addLoader(new GSPDeleteOperationLoader());
        
        // Other operations
        addLoader(new SleepOperationLoader());
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
