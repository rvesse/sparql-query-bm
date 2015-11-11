package net.sf.sparql.benchmarking.runners.mix.ordering;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.sf.sparql.benchmarking.operations.Operation;
import net.sf.sparql.benchmarking.operations.OperationMix;
import net.sf.sparql.benchmarking.options.Options;

public class DefaultMixOrderProvider extends AbstractMixOrderProvider {

    @Override
    public <T extends Options> List<Integer> getOperationOrder(T options, OperationMix mix) {
        List<Integer> ids = new ArrayList<Integer>();
        Set<Integer> excludes = this.getOperationExcludes(options);
        if (options.getRandomizeOrder()) {
            // Randomize the Order
            List<Integer> unallocatedIds = new ArrayList<Integer>();
            
            // Find the eligible IDs
            Iterator<Operation> ops = mix.getOperations();
            while (ops.hasNext()) {
                int id = ops.next().getId();
                if (excludes.contains(id))
                    continue;
                unallocatedIds.add(id);
            }
            
            // Select from them at random
            while (unallocatedIds.size() > 0) {
                int id = (int) (Math.random() * unallocatedIds.size());
                ids.add(unallocatedIds.get(id));
                unallocatedIds.remove(id);
            }
        } else {
            // Fixed Order
            Iterator<Operation> ops = mix.getOperations();
            while (ops.hasNext()) {
                int id = ops.next().getId();
                if (excludes.contains(id))
                    continue;
                ids.add(id);
            }
        }
        return ids;
    }
}
