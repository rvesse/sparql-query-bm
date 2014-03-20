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
