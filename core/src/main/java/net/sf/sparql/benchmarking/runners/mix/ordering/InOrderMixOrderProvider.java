package net.sf.sparql.benchmarking.runners.mix.ordering;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.sf.sparql.benchmarking.operations.Operation;
import net.sf.sparql.benchmarking.operations.OperationMix;
import net.sf.sparql.benchmarking.options.Options;

public class InOrderMixOrderProvider extends AbstractMixOrderProvider {

    @Override
    public <T extends Options> List<Integer> getOperationOrder(T options, OperationMix mix) {
        List<Integer> ids = new ArrayList<Integer>();
        // Fixed Order
        Iterator<Operation> ops = mix.getOperations();
        Set<Integer> excludes = this.getOperationExcludes(options);
        while (ops.hasNext()) {
            int id = ops.next().getId();
            if (excludes.contains(id))
                continue;
            ids.add(id);
        }
        return ids;
    }

    /**
     * Returns {@code false} since there is no need to report the operation
     * order since it will always be the same
     */
    @Override
    public <T extends Options> boolean reportOperationOrder(T options) {
        return false;
    }
}
