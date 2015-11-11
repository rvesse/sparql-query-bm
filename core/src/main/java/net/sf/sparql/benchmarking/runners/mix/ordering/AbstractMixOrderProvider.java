package net.sf.sparql.benchmarking.runners.mix.ordering;

import java.util.HashSet;
import java.util.Set;

import net.sf.sparql.benchmarking.options.Options;

public abstract class AbstractMixOrderProvider implements MixOrderProvider {
    
    private static final String EXCLUDED_IDS = "excludedOperationIDs";

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Options> Set<Integer> getOperationExcludes(T options) {
        Object value = options.getCustomSettings().get(EXCLUDED_IDS);
        if (value == null) {
            value = new HashSet<Integer>();
            options.getCustomSettings().put(EXCLUDED_IDS, value);
        }
        return (Set<Integer>) value;
    }

    @Override
    public <T extends Options> boolean reportOperationOrder(T options) {
        return true;
    }

}
