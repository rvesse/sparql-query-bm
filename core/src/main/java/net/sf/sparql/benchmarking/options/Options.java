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

import org.apache.jena.query.Dataset;

import net.sf.sparql.benchmarking.monitoring.ProgressListener;
import net.sf.sparql.benchmarking.operations.OperationMix;
import net.sf.sparql.benchmarking.runners.mix.DefaultOperationMixRunner;
import net.sf.sparql.benchmarking.runners.mix.OperationMixRunner;
import net.sf.sparql.benchmarking.runners.operations.DefaultOperationRunner;
import net.sf.sparql.benchmarking.runners.operations.OperationRunner;

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
     * Default Limit, values <= 0 are considered to mean leave existing LIMIT
     * as-is and don't impose a limit on unlimited queries
     */
    public static final long DEFAULT_LIMIT = 0;

    /**
     * Adds a Progress Listener if it is not already registered
     * 
     * @param listener
     *            Progress Listener
     */
    public abstract void addListener(ProgressListener listener);

    /**
     * Makes a copy of the options
     * <p>
     * While this is guaranteed to take a copy of primitive typed properties
     * there is no guarantee that it takes a copy of reference types so changing
     * some properties will still affect the original options. This method is
     * primarily intended for use in cases where you need to tweak an option
     * without interfering with other consumers of the options which is
     * particularly relevant when running multi-threaded testing.
     * </p>
     * 
     * @return Copied options
     */
    public abstract <T extends Options> T copy();

    /**
     * Gets whether the client will allow the server to return Deflate/GZip
     * compressed responses
     * 
     * @return Whether Deflate/GZip compression is allowed
     */
    public abstract boolean getAllowCompression();

    /**
     * Gets the HTTP authenticator in use
     * 
     * @return HTTP authenticator
     */
    public abstract HttpAuthenticator getAuthenticator();

    /**
     * Gets a custom endpoint
     * 
     * @param name
     *            Endpoint name
     * @return Endpoint URI
     */
    public abstract String getCustomEndpoint(String name);

    /**
     * Gets an unmodifiable copy of the defined custom endpoints
     * <p>
     * Custom endpoints are a more specific form of the
     * {@link #getCustomSettings()} and provide a slightly more user friendly
     * and type safe interface when the custom setting to be defined has a
     * string value.
     * </p>
     * 
     * @return Map of custom endpoints
     */
    public abstract Map<String, String> getCustomEndpoints();

    /**
     * Gets a map that may be used to get/set custom settings
     * <p>
     * This is provided so custom operations may be created that can share state
     * or that need custom settings to be provided and can't modify/extend the
     * standard {@code Options} interface since they want to run with existing
     * standard runners.
     * </p>
     * 
     * @return Map of custom settings
     */
    public abstract Map<String, Object> getCustomSettings();

    /**
     * Gets a dataset that is used for in-memory queries and updates
     * 
     * @return Dataset
     */
    public abstract Dataset getDataset();

    /**
     * Gets the in-use executor for running queries and query mixes in threads
     * using the Java concurrent framework
     * 
     * @return The Executor Service used to execute tasks
     */
    public abstract ExecutorService getExecutor();

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
     * Gets the SPARQL graph store protocol endpoint that is in use
     * 
     * @return SPARQL graph store endpoint URI
     */
    public abstract String getGraphStoreEndpoint();

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
     * Gets the Halt on Error behavior
     * 
     * @return Whether an error causes benchmarking to abort
     */
    public abstract boolean getHaltOnError();

    /**
     * Gets the Halt on Timeout behavior
     * 
     * @return Whether a timeout causes benchmarking to abort
     */
    public abstract boolean getHaltOnTimeout();

    /**
     * Gets the LIMIT to impose on queries
     * 
     * @return Limit to impose
     */
    public abstract long getLimit();

    /**
     * Gets the Progress Listeners registered
     * 
     * @return Progress Listeners
     */
    public abstract List<ProgressListener> getListeners();

    /**
     * Gets whether the API should ensure that relative URIs are presented as
     * absolute URIs to services the harness interacts with
     * 
     * @return True if absolute URIs will be ensured, false otherwise
     */
    public abstract boolean getEnsureAbsoluteURIs();

    /**
     * Gets the maximum delay between operations
     * 
     * @return Maximum Delay in milliseconds
     */
    public abstract int getMaxDelay();

    /**
     * Gets the operation mix runner to use, if {@code null} is returned then
     * the default {@link DefaultOperationMixRunner} should be used
     * 
     * @return Operation mix runner
     */
    public abstract OperationMixRunner getMixRunner();

    /**
     * Gets whether query results are counted or just thrown away
     * 
     * @return True if results will not be counted
     */
    public abstract boolean getNoCount();

    /**
     * Gets the Query Mix that is used
     * 
     * @return Query Mix
     */
    public abstract OperationMix getOperationMix();

    /**
     * Gets the operation runner to use, if {@code null} is returned then the
     * default {@link OperationRunner} should be used
     * 
     * @return Operation runner
     */
    public abstract OperationRunner getOperationRunner();

    /**
     * Gets the number of parallel threads used for testing
     * 
     * @return Number of parallel threads
     */
    public abstract int getParallelThreads();

    /**
     * Gets the SPARQL query endpoint that is in use
     * 
     * @return SPARQL query endpoint URI
     */
    public abstract String getQueryEndpoint();

    /**
     * Gets whether operation order should be randomized
     * 
     * @return Whether operation order is random
     */
    public abstract boolean getRandomizeOrder();

    /**
     * Gets the Results format used for operations that make ASK queries
     * 
     * @return MIME Type for ASK results
     */
    public abstract String getResultsAskFormat();

    /**
     * Gets the Results format used for operations that make CONSTRUCT/DESCRIBE
     * queries or that retrieve RDF graphs
     * 
     * @return MIME Type for CONSTRUCT/DESCRUBE results and RDF graphs
     */
    public abstract String getResultsGraphFormat();

    /**
     * Gets the Results format used for operations that make SELECT queries
     * 
     * @return MIME Type for SELECT results
     */
    public abstract String getResultsSelectFormat();

    /**
     * Gets the Sanity Checking Level
     * 
     * @return Sanity Check Level
     */
    public abstract int getSanityCheckLevel();

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
     * Gets the timeout for operations, a zero/negative value indicates no
     * timeout
     * 
     * @return Timeout in seconds
     */
    public abstract int getTimeout();

    /**
     * Gets the SPARQL Update endpoint that is in use
     * 
     * @return SPARQL update endpoint URI
     */
    public abstract String getUpdateEndpoint();

    /**
     * Removes a Progress Listener if it is registered
     * 
     * @param listener
     *            Progress Listener
     */
    public abstract void removeListener(ProgressListener listener);

    /**
     * Resets the global run order
     * <p>
     * Useful for runners that incorporate warmups into their runs
     * </p>
     */
    public abstract void resetGlobalOrder();

    /**
     * Sets whether the client will allow the server to return Deflate/GZip
     * compressed responses
     * 
     * @param allowed
     *            Whether Deflate/GZip compression is allowed
     */
    public abstract void setAllowCompression(boolean allowed);

    /**
     * Sets the HTTP authenticator used
     * 
     * @param authenticator
     *            HTTP authenticator
     */
    public abstract void setAuthenticator(HttpAuthenticator authenticator);

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
     * Sets a dataset to be used for in-memory queries and updates
     * 
     * @param dataset
     *            Dataset
     */
    public abstract void setDataset(Dataset dataset);

    /**
     * Sets whether the API should ensure that relative URIs are presented as
     * absolute URIs to services the harness interacts with
     * 
     * @return True if absolute URIs should be ensured, false otherwise
     */
    public abstract void setEnsureAbsoluteURIs(boolean ensureAbsolute);

    /**
     * Gets the SPARQL graph store protocol endpoint that is in use
     * 
     * @param endpoint
     */
    public abstract void setGraphStoreEndpoint(String endpoint);

    /**
     * Sets Halt on Any behavior, if set to true sets Halt on Error and Halt on
     * Timeout to true as well
     * 
     * @param halt
     *            Whether any issue should cause benchmarking to abort
     */
    public abstract void setHaltAny(boolean halt);

    /**
     * Sets the Halting Behaviour
     * 
     * @param behaviour
     *            Halting Behaviour
     */
    public abstract void setHaltBehaviour(HaltBehaviour behaviour);

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
     * Sets the LIMIT to impose on queries
     * <p>
     * Values less than or equal to zero mean existing limits are left
     * unchanged, non-zero values will be imposed iff existing limit is greater
     * than the set limit
     * </p>
     * 
     * @param limit
     *            Limit to impose
     */
    public abstract void setLimit(long limit);

    /**
     * Sets the maximum delay between operations
     * 
     * @param milliseconds
     *            Maximum Delay in milliseconds
     */
    public abstract void setMaxDelay(int milliseconds);

    /**
     * Sets the operation mix runner to use, if set to {@code null} then the
     * default {@link DefaultOperationMixRunner} should be used
     * 
     * @param runner
     *            Operation mix runner
     */
    public abstract void setMixRunner(OperationMixRunner runner);

    /**
     * Sets whether query results are counted or just thrown away
     * <p>
     * Currently enabling this only applies to SELECT queries as only SELECT
     * queries stream the results currently, future versions of this tool will
     * also stream CONSTRUCT/DESCRIBE results but this is yet to be implemented
     * </p>
     * 
     * @param noCount
     *            Whether query results are counted
     */
    public abstract void setNoCount(boolean noCount);

    /**
     * Sets the Query Mix to use
     * 
     * @param queries
     *            Query Mix
     */
    public abstract void setOperationMix(OperationMix queries);

    /**
     * Sets the operation runner to use, if set to {@code null} then the default
     * {@link DefaultOperationRunner} should be used
     * 
     * @param runner
     *            Operation runner
     */
    public abstract void setOperationRunner(OperationRunner runner);

    /**
     * Sets the number of parallel threads used for testing
     * 
     * @param threads
     *            Number of Parallel Threads
     */
    public abstract void setParallelThreads(int threads);

    /**
     * Sets the SPARQL query endpoint to be used
     * 
     * @param endpoint
     *            SPARQL query endpoint URI
     */
    public abstract void setQueryEndpoint(String endpoint);

    /**
     * Sets whether operation order should be randomized
     * 
     * @param randomize
     *            Whether operation order should be random
     */
    public abstract void setRandomizeOrder(boolean randomize);

    /**
     * Sets the Results format to be used for operations that make ASK queries
     * 
     * @param contentType
     *            MIME Type for ASK results
     */
    public abstract void setResultsAskFormat(String contentType);

    /**
     * Sets the Results format used for operations that CONSTRUCT/DESCRIBE
     * queries or that retrieve RDF graphs
     * 
     * @param contentType
     *            MIME Type for CONSTRUCT/DESCRIBE results and RDF graphs
     */
    public abstract void setResultsGraphFormat(String contentType);

    /**
     * Sets the Results format to be used for operations that make SELECT
     * queries
     * 
     * @param contentType
     *            MIME Type for SELECT results
     */
    public abstract void setResultsSelectFormat(String contentType);

    /**
     * Sets the Sanity Checking level
     * 
     * @param level
     *            Sanity Check Level
     */
    public abstract void setSanityCheckLevel(int level);

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
     * Sets the timeout for operations, a zero/negative value indicates no
     * timeout
     * 
     * @param timeout
     *            Timeout in seconds
     */
    public abstract void setTimeout(int timeout);

    /**
     * Sets the SPARQL update endpoint that is in use
     * 
     * @param endpoint
     *            SPARQL update endpoint URI
     */
    public abstract void setUpdateEndpoint(String endpoint);
}