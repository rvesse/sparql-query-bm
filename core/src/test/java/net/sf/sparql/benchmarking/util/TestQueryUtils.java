package net.sf.sparql.benchmarking.util;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.syntax.ElementSubQuery;
import org.junit.Assert;
import org.junit.Test;

public class TestQueryUtils {

    private void testNoChange(String query) {
        Query original = QueryFactory.create(query);
        Query summarized = QueryUtils.summarize(original);

        Assert.assertSame(original, summarized);
    }

    private void testSelectSummarized(String query) {
        Query original = QueryFactory.create(query);
        Assert.assertTrue(original.isSelectType());
        Query summarized = QueryUtils.summarize(original);
        Assert.assertTrue(summarized.isSelectType());
        Assert.assertNotSame(original, summarized);

        Assert.assertTrue(summarized.getQueryPattern() instanceof ElementSubQuery);
        Assert.assertEquals(original, ((ElementSubQuery) summarized.getQueryPattern()).getQuery());

        // Check round trip parserable
        String output = summarized.toString();
        //System.out.println(output);
        QueryFactory.create(output);
    }

    @Test
    public void summarize_01() {
        testNoChange("ASK { }");
    }
    
    @Test
    public void summarize_02() {
        testNoChange("DESCRIBE <urn:test>");
    }
    
    @Test
    public void summarize_03() {
        testSelectSummarized("SELECT * WHERE { }");
    }
    
    @Test
    public void summarize_04() {
        testSelectSummarized("PREFIX : <urn:prefix:> SELECT * WHERE { }");
    }
    
    @Test
    public void summarize_05() {
        testSelectSummarized("PREFIX : <urn:prefix:> SELECT * WHERE { ?s :predicate ?o }");
    }
}
