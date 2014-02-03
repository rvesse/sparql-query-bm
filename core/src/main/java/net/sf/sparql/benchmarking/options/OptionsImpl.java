/*
 * Copyright 2014 YarcData LLC All Rights Reserved.
 */

package net.sf.sparql.benchmarking.options;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.jena.atlas.web.auth.HttpAuthenticator;

import net.sf.sparql.benchmarking.HaltBehaviour;
import net.sf.sparql.benchmarking.monitoring.ProgressListener;
import net.sf.sparql.benchmarking.operations.OperationMix;

/**
 * Implementation of generic options
 * 
 * @author rvesse
 * 
 */
public class OptionsImpl implements Options {

    private boolean haltOnTimeout = false;
    private boolean haltOnError = false;
    private boolean haltAny = false;
    private HaltBehaviour haltBehaviour = DEFAULT_HALT_BEHAVIOUR;
    private List<ProgressListener> listeners = new ArrayList<ProgressListener>();
    private ExecutorService executor = Executors.newCachedThreadPool();
    private OperationMix operationMix;
    private String queryEndpoint;
    private String updateEndpoint;
    private String graphStoreEndpoint;
    private Map<String, String> customEndpoints = new HashMap<String, String>();
    private int runs = DEFAULT_RUNS;
    private int warmups = DEFAULT_WARMUPS;
    private int timeout = DEFAULT_TIMEOUT;
    private String selectResultsFormat = DEFAULT_FORMAT_SELECT;
    private String askResultsFormat = DEFAULT_FORMAT_SELECT;
    private String graphResultsFormat = DEFAULT_FORMAT_GRAPH;
    private int delay = DEFAULT_MAX_DELAY;
    private int parallelThreads = DEFAULT_PARALLEL_THREADS;
    private boolean allowGZip = false;
    private boolean allowDeflate = false;
    private HttpAuthenticator authenticator;
    private AtomicLong globalOrder = new AtomicLong(0);
    private boolean randomize = true;

    @Override
    public ExecutorService getExecutor() {
        return executor;
    }

    /**
     * Sets the Halt on Timeout behavior
     * 
     * @param halt
     *            Whether a timeout should cause benchmarking to abort
     */
    public void setHaltOnTimeout(boolean halt) {
        haltOnTimeout = halt;
    }

    @Override
    public boolean getHaltOnTimeout() {
        return haltOnTimeout;
    }

    /**
     * Sets the Halt on Error behavior
     * 
     * @param halt
     *            Whether an error should cause benchmarking to abort
     */
    public void setHaltOnError(boolean halt) {
        haltOnError = halt;
    }

    @Override
    public boolean getHaltOnError() {
        return haltOnError;
    }

    /**
     * Sets Halt on Any behavior, if set to true sets Halt on Error and Halt on
     * Timeout to true as well
     * 
     * @param halt
     *            Whether any issue should cause benchmarking to abort
     */
    public void setHaltAny(boolean halt) {
        haltAny = halt;
        if (halt) {
            haltOnError = true;
            haltOnTimeout = true;
        }
    }

    @Override
    public boolean getHaltAny() {
        return haltAny;
    }

    /**
     * Sets the Halting Behaviour
     * 
     * @param behaviour
     *            Halting Behaviour
     */
    public void setHaltBehaviour(HaltBehaviour behaviour) {
        haltBehaviour = behaviour;
    }

    @Override
    public HaltBehaviour getHaltBehaviour() {
        return haltBehaviour;
    }

    @Override
    public List<ProgressListener> getListeners() {
        return this.listeners;
    }

    @Override
    public void addListener(ProgressListener listener) {
        if (!this.listeners.contains(listener)) {
            this.listeners.add(listener);
        }
    }

    @Override
    public void removeListener(ProgressListener listener) {
        this.listeners.remove(listener);
    }

    @Override
    public OperationMix getOperationMix() {
        return operationMix;
    }

    @Override
    public void setOperationMix(OperationMix queries) {
        operationMix = queries;
    }

    @Override
    public void setQueryEndpoint(String endpoint) {
        this.queryEndpoint = endpoint;
    }

    @Override
    public String getQueryEndpoint() {
        return queryEndpoint;
    }

    @Override
    public void setUpdateEndpoint(String endpoint) {
        this.updateEndpoint = endpoint;
    }

    @Override
    public String getUpdateEndpoint() {
        return updateEndpoint;
    }

    @Override
    public void setGraphStoreEndpoint(String endpoint) {
        this.graphStoreEndpoint = endpoint;
    }

    @Override
    public String getGraphStoreEndpoint() {
        return graphStoreEndpoint;
    }

    @Override
    public void setCustomEndpoint(String name, String endpoint) {
        this.customEndpoints.put(name, endpoint);
    }

    @Override
    public String getCustomEndpoint(String name) {
        return customEndpoints.get(name);
    }

    @Override
    public Map<String, String> getCustomEndpoints() {
        return Collections.unmodifiableMap(this.customEndpoints);
    }

    /**
     * Sets the number of times the Query Mix will be run
     * 
     * @param runs
     *            Number of Runs
     */
    @Override
    public void setRuns(int runs) {
        if (runs < 0)
            runs = 1;
        this.runs = runs;
    }

    /**
     * Gets the number of times the Query Mix will be run
     * 
     * @return Number of Runs
     */
    @Override
    public int getRuns() {
        return runs;
    }

