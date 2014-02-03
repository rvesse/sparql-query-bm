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
