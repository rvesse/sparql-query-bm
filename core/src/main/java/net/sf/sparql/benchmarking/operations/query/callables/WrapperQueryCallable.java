/*
 * Copyright 2014 YarcData LLC All Rights Reserved.
 */

package net.sf.sparql.benchmarking.operations.query.callables;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;

import net.sf.sparql.benchmarking.options.Options;
import net.sf.sparql.benchmarking.runners.Runner;
import net.sf.sparql.benchmarking.stats.impl.QueryRun;

/**
 * A decorator to allow query callables to have parts of their behaviour
 * modified without extending them directly. This is useful for making
 * decorators that change behaviour for both local and remote queries.
 * <p>
 * Note that we specifically prevent decoration of the {@link #call()} method
 * because we want it to call our versions of the relevant protected methods. If
 * we simply decorated this method and called {@link #call()} on the underlying
 * callable then its versions of those methods would be called rather than our
 * decorated versions and the decorator would have no effect.
 * </p>
 * 
 * @author rvesse
 * 
 * @param <T>
 *            Options type
 * @param <TCallable>
 *            Callable type
 */
public abstract class WrapperQueryCallable<T extends Options, TCallable extends AbstractQueryCallable<T>> extends
        AbstractQueryCallable<T> {

    private AbstractQueryCallable<T> callable;

    /**
     * Creates a new decorator
     * 
     * @param runner
     *            Runner
     * @param options
     *            Options
     * @param callable
     *            Callable to decorate
     */
    public WrapperQueryCallable(Runner<T> runner, T options, TCallable callable) {
        super(runner, options);
        this.callable = callable;
    }

    @Override
    public final QueryRun call() {
        return super.call();
    }

    @Override
    protected Query getQuery() {
        return this.callable.getQuery();
    }

    @Override
    protected long countResults(T options, boolean result) {
        return this.callable.countResults(options, result);
    }

    @Override
    protected long countResults(T options, Model m) {
        return this.callable.countResults(options, m);
    }

    @Override
    protected long countResults(T options, ResultSet rset) {
        return this.callable.countResults(options, rset);
    }

    @Override
    protected void customizeRequest(QueryExecution qe) {
        this.callable.customizeRequest(qe);
    }

    @Override
    protected QueryExecution createQueryExecution(Query query) {
        return this.callable.createQueryExecution(query);
    }

}
