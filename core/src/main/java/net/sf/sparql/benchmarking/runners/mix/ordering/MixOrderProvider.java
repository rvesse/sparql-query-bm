package net.sf.sparql.benchmarking.runners.mix.ordering;

import java.util.List;
import java.util.Set;

import net.sf.sparql.benchmarking.operations.OperationMix;
import net.sf.sparql.benchmarking.options.Options;

/**
 * Interface for classes which provide an ordering of operations to be run to an
 * operation mix runner
 * 
 * @author rvesse
 *
 */
public interface MixOrderProvider {

    /**
     * Gets the set of operation IDs which should be excluded from the provided
     * orders
     * 
     * @param options
     *            Options
     * @return Set of excluded operation IDs
     */
    public abstract <T extends Options> Set<Integer> getOperationExcludes(T options);

    /**
     * Gets the order in which the runner should run the operations
     * 
     * @param options
     *            Options
     * @param mix
     *            Mix containing the operations to be run
     * @return Operation order expressed as a list of IDs
     */
    public abstract <T extends Options> List<Integer> getOperationOrder(T options, OperationMix mix);

    /**
     * Whether the runner should report the order of operations as a progress
     * message
     * <p>
     * The default implementation returns {@code true} so operation order is
     * always reported
     * </p>
     * 
     * @return True if operation order should be default
     */
    public abstract <T extends Options> boolean reportOperationOrder(T options);
}
