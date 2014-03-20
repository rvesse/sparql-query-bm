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
