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

package net.sf.sparql.benchmarking.options;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.apache.jena.atlas.web.auth.HttpAuthenticator;
import org.apache.jena.riot.WebContent;

import net.sf.sparql.benchmarking.monitoring.ProgressListener;
import net.sf.sparql.benchmarking.operations.OperationMix;

/**
 * Interface for generic runner options
 * 
 * @author rvesse
 * 
 */
public interface Options {

    /**
     * Default Halting Behaviour
     */
    public static final HaltBehaviour DEFAULT_HALT_BEHAVIOUR = HaltBehaviour.THROW_EXCEPTION;
    /**
     * Default Timeout in Seconds
     */
    public static final int DEFAULT_TIMEOUT = 300;
    /**
     * Default Result Format for {@code SELECT} queries
     */
    public static final String DEFAULT_FORMAT_SELECT = WebContent.contentTypeResultsXML;
    /**
     * Default Result Format for {@code ASK} queries
     */
    public static final String DEFAULT_FORMAT_ASK = WebContent.contentTypeResultsXML;
    /**
     * Default Result Format for {@code CONSTRUCT} and {@code DESCRIBE} queries
     * and operations that retrieve RDF graphs
     */
    public static final String DEFAULT_FORMAT_GRAPH = WebContent.contentTypeRDFXML;
    /**
     * Default Max Delay between operations in milliseconds
     */
    public static final int DEFAULT_MAX_DELAY = 1000;
    /**
     * Default Parallel Threads to use
     */
    public static final int DEFAULT_PARALLEL_THREADS = 1;
    /**
     * Default Sanity Checks
     */
    public static final int DEFAULT_SANITY_CHECKS = 2;

    /**
     * Gets the in-use executor for running queries and query mixes in threads
     * using the Java concurrent framework
     * 
     * @return The Executor Service used to execute tasks
     */
    public abstract ExecutorService getExecutor();

    /**
     * Gets the Halt on Timeout behavior
     * 
     * @return Whether a timeout causes benchmarking to abort
     */
    public abstract boolean getHaltOnTimeout();

    /**
     * Gets the Halt on Error behavior
     * 
     * @return Whether an error causes benchmarking to abort
     */
    public abstract boolean getHaltOnError();

    /**
     * Gets Halt on Any behavior
     * 
     * @return Whether any issue causes benchmarking to abort
     */
    public abstract boolean getHaltAny();

    /**
     * Gets the Halting Behaviour
     * 
     * @return Halting Behaviour
     */
    public abstract HaltBehaviour getHaltBehaviour();

    /**
     * Gets the Progress Listeners registered
     * 
     * @return Progress Listeners
     */
    public abstract List<ProgressListener> getListeners();

    /**
     * Adds a Progress Listener if it is not already registered
     * 
     * @param listener
     *            Progress Listener
     */
    public abstract void addListener(ProgressListener listener);

    /**
     * Removes a Progress Listener if it is registered
     * 
     * @param listener
     *            Progress Listener
     */
    public abstract void removeListener(ProgressListener listener);

    /**
     * Gets an unmodifiable copy of the defined custom endpoints
     * 
     * @return Map of custom endpoints
     */
    public abstract Map<String, String> getCustomEndpoints();

    /**
     * Gets a custom defined endpoint
     * 
     * @param name
     *            Endpoint name
     * @return Endpoint URI
     */
    public abstract String getCustomEndpoint(String name);

    /**
     * Resets the global run order
     * <p>
     * Useful for runners that incorporate warmups into their runs
     * </p>
     */
    public abstract void resetGlobalOrder();

    /**
     * Gets the Global Run Order
     * <p>
     * Called elsewhere so that mix runs and operation runs record what order
     * they were run in
     * </p>
     * 
     * @return Global Run Order
     */
    public abstract long getGlobalOrder();

    /**
     * Gets the number of parallel threads used for testing
     * 
     * @return Number of parallel threads
     */
    public abstract int getParallelThreads();

    /**
     * Sets the number of parallel threads used for testing
     * 
     * @param threads
     *            Number of Parallel Threads
     */
    public abstract void setParallelThreads(int threads);

    /**
     * Gets the maximum delay between operations
     * 
     * @return Maximum Delay in milliseconds
     */
    public abstract int getMaxDelay();

    /**
     * Sets the maximum delay between operations
     * 
     * @param milliseconds
     *            Maximum Delay in milliseconds
     */
    public abstract void setMaxDelay(int milliseconds);

    /**
     * Gets the HTTP authenticator in use
     * 
     * @return HTTP authenticator
     */
    public abstract HttpAuthenticator getAuthenticator();

    /**
     * Sets the HTTP authenticator used
     * 
     * @param authenticator
     *            HTTP authenticator
     */
    public abstract void setAuthenticator(HttpAuthenticator authenticator);

    /**
     * Gets whether the client will allow the server to return Deflate/GZip
     * compressed responses
     * 
     * @return Whether Deflate/GZip compression is allowed
     */
    public abstract boolean getAllowCompression();

    /**
     * Sets whether the client will allow the server to return Deflate/GZip
     * compressed responses
     * 
     * @param allowed
     *            Whether Deflate/GZip compression is allowed
     */
    public abstract void setAllowCompression(boolean allowed);

    /**
     * Gets the Results format used for operations that make CONSTRUCT/DESCRIBE
     * queries or that retrieve RDF graphs
     * 
     * @return MIME Type for CONSTRUCT/DESCRUBE results and RDF graphs
     */
    public abstract String getResultsGraphFormat();

