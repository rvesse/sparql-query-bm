/*
 * Copyright 2014 YarcData LLC All Rights Reserved.
 */

package net.sf.sparql.benchmarking.operations.parameterized;

import java.util.Collection;

import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.update.UpdateRequest;

import net.sf.sparql.benchmarking.operations.OperationCallable;
import net.sf.sparql.benchmarking.operations.update.UpdateCallable;
import net.sf.sparql.benchmarking.operations.update.UpdateOperation;
import net.sf.sparql.benchmarking.options.Options;
import net.sf.sparql.benchmarking.runners.Runner;
import net.sf.sparql.benchmarking.stats.UpdateRun;

/**
 * A parameterized update operation
 * 
 * @author rvesse
 * 
 */
public class ParameterizedUpdateOperation extends AbstractParameterizedSparqlOperation<UpdateRun> implements UpdateOperation {

    /**
     * Creates a new parameterized update operation
     * 
     * @param sparqlString
     *            SPARQL String
     * @param parameters
     *            Parameters
     * @param name
     *            Name
     */
    public ParameterizedUpdateOperation(String sparqlString, Collection<Binding> parameters, String name) {
        super(sparqlString, parameters, name);
    }

    @Override
    public <T extends Options> boolean canRun(Runner<T> runner, T options) {
        if (options.getUpdateEndpoint() == null) {
            runner.reportProgress(options, "Updates cannot run with no update endpoint specified");
            return false;
        }
        return true;
    }

    @Override
    public String getType() {
        return "Parameterized SPARQL Update";
    }

    @Override
    protected <T extends Options> OperationCallable<T, UpdateRun> createCallable(Runner<T> runner, T options) {
        return new UpdateCallable<T>(this.getUpdate(), runner, options);
    }

    @Override
    protected UpdateRun createErrorInformation(String message, long runtime) {
        return new UpdateRun(message, runtime);
    }

    @Override
    public UpdateRequest getUpdate() {
        return this.getParameterizedSparql().asUpdate();
    }

    @Override
    public String getUpdateString() {
        return this.getParameterizedSparql().getCommandText();
    }

}
