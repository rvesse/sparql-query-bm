/*
 * Copyright 2014 YarcData LLC All Rights Reserved.
 */

package net.sf.sparql.benchmarking.options;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.apache.jena.atlas.web.auth.HttpAuthenticator;
import org.apache.jena.riot.WebContent;

import net.sf.sparql.benchmarking.HaltBehaviour;
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
     * Default Runs
     */
    public static final int DEFAULT_RUNS = 25;
    /**
     * Default Timeout in Seconds
     */
    public static final int DEFAULT_TIMEOUT = 300;
    /**
     * Default Warmup Runs
     */
    public static final int DEFAULT_WARMUPS = 5;
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
     */
    public static final String DEFAULT_FORMAT_GRAPH = WebContent.contentTypeRDFXML;
    /**
     * Default Max Delay between Queries in milliseconds
     */
    public static final int DEFAULT_MAX_DELAY = 1000;
    /**
     * Default Parallel Threads for query run evaluation
     */
    public static final int DEFAULT_PARALLEL_THREADS = 1;

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
     * Called elsewhere so that mix runs and query runs record what order they
     * were run in
     * </p>
     * 
     * @return Global Run Order
     */
    public abstract long getGlobalOrder();

    /**
     * Gets the number of parallel threads used for query run evaluation
     * 
     * @return Number of parallel threads
     */
    public abstract int getParallelThreads();

    /**
     * Sets the number of parallel threads used for query run evaluation
     * 
     * @param threads
     *            Number of Parallel Threads
     */
    public abstract void setParallelThreads(int threads);

    /**
     * Gets the maximum delay between queries
     * 
     * @return Maximum Delay in milliseconds
     */
    public abstract int getMaxDelay();

    /**
     * Sets the maximum delay between queries
     * 
     * @param milliseconds
     *            Maximum Delay in milliseconds
     */
    public abstract void setMaxDelay(int milliseconds);

    /**
     * Gets the HTTP authenticator in use
     * 
     * @return HTTP authenticator
     * @since 1.1.0
     */
    public abstract HttpAuthenticator getAuthenticator();

    /**
     * Sets the HTTP authenticator used
     * 
     * @param authenticator
     *            HTTP authenticator
     * @since 1.1.0
     */
    public abstract void setAuthenticator(HttpAuthenticator authenticator);

    /**
     * Gets whether the client will allow the server to return Deflate encoded
     * responses
     * 
     * @return Whether Deflate encoding is allowed
     */
    public abstract boolean getAllowDeflateEncoding();

    /**
     * Sets whether the client will allow the server to return Deflate encoded
     * responses
     * 
     * @param allowed
     *            Whether Deflate encoding is allowed
     */
    public abstract void setAllowDeflateEncoding(boolean allowed);

    /**
     * Gets whether the client will allow the server to return GZip encoded
     * responses
     * 
     * @return Whether GZip encoding is allowed
     */
    public abstract boolean getAllowGZipEncoding();

    /**
     * Sets whether the client should allow the server to return GZip encoded
     * responses
     * 
     * @param allowed
     *            Whether GZip encoding is allowed
     */
    public abstract void setAllowGZipEncoding(boolean allowed);

    /**
     * Gets the Results format used for CONSTRUCT/DESCRIBE queries
     * 
     * @return MIME Type for CONSTRUCT/DESCRUBE results
     */
    public abstract String getResultsGraphFormat();

    /**
     * Sets the Results format used for CONSTRUCT/DESCRIBE queries
     * 
     * @param contentType
     *            MIME Type for CONSTRUCT/DESCRIBE results
     */
    public abstract void setResultsGraphFormat(String contentType);

    /**
     * Gets the Results format used for SELECT queries
     * 
     * @return MIME Type for SELECT results
     */
    public abstract String getResultsSelectFormat();

    /**
     * Sets the Results format to be used for SELECT queries
     * 
     * @param contentType
     *            MIME Type for SELECT results
     */
    public abstract void setResultsSelectFormat(String contentType);

    /**
     * Gets the Results format used for ASK queries
     * 
     * @return MIME Type for ASK results
     */
    public abstract String getResultsAskFormat();

    /**
     * Sets the Results format to be used for ASK queries
     * 
     * @param contentType
     *            MIME Type for ASK results
     */
    public abstract void setResultsAskFormat(String contentType);

    /**
     * Gets the timeout for queries
     * 
     * @return Timeout in seconds
     */
    public abstract int getTimeout();

    /**
     * Sets the timeout for queries
     * 
     * @param timeout
     *            Timeout in seconds
     */
    public abstract void setTimeout(int timeout);

    /**
     * Gets the number of times the Query Mix will be run as a warm up prior to
     * actual runs
     * 
     * @return Number of Warmup Runs
     */
    public abstract int getWarmups();

    /**
     * Sets the number of times the Query Mix will be run as a warm up prior to
     * actual runs
     * 
     * @param runs
     *            Number of Warmup Runs
     */
    public abstract void setWarmups(int runs);

    /**
     * Gets the number of times the Query Mix will be run
     * 
     * @return Number of Runs
     */
    public abstract int getRuns();

    /**
     * Sets the number of times the Query Mix will be run
     * 
     * @param runs
     *            Number of Runs
     */
    public abstract void setRuns(int runs);

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

}