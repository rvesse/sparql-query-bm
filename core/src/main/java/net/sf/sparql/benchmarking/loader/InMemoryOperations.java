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

import net.sf.sparql.benchmarking.loader.query.FixedQueryOperationLoader;
import net.sf.sparql.benchmarking.loader.query.InMemoryFixedQueryOperationLoader;
import net.sf.sparql.benchmarking.loader.query.InMemoryParameterizedQueryOperationLoader;
import net.sf.sparql.benchmarking.loader.query.InMemorySummarizedFixedQueryOperationLoader;
import net.sf.sparql.benchmarking.loader.query.ParameterizedQueryOperationLoader;
import net.sf.sparql.benchmarking.loader.update.FixedUpdateOperationLoader;
import net.sf.sparql.benchmarking.loader.update.InMemoryFixedUpdateOperationLoader;
import net.sf.sparql.benchmarking.loader.update.InMemoryParameterizedUpdateOperationLoader;
import net.sf.sparql.benchmarking.loader.update.ParameterizedUpdateOperationLoader;

/**
 * Static helper class for registering the in-memory operations in place of the
 * standard remote operations
 * 
 * @author rvesse
 * 
 */
public class InMemoryOperations {

    /**
     * Private constructor prevents direct instantiation
     */
    private InMemoryOperations() {

    }

    /**
     * Registers the in-memory versions of various operations in place of the
     * normal remote operations with the {@link OperationLoaderRegistry}. This
     * allows for mixes written for remote systems to also be run against local
     * systems without having to edit the mix.
     * <p>
     * Note that even without having called this method you can still use the
     * in-memory operations directly in your mix by adding the {@code mem-}
     * prefix to the standard operation names e.g. {@code mem-query} instead of
     * {@code query}.
     * </p>
     * <p>
     * You can call {@link #restoreRemoteOperations()} to restore the normal
     * operation mappings. This is usually preferable to calling
     * {@link OperationLoaderRegistry#resetLoaders()} since it will only restore
     * the built-in remote operations and not remove any custom operations you
     * may have registered.
     * </p>
     */
    public static void useInMemoryOperations() {
        // Query Operations
        OperationLoaderRegistry.addLoader("query", new InMemoryFixedQueryOperationLoader());
        OperationLoaderRegistry.addLoader("param-query", new InMemoryParameterizedQueryOperationLoader());

        // Update Operations
        OperationLoaderRegistry.addLoader("update", new InMemoryFixedUpdateOperationLoader());
        OperationLoaderRegistry.addLoader("param-update", new InMemoryParameterizedUpdateOperationLoader());
    }

    public static void useInMemoryOperations(boolean summarize) {
        useInMemoryOperations();

        if (!summarize)
            return;

        // Switch Query Operations for Summarized Query Operations
        OperationLoaderRegistry.addLoader("query", new InMemorySummarizedFixedQueryOperationLoader());
        //OperationLoaderRegistry.addLoader("param-query", new InMemorySummarizedParameterizedQueryOperationLoader());
    }

    /**
     * Restores the registration of the remote operations that may have been
     * overridden by previous calls to {@link #useInMemoryOperations()}
     */
    public static void restoreRemoteOperations() {
        // Query Operations
        OperationLoaderRegistry.addLoader(new FixedQueryOperationLoader());
        OperationLoaderRegistry.addLoader(new ParameterizedQueryOperationLoader());

        // Update Operations
        OperationLoaderRegistry.addLoader(new FixedUpdateOperationLoader());
        OperationLoaderRegistry.addLoader(new ParameterizedUpdateOperationLoader());
    }
}
