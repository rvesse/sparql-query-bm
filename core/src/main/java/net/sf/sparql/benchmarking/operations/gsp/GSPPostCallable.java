/*
 * Copyright 2014 YarcData LLC All Rights Reserved.
 */

package net.sf.sparql.benchmarking.operations.gsp;

import com.hp.hpl.jena.query.DatasetAccessor;
import com.hp.hpl.jena.rdf.model.Model;

import net.sf.sparql.benchmarking.options.Options;
import net.sf.sparql.benchmarking.runners.Runner;

/**
 * A callable which runs the Graph Store Protocol POST operation
 * 
 * @author rvesse
 * 
 * @param <T>
 *            Options type
 */
public class GSPPostCallable<T extends Options> extends AbstractGSPCallable<T> {

    private Model data;

    /**
     * Creates a new callable that operates on the default graph
     * 
     * @param runner
     *            Runner
     * @param options
     *            Options
     * @param data
     *            Data to be added
     */
    public GSPPostCallable(Runner<T> runner, T options, Model data) {
        this(runner, options, data, null);
    }

    /**
     * Creates a new callable that operates on a specific graph
     * 
     * @param runner
     *            Runner
     * @param options
     *            Options
     * @param data
     *            Data to be added
     * @param uri
     *            Graph URI
     */
    public GSPPostCallable(Runner<T> runner, T options, Model data, String uri) {
        super(runner, options, uri);
        this.data = data;
    }

    @Override
    protected long doOperation(DatasetAccessor accessor) {
        if (this.isDefaultGraphUri()) {
            accessor.add(this.data);
        } else {
            accessor.add(this.getGraphUri(), this.data);
        }
        return 0;
    }

}
