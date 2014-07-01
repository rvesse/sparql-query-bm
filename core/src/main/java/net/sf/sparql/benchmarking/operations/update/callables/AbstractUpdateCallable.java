/*
SPARQL Query Benchmarker is licensed under a 3 Clause BSD License

----------------------------------------------------------------------

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

package net.sf.sparql.benchmarking.operations.update.callables;

import org.apache.jena.atlas.web.HttpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.update.UpdateProcessor;
import com.hp.hpl.jena.update.UpdateRequest;

import net.sf.sparql.benchmarking.operations.AbstractOperationCallable;
import net.sf.sparql.benchmarking.options.Options;
import net.sf.sparql.benchmarking.runners.Runner;
import net.sf.sparql.benchmarking.stats.impl.UpdateRun;
import net.sf.sparql.benchmarking.util.ErrorCategories;
import net.sf.sparql.benchmarking.util.FormatUtils;

/**
 * Abstract callable for update operations
 * 
 * @author rvesse
 * 
 * @param <T>
 *            Options type
 */
public abstract class AbstractUpdateCallable<T extends Options> extends AbstractOperationCallable<T> {

    static final Logger logger = LoggerFactory.getLogger(AbstractUpdateCallable.class);

    /**
     * Creates a new callable
     * 
     * @param runner
     *            Runner
     * @param options
     *            Options
     */
    public AbstractUpdateCallable(Runner<T> runner, T options) {
        super(runner, options);
    }

    /**
     * Gets the update request to be executed
     * 
     * @return Update request
     */
    protected abstract UpdateRequest getUpdate();

    /**
     * Provides derived implementations the option to customize the update
     * processor before actually executing the update e.g. to add custom
     * parameters
     * <p>
     * The default implementation does nothing.
     * </p>
     * 
     * @param processor
     *            Update processor
     */
    protected void customizeRequest(UpdateProcessor processor) {
        // Default implementation does nothing
    }

    /**
     * Creates an update processor for running the update
     * 
     * @param update
     *            Update
     * 
     * @return Update processor
     */
    protected abstract UpdateProcessor createUpdateProcessor(UpdateRequest update);

    @Override
    public UpdateRun call() throws Exception {
        UpdateRequest update = this.getUpdate();
        logger.debug("Running update:\n" + update.toString());

        // Create a remote update processor and configure it appropriately
        UpdateProcessor processor = this.createUpdateProcessor(update);
        this.customizeRequest(processor);

        long startTime = System.nanoTime();
        try {
            // Execute the update
            processor.execute();
        } catch (HttpException e) {
            // Make sure to categorize HTTP errors appropriately
            logger.error("{}", FormatUtils.formatException(e));
            return new UpdateRun(e.getMessage(), ErrorCategories.categorizeHttpError(e), System.nanoTime() - startTime);
        }

        if (this.isCancelled())
            return null;

        long endTime = System.nanoTime();
        return new UpdateRun(endTime - startTime);
    }

}