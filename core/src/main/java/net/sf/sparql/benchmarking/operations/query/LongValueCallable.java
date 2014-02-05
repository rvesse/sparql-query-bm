/*
 * Copyright 2014 YarcData LLC All Rights Reserved.
 */

package net.sf.sparql.benchmarking.operations.query;

import net.sf.sparql.benchmarking.options.Options;
import net.sf.sparql.benchmarking.runners.Runner;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.util.NodeFactoryExtra;

/**
 * A callable for getting the value of a specific variable in the first row of a
 * result set as a long
 * <p>
 * Usually used in conjunction with custom operations like
 * {@link DatasetSizeOperation} which calculate some aggregate on the data using
 * a {@code SELECT} query and want to return that aggregate value as the number
 * of results rather than the number of results rows as the basic
 * {@link QueryCallable} would return.
 * </p>
 * 
 * @author rvesse
 * 
 * @param <T>
 *            Options type
 */
public class LongValueCallable<T extends Options> extends QueryCallable<T> {

    private String var;

    /**
     * Creates a new callable
     * 
     * @param q
     *            Query
     * @param var
     *            Variable whose value is to be retrieved
     * @param runner
     *            Runner
     * @param options
     *            Options
     */
    public LongValueCallable(Query q, String var, Runner<T> runner, T options) {
        super(q, runner, options);
        this.var = var;
    }

    @Override
    protected long countResults(T options, ResultSet rset) {
        if (rset.hasNext()) {
            Binding b = rset.nextBinding();
            Node n = b.get(Var.alloc(this.var));
            if (n == null)
                return 0;
            return NodeFactoryExtra.nodeToLong(n);
        } else {
            return 0;
        }
    }

}
