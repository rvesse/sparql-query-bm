package net.sf.sparql.benchmarking.operations.query;

import org.apache.jena.query.Query;

import net.sf.sparql.benchmarking.operations.OperationCallable;
import net.sf.sparql.benchmarking.operations.query.callables.LongValueCallable;
import net.sf.sparql.benchmarking.operations.query.callables.RemoteQueryCallable;
import net.sf.sparql.benchmarking.options.Options;
import net.sf.sparql.benchmarking.runners.Runner;
import net.sf.sparql.benchmarking.util.QueryUtils;

public class SummarizedFixedQueryOperation extends FixedQueryOperation {

    public SummarizedFixedQueryOperation(String name, String queryString) {
        super(name, queryString);
    }

    @Override
    public <T extends Options> OperationCallable<T> createCallable(Runner<T> runner, T options) {
        Query rawQuery = this.getQuery();
        Query summaryQuery = QueryUtils.summarize(rawQuery);
        if (rawQuery == summaryQuery) {
            // If unchanged run as normal
            return super.createCallable(runner, options);
        } else {
            // If summarised run as a scalar value retrieval
            return new LongValueCallable<T, RemoteQueryCallable<T>>(runner, options,
                    new RemoteQueryCallable<T>(summaryQuery, runner, options),
                    QueryUtils.SUMMARIZED_QUERY_RESULT_VARIABLE);
        }
    }

}
