package net.sf.sparql.benchmarking.util;

import org.apache.jena.query.Query;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.aggregate.AggCount;
import org.apache.jena.sparql.expr.aggregate.Aggregator;
import org.apache.jena.sparql.syntax.ElementSubQuery;

public class QueryUtils {

    /**
     * Name of the result variable used for queries which are summarised
     */
    public static final String SUMMARIZED_QUERY_RESULT_VARIABLE = "sqbmResultSummary";

    /**
     * Private constructor prevents direction instantiation
     */
    private QueryUtils() {
    }

    /**
     * Rewrites a query into summarised form
     * <p>
     * This aims to rewrite the query such that the system being tested needs to
     * do all the work necessary to answer the query but only need return the
     * count of results rather than materialising and sending all the individual
     * result rows. Rewrites are based upon the type of the query so for some
     * types of query the summarised query may not request the system do the
     * exact same work as would have originally been required. This is primarily
     * true in the case of {@code CONSTRUCT} and {@code DESCRIBE} queries.
     * </p>
     * 
     * @param rawQuery
     *            Query to summarise
     * @return Summarised query, in some cases this may be the same as the input
     *         query
     */
    public static Query summarize(Query rawQuery) {
        // Rewrite depending on the query type
        if (rawQuery.isAskType()) {
            // No rewrite needed
            return rawQuery;
        } else if (rawQuery.isConstructType()) {
            // Summarise by just wrapping the WHERE clause
            Query summaryQuery = prepareWrapper(rawQuery.getPrologue());
            summaryQuery.setQueryPattern(rawQuery.getQueryPattern());
            return summaryQuery;
        } else if (rawQuery.isDescribeType()) {
            // Summarise differently depending on whether it has a WHERE clause
            // or not
            if (rawQuery.getQueryPattern() != null) {
                // Has a WHERE clause
                Query summaryQuery = prepareWrapper(rawQuery.getPrologue());
                summaryQuery.setQueryPattern(rawQuery.getQueryPattern());
                return summaryQuery;
            } else {
                // Has no WHERE clause, leave as-is
                return rawQuery;
            }
        } else if (rawQuery.isSelectType()) {
            // Simple wrap as sub-query in a SELECT
            Query summaryQuery = prepareWrapper(rawQuery);
            rawQuery.setBaseURI((String) null);
            rawQuery.setPrefixMapping(new PrefixMappingImpl());
            summaryQuery.setQueryPattern(new ElementSubQuery(rawQuery));
            return summaryQuery;
        } else {
            // Leave unknown query types as-is
            return rawQuery;
        }

    }

    private static Query prepareWrapper(Prologue prologue) {
        Query summaryQuery = new Query();
        if (prologue.explicitlySetBaseURI()) {
            summaryQuery.setBaseURI(prologue.getBaseURI());
        }
        summaryQuery.setPrefixMapping(prologue.getPrefixMapping());

        summaryQuery.setQuerySelectType();
        Aggregator countAgg = new AggCount();
        Expr countExpr = summaryQuery.allocAggregate(countAgg);
        summaryQuery.addResultVar(SUMMARIZED_QUERY_RESULT_VARIABLE, countExpr);
        return summaryQuery;
    }
}
