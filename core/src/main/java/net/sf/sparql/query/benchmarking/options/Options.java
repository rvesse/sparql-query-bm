/*
 * Copyright 2014 YarcData LLC All Rights Reserved.
 */

package net.sf.sparql.query.benchmarking.options;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.apache.jena.atlas.web.auth.HttpAuthenticator;
import org.apache.jena.riot.WebContent;

import net.sf.sparql.query.benchmarking.HaltBehaviour;
import net.sf.sparql.query.benchmarking.monitoring.ProgressListener;
import net.sf.sparql.query.benchmarking.operations.BenchmarkOperationMix;

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

    public abstract void resetGlobalOrder();

    public abstract long getGlobalOrder();

    public abstract int getParallelThreads();

    public abstract void setParallelThreads(int threads);

    public abstract int getMaxDelay();

    public abstract void setMaxDelay(int milliseconds);

    public abstract HttpAuthenticator getAuthenticator();

    public abstract void setAuthenticator(HttpAuthenticator authenticator);

    public abstract boolean getAllowDeflateEncoding();

    public abstract void setAllowDeflateEncoding(boolean allowed);

    public abstract boolean getAllowGZipEncoding();

    public abstract void setAllowGZipEncoding(boolean allowed);

    public abstract String getResultsGraphFormat();

    public abstract void setResultsGraphFormat(String contentType);

    public abstract String getResultsSelectFormat();

    public abstract void setResultsSelectFormat(String contentType);

    public abstract String getResultsAskFormat();

    public abstract void setResultsAskFormat(String contentType);

    public abstract int getTimeout();

    public abstract void setTimeout(int timeout);

    public abstract int getWarmups();

    public abstract void setWarmups(int runs);

    public abstract int getRuns();

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
    public abstract void setOperationMix(BenchmarkOperationMix queries);

    /**
     * Gets the Query Mix that is used
     * 
     * @return Query Mix
     */
    public abstract BenchmarkOperationMix getOperationMix();

    public abstract boolean getRandomizeOrder();

    public abstract void setRandomizeOrder(boolean randomize);

}