    /**
     * Sets the number of times the Query Mix will be run as a warm up prior to
     * actual runs
     * 
     * @param runs
     *            Number of Warmup Runs
     */
    @Override
    public void setWarmups(int runs) {
        if (runs <= 0)
            runs = 0;
        warmups = runs;
    }

    /**
     * Gets the number of times the Query Mix will be run as a warm up prior to
     * actual runs
     * 
     * @return Number of Warmup Runs
     */
    @Override
    public int getWarmups() {
        return warmups;
    }

    /**
     * Sets the timeout for queries
     * 
     * @param timeout
     *            Timeout in seconds
     */
    @Override
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    /**
     * Gets the timeout for queries
     * 
     * @return Timeout in seconds
     */
    @Override
    public int getTimeout() {
        return timeout;
    }

    /**
     * Sets the Results format to be used for ASK queries
     * 
     * @param contentType
     *            MIME Type for ASK results
     */
    @Override
    public void setResultsAskFormat(String contentType) {
        askResultsFormat = contentType;
    }

    /**
     * Gets the Results format used for ASK queries
     * 
     * @return MIME Type for ASK results
     */
    @Override
    public String getResultsAskFormat() {
        return askResultsFormat;
    }

    /**
     * Sets the Results format to be used for SELECT queries
     * 
     * @param contentType
     *            MIME Type for SELECT results
     */
    @Override
    public void setResultsSelectFormat(String contentType) {
        selectResultsFormat = contentType;
    }

    /**
     * Gets the Results format used for SELECT queries
     * 
     * @return MIME Type for SELECT results
     */
    @Override
    public String getResultsSelectFormat() {
        return selectResultsFormat;
    }

    /**
     * Sets the Results format used for CONSTRUCT/DESCRIBE queries
     * 
     * @param contentType
     *            MIME Type for CONSTRUCT/DESCRIBE results
     */
    @Override
    public void setResultsGraphFormat(String contentType) {
        graphResultsFormat = contentType;
    }

    /**
     * Gets the Results format used for CONSTRUCT/DESCRIBE queries
     * 
     * @return MIME Type for CONSTRUCT/DESCRUBE results
     */
    @Override
    public String getResultsGraphFormat() {
        return graphResultsFormat;
    }

    /**
     * Sets whether the client should allow the server to return GZip encoded
     * responses
     * 
     * @param allowed
     *            Whether GZip encoding is allowed
     */
    @Override
    public void setAllowGZipEncoding(boolean allowed) {
        allowGZip = allowed;
    }

    /**
     * Gets whether the client will allow the server to return GZip encoded
     * responses
     * 
     * @return Whether GZip encoding is allowed
     */
    @Override
    public boolean getAllowGZipEncoding() {
        return allowGZip;
    }

    /**
     * Sets whether the client will allow the server to return Deflate encoded
     * responses
     * 
     * @param allowed
     *            Whether Deflate encoding is allowed
     */
    @Override
    public void setAllowDeflateEncoding(boolean allowed) {
        allowDeflate = allowed;
    }

    /**
     * Gets whether the client will allow the server to return Deflate encoded
     * responses
     * 
     * @return Whether Deflate encoding is allowed
     */
    @Override
    public boolean getAllowDeflateEncoding() {
        return allowDeflate;
    }

    /**
     * Sets the HTTP authenticator used
     * 
     * @param authenticator
     *            HTTP authenticator
     * @since 1.1.0
     */
    @Override
    public void setAuthenticator(HttpAuthenticator authenticator) {
        this.authenticator = authenticator;
    }

    /**
     * Gets the HTTP authenticator in use
     * 
     * @return HTTP authenticator
     * @since 1.1.0
     */
    @Override
    public HttpAuthenticator getAuthenticator() {
        return this.authenticator;
    }

    /**
     * Sets the maximum delay between queries
     * 
     * @param milliseconds
     *            Maximum Delay in milliseconds
     */
    @Override
    public void setMaxDelay(int milliseconds) {
        if (delay < 0)
            delay = 0;
        delay = milliseconds;
    }

    /**
     * Gets the maximum delay between queries
     * 
     * @return Maximum Delay in milliseconds
     */
    @Override
    public int getMaxDelay() {
        return delay;
    }

    /**
     * Sets the number of parallel threads used for query run evaluation
     * 
     * @param threads
     *            Number of Parallel Threads
     */
    @Override
    public void setParallelThreads(int threads) {
        if (threads < 1)
            threads = 1;
        parallelThreads = threads;
    }

    /**
     * Gets the number of parallel threads used for query run evaluation
     * 
     * @return Number of parallel threads
     */
    @Override
    public int getParallelThreads() {
        return parallelThreads;
    }

    /**
     * Gets the Global Run Order
     * <p>
     * Called elsewhere so that mix runs and query runs record what order they
     * were run in
     * </p>
     * 
     * @return Global Run Order
     */
    @Override
    public long getGlobalOrder() {
        return globalOrder.incrementAndGet();
    }

    /**
     * Resets the global run order
     * <p>
     * Useful for runners that incorporate warmups into their runs
     * </p>
     */
    @Override
    public void resetGlobalOrder() {
        globalOrder.set(0);
    }

    /**
     * Sets whether operation order should be randomized
     * 
     * @param randomize
     *            Whether operation order should be random
     */
    @Override
    public void setRandomizeOrder(boolean randomize) {
        this.randomize = randomize;
    }

    /**
     * Gets whether operation order  should be randomized
     * 
     * @return Whether operation order is random
     */
    @Override
    public boolean getRandomizeOrder() {
        return randomize;
    }

}