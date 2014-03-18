/*
 * Copyright 2014 YarcData LLC All Rights Reserved.
 */

package net.sf.sparql.benchmarking.operations.query.callables;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

import net.sf.sparql.benchmarking.options.Options;
import net.sf.sparql.benchmarking.runners.Runner;

/**
 * A callable which counts SELECT results by retrieving the value of a specific
 * column of the first row and then converting that value into an integer
 * 
 * @author rvesse
 * @param <T>
 *            Options type
 * @param <TCallable>
 *            Callable type
 * 
 */
public abstract class AbstractScalarValueCallable<T extends Options, TCallable extends AbstractQueryCallable<T>> extends
        WrapperQueryCallable<T, TCallable> {

    private String var;

    /**
     * Creates a new scalar value callable
     * 
     * @param runner
     *            Runner
     * @param options
     *            Options
     * @param callable
     *            Callable to decorate
     * @param var
     *            Variable name to take the value from
     */
    public AbstractScalarValueCallable(Runner<T> runner, T options, TCallable callable, String var) {
        super(runner, options, callable);
        if (var == null)
            throw new NullPointerException("A null variable are not permitted");
        this.var = var;
    }

    @Override
    protected long countResults(T options, ResultSet rset) {
        if (rset.hasNext()) {
            Binding b = rset.nextBinding();
            Node n = b.get(Var.alloc(this.var));
            if (n == null)
                return 0;
            return nodeToLong(n);
        } else {
            return 0;
        }
    }

    /**
     * Method that should be implemented by derived classes to convert the node
     * into an integer
     * 
     * @param n
     *            Node
     * @return Long integer
     */
    protected abstract long nodeToLong(Node n);

}
