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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.jena.atlas.web.auth.HttpAuthenticator;

import org.apache.jena.query.Dataset;

import net.sf.sparql.benchmarking.monitoring.ProgressListener;
import net.sf.sparql.benchmarking.operations.OperationMix;
import net.sf.sparql.benchmarking.runners.mix.OperationMixRunner;
import net.sf.sparql.benchmarking.runners.operations.OperationRunner;

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
    private Dataset dataset;
    private Map<String, String> customEndpoints = new HashMap<String, String>();
    private Map<String, Object> customSettings = new HashMap<String, Object>();
    private int timeout = DEFAULT_TIMEOUT;
    private String selectResultsFormat = DEFAULT_FORMAT_SELECT;
    private String askResultsFormat = DEFAULT_FORMAT_SELECT;
    private String graphResultsFormat = DEFAULT_FORMAT_GRAPH;
    private int delay = DEFAULT_MAX_DELAY;
    private int parallelThreads = DEFAULT_PARALLEL_THREADS;
    private boolean allowCompression = false;
    private HttpAuthenticator authenticator;
    private AtomicLong globalOrder = new AtomicLong(0);
    private boolean randomize = true;
    int sanity = DEFAULT_SANITY_CHECKS;
    private OperationMix setupMix;
    private OperationMix teardownMix;
    private OperationMixRunner mixRunner;
    private OperationRunner opRunner;
    private long limit = DEFAULT_LIMIT;
    private long localLimit = DEFAULT_LIMIT;
    private boolean noCount = false;
    private boolean ensureAbsoluteURIs = false;

    @Override
    public void addListener(ProgressListener listener) {
        if (!this.listeners.contains(listener)) {
            this.listeners.add(listener);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Options> T copy() {
        OptionsImpl copy = new OptionsImpl();
        this.copyStandardOptions(copy);
        return (T) copy;
    }

    /**
     * Copies standard options across from this instance
     * <p>
     * Primarily intended for use by derived implementations which extend the
     * basic options to make it easier for them to create copies of themselves.
     * </p>
     * 
     * @param copy
     *            Copy to copy to
     */
    protected final void copyStandardOptions(OptionsImpl copy) {
        copy.setAllowCompression(this.getAllowCompression());
        copy.setAuthenticator(this.getAuthenticator());
        for (String key : this.customEndpoints.keySet()) {
            copy.setCustomEndpoint(key, this.getCustomEndpoint(key));
        }
        copy.setDataset(this.getDataset());
        copy.setEnsureAbsoluteURIs(this.getEnsureAbsoluteURIs());
        copy.setGraphStoreEndpoint(this.getGraphStoreEndpoint());
        copy.setHaltAny(this.getHaltAny());
        copy.setHaltBehaviour(this.getHaltBehaviour());
        copy.setHaltOnError(this.getHaltOnError());
        copy.setHaltOnTimeout(this.getHaltOnTimeout());
        copy.setLimit(this.getLimit());
        copy.setLocalLimit(this.getLocalLimit());
        copy.setMaxDelay(this.getMaxDelay());
        copy.setMixRunner(this.getMixRunner());
        copy.setNoCount(this.getNoCount());
        copy.setOperationMix(this.getOperationMix());
        copy.setOperationRunner(this.getOperationRunner());
        copy.setParallelThreads(this.getParallelThreads());
        copy.setQueryEndpoint(this.getQueryEndpoint());
        copy.setRandomizeOrder(this.getRandomizeOrder());
        copy.setResultsAskFormat(this.getResultsAskFormat());
        copy.setResultsGraphFormat(this.getResultsGraphFormat());
        copy.setResultsSelectFormat(this.getResultsSelectFormat());
        copy.setSanityCheckLevel(this.getSanityCheckLevel());
        copy.setSetupMix(this.getSetupMix());
        copy.setTeardownMix(this.getTeardownMix());
        copy.setTimeout(this.getTimeout());
        copy.globalOrder.set(this.globalOrder.get());
    }

    @Override
    public boolean getAllowCompression() {
        return allowCompression;
    }

    @Override
    public HttpAuthenticator getAuthenticator() {
        return this.authenticator;
    }

    @Override
    public String getCustomEndpoint(String name) {
        return customEndpoints.get(name);
    }

    @Override
    public Map<String, String> getCustomEndpoints() {
        return Collections.unmodifiableMap(this.customEndpoints);
    }

    @Override
    public Map<String, Object> getCustomSettings() {
        return this.customSettings;
    }
    
    @Override
    public Dataset getDataset() {
        return dataset;
    }
    
    @Override
    public boolean getEnsureAbsoluteURIs() {
        return ensureAbsoluteURIs;
    }

    @Override
    public ExecutorService getExecutor() {
        return executor;
    }

    @Override
    public long getGlobalOrder() {
        return globalOrder.incrementAndGet();
    }

    @Override
    public String getGraphStoreEndpoint() {
        return graphStoreEndpoint;
    }

    @Override
    public boolean getHaltAny() {
        return haltAny;
    }

    @Override
    public HaltBehaviour getHaltBehaviour() {
        return haltBehaviour;
    }

    @Override
    public boolean getHaltOnError() {
        return haltOnError;
    }

    @Override
    public boolean getHaltOnTimeout() {
        return haltOnTimeout;
    }

    @Override
    public long getLimit() {
        return limit;
    }
    
    @Override
    public long getLocalLimit() {
        return localLimit;
    }

    @Override
    public List<ProgressListener> getListeners() {
        return this.listeners;
    }

    @Override
    public int getMaxDelay() {
        return delay;
    }

    @Override
    public OperationMixRunner getMixRunner() {
        return this.mixRunner;
    }

    @Override
    public boolean getNoCount() {
        return noCount;
    }

    @Override
    public OperationMix getOperationMix() {
        return operationMix;
    }

    @Override
    public OperationRunner getOperationRunner() {
        return this.opRunner;
    }

    @Override
    public int getParallelThreads() {
        return parallelThreads;
    }

    @Override
    public String getQueryEndpoint() {
        return queryEndpoint;
    }

    @Override
    public boolean getRandomizeOrder() {
        return randomize;
    }

    @Override
    public String getResultsAskFormat() {
        return askResultsFormat;
    }

    @Override
    public String getResultsGraphFormat() {
        return graphResultsFormat;
    }

    @Override
    public String getResultsSelectFormat() {
        return selectResultsFormat;
    }

    @Override
    public int getSanityCheckLevel() {
        return sanity;
    }

    @Override
    public OperationMix getSetupMix() {
        return this.setupMix;
    }

    @Override
    public OperationMix getTeardownMix() {
        return this.teardownMix;
    }

    @Override
    public int getTimeout() {
        return timeout;
    }

    @Override
    public String getUpdateEndpoint() {
        return updateEndpoint;
    }

    @Override
    public void removeListener(ProgressListener listener) {
        this.listeners.remove(listener);
    }

    @Override
    public void resetGlobalOrder() {
        globalOrder.set(0);
    }

    @Override
    public void setAllowCompression(boolean allowed) {
        allowCompression = allowed;
    }

    @Override
    public void setAuthenticator(HttpAuthenticator authenticator) {
        this.authenticator = authenticator;
    }

    @Override
    public void setCustomEndpoint(String name, String endpoint) {
        this.customEndpoints.put(name, endpoint);
    }
    
    @Override
    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }
    
    @Override
    public void setEnsureAbsoluteURIs(boolean ensureAbsolute) {
        this.ensureAbsoluteURIs = ensureAbsolute;
    }

    @Override
    public void setGraphStoreEndpoint(String endpoint) {
        this.graphStoreEndpoint = endpoint;
    }

    @Override
    public void setHaltAny(boolean halt) {
        haltAny = halt;
        if (halt) {
            haltOnError = true;
            haltOnTimeout = true;
        }
    }

    @Override
    public void setHaltBehaviour(HaltBehaviour behaviour) {
        haltBehaviour = behaviour;
    }

    @Override
    public void setHaltOnError(boolean halt) {
        haltOnError = halt;
    }

    @Override
    public void setHaltOnTimeout(boolean halt) {
        haltOnTimeout = halt;
    }

    @Override
    public void setLimit(long limit) {
        this.limit = limit;
    }
    
    @Override
    public void setLocalLimit(long limit) {
        this.localLimit = limit;
    }

    @Override
    public void setMaxDelay(int milliseconds) {
        if (delay < 0)
            delay = 0;
        delay = milliseconds;
    }

    @Override
    public void setMixRunner(OperationMixRunner runner) {
        this.mixRunner = runner;
    }

    @Override
    public void setNoCount(boolean noCount) {
        this.noCount = noCount;
    }

    @Override
    public void setOperationMix(OperationMix queries) {
        operationMix = queries;
    }

    @Override
    public void setOperationRunner(OperationRunner runner) {
        this.opRunner = runner;
    }

    @Override
    public void setParallelThreads(int threads) {
        if (threads < 1)
            threads = 1;
        parallelThreads = threads;
    }

    @Override
    public void setQueryEndpoint(String endpoint) {
        this.queryEndpoint = endpoint;
    }

    @Override
    public void setRandomizeOrder(boolean randomize) {
        this.randomize = randomize;
    }

    @Override
    public void setResultsAskFormat(String contentType) {
        askResultsFormat = contentType;
    }

    @Override
    public void setResultsGraphFormat(String contentType) {
        graphResultsFormat = contentType;
    }

    @Override
    public void setResultsSelectFormat(String contentType) {
        selectResultsFormat = contentType;
    }

    @Override
    public void setSanityCheckLevel(int level) {
        sanity = level;
    }

    @Override
    public void setSetupMix(OperationMix mix) {
        this.setupMix = mix;
    }

    @Override
    public void setTeardownMix(OperationMix mix) {
        this.teardownMix = mix;
    }

    @Override
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    @Override
    public void setUpdateEndpoint(String endpoint) {
        this.updateEndpoint = endpoint;
    }
}