    /**
     * Sets the Results format used for operations that CONSTRUCT/DESCRIBE
     * queries or that retrieve RDF graphs
     * 
     * @param contentType
     *            MIME Type for CONSTRUCT/DESCRIBE results and RDF graphs
     */
    public abstract void setResultsGraphFormat(String contentType);

    /**
     * Gets the Results format used for operations that make SELECT queries
     * 
     * @return MIME Type for SELECT results
     */
    public abstract String getResultsSelectFormat();

    /**
     * Sets the Results format to be used for operations that make SELECT
     * queries
     * 
     * @param contentType
     *            MIME Type for SELECT results
     */
    public abstract void setResultsSelectFormat(String contentType);

    /**
     * Gets the Results format used for operations that make ASK queries
     * 
     * @return MIME Type for ASK results
     */
    public abstract String getResultsAskFormat();

    /**
     * Sets the Results format to be used for operations that make ASK queries
     * 
     * @param contentType
     *            MIME Type for ASK results
     */
    public abstract void setResultsAskFormat(String contentType);

    /**
     * Gets the timeout for operations, a zero/negative value indicates no
     * timeout
     * 
     * @return Timeout in seconds
     */
    public abstract int getTimeout();

    /**
     * Sets the timeout for operations, a zero/negative value indicates no
     * timeout
     * 
     * @param timeout
     *            Timeout in seconds
     */
    public abstract void setTimeout(int timeout);

    /**
     * Sets a custom defined endpoint
     * 
     * @param name
     *            Name
     * @param endpoint
     *            Endpoint URI
     */
    public abstract void setCustomEndpoint(String name, String endpoint);

    /**
     * Gets the SPARQL graph store protocol endpoint that is in use
     * 
     * @return SPARQL graph store endpoint URI
     */
    public abstract String getGraphStoreEndpoint();

    /**
     * Gets the SPARQL graph store protocol endpoint that is in use
     * 
     * @param endpoint
     */
    public abstract void setGraphStoreEndpoint(String endpoint);

    /**
     * Gets the SPARQL Update endpoint that is in use
     * 
     * @return SPARQL update endpoint URI
     */
    public abstract String getUpdateEndpoint();

    /**
     * Sets the SPARQL update endpoint that is in use
     * 
     * @param endpoint
     *            SPARQL update endpoint URI
     */
    public abstract void setUpdateEndpoint(String endpoint);

    /**
     * Gets the SPARQL query endpoint that is in use
     * 
     * @return SPARQL query endpoint URI
     */
    public abstract String getQueryEndpoint();

    /**
     * Sets the SPARQL query endpoint to be used
     * 
     * @param endpoint
     *            SPARQL query endpoint URI
     */
    public abstract void setQueryEndpoint(String endpoint);

    /**
     * Sets the Query Mix to use
     * 
     * @param queries
     *            Query Mix
     */
    public abstract void setOperationMix(OperationMix queries);

    /**
     * Gets the Query Mix that is used
     * 
     * @return Query Mix
     */
    public abstract OperationMix getOperationMix();

    /**
     * Gets whether operation order should be randomized
     * 
     * @return Whether operation order is random
     */
    public abstract boolean getRandomizeOrder();

    /**
     * Sets whether operation order should be randomized
     * 
     * @param randomize
     *            Whether operation order should be random
     */
    public abstract void setRandomizeOrder(boolean randomize);

    /**
     * Sets the Halting Behaviour
     * 
     * @param behaviour
     *            Halting Behaviour
     */
    public abstract void setHaltBehaviour(HaltBehaviour behaviour);

    /**
     * Sets Halt on Any behavior, if set to true sets Halt on Error and Halt on
     * Timeout to true as well
     * 
     * @param halt
     *            Whether any issue should cause benchmarking to abort
     */
    public abstract void setHaltAny(boolean halt);

    /**
     * Sets the Halt on Error behavior
     * 
     * @param halt
     *            Whether an error should cause benchmarking to abort
     */
    public abstract void setHaltOnError(boolean halt);

    /**
     * Sets the Halt on Timeout behavior
     * 
     * @param halt
     *            Whether a timeout should cause benchmarking to abort
     */
    public abstract void setHaltOnTimeout(boolean halt);

    /**
     * Gets the Sanity Checking Level
     * 
     * @return Sanity Check Level
     */
    public abstract int getSanityCheckLevel();

    /**
     * Sets the Sanity Checking level
     * 
     * @param level
     *            Sanity Check Level
     */
    public abstract void setSanityCheckLevel(int level);

    /**
     * Gets the tear down mix to be run, {@code null} indicates no tear down mix
     * is requested.
     * <p>
     * Operations in a tear down mix are guaranteed to be run exactly in the
     * order given.
     * </p>
     * 
     * @return Tear down mix or null
     */
    public abstract OperationMix getTeardownMix();

    /**
     * Sets the tear down mix that will be run once after testing completes
     * successfully.
     * <p>
     * Operations in a tear down mix are guaranteed to be run exactly in the
     * order given.
     * </p>
     * 
     * @param mix
     *            Tear down mix
     */
    public abstract void setTeardownMix(OperationMix mix);

    /**
     * Gets the setup mix to be run, {@code null} indicates no setup mix is
     * requested.
     * <p>
     * Operations in a setup mix are guaranteed to be run exactly in the order
     * given. </>
     * 
     * @return Setup mix or null
     */
    public abstract OperationMix getSetupMix();

    /**
     * Sets the setup mix that will be run once before testing starts.
     * <p>
     * Operations in a setup mix are guaranteed to be run exactly in the order
     * given. </>
     * 
     * @param mix
     *            Setup mix
     */
    public abstract void setSetupMix(OperationMix mix);

}