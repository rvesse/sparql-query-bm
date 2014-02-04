/*
 * Copyright 2014 YarcData LLC All Rights Reserved.
 */

package net.sf.sparql.benchmarking.operations.parameterized;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

import net.sf.sparql.benchmarking.operations.AbstractOperation;
import net.sf.sparql.benchmarking.stats.OperationRun;

/**
 * Abstract parameterized SPARQL operation
 * 
 * @author rvesse
 * 
 * @param <TRun>
 *            Run information type
 */
public abstract class AbstractParameterizedSparqlOperation<TRun extends OperationRun> extends AbstractOperation<TRun> {

    private ParameterizedSparqlString sparqlStr;
    private List<Binding> pool = new ArrayList<Binding>();
    private Random random = new Random();

    /**
     * Creates a new parameterized SPARQL operation
     * 
     * @param sparqlString
     *            SPARQL string
     * @param parameters
     *            Parameters to inject, each binding represents a single set of
     *            parameters
     * @param name
     *            Name
     */
    public AbstractParameterizedSparqlOperation(String sparqlString, Collection<Binding> parameters, String name) {
        super(name);
        this.sparqlStr = new ParameterizedSparqlString(sparqlString);
        this.pool.addAll(parameters);
    }

    /**
     * Gets the parameterized SPARQL with a random set of parameters injected
     * 
     * @return Parameterized SPARQL string
     */
    protected final ParameterizedSparqlString getParameterizedSparql() {
        this.sparqlStr.clearParams();
        int r = this.random.nextInt(this.pool.size());
        Binding b = this.pool.get(r);

        Iterator<Var> vs = b.vars();
        while (vs.hasNext()) {
            Var v = vs.next();
            this.sparqlStr.setParam(v.getName(), b.get(v));
        }

        return this.sparqlStr;
    }

    @Override
    public String getContentString() {
        StringBuilder builder = new StringBuilder();
        builder.append(sparqlStr.getCommandText());
        // TODO Display available parameters
        return builder.toString();
    }
}
