package net.sf.sparql.benchmarking.runners.mix.ordering;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import net.sf.sparql.benchmarking.operations.Operation;
import net.sf.sparql.benchmarking.operations.OperationMix;
import net.sf.sparql.benchmarking.options.Options;

public class SamplingMixOrderProvider extends AbstractMixOrderProvider {
    private int sampleSize = 0;
    private boolean allowRepeats = false;

    /**
     * Creates an order provider with the given sample size
     * 
     * @param sampleSize
     *            Sample size, if <= 0 then sample size will always be the mix
     *            size
     */
    public SamplingMixOrderProvider(int sampleSize) {
        this(sampleSize, false);
    }

    /**
     * Creates an order provider which optionally allows repeats, the sample
     * size will always be the mix size
     * 
     * @param allowRepeats
     *            Whether to allow repeats
     */
    public SamplingMixOrderProvider(boolean allowRepeats) {
        this(0, allowRepeats);
    }

    /**
     * Creates an order provider with the given sample size that optionally
     * allows repeats
     * 
     * @param sampleSize
     *            Sample size, if <= 0 then sample size will always be the mix
     *            size
     * @param allowRepeats
     *            Whether to allow repeats
     */
    public SamplingMixOrderProvider(int sampleSize, boolean allowRepeats) {
        this.sampleSize = sampleSize;
        this.allowRepeats = allowRepeats;
    }

    @Override
    public <T extends Options> List<Integer> getOperationOrder(T options, OperationMix mix) {
        if (options.getRandomizeOrder()) {
            return getRandomSample(options, mix);
        } else {
            return getInOrderSample(options, mix);
        }
    }

    protected <T extends Options> List<Integer> getRandomSample(T options, OperationMix mix) {
        List<Integer> ids = new ArrayList<Integer>();
        int limit = this.sampleSize > 0 ? this.sampleSize : mix.size();

        // Prepare the pool of eligible IDs
        List<Integer> pool = getPool(options, mix);

        Random random = new Random();
        while (ids.size() < limit) {
            if (pool.size() == 0) {
                // If the desired sample size is greater than the mix size and
                // repeats are not allowed the pool will be empty before we've
                // reached the limit so we just return a sample that is of the
                // size of the mix
                return ids;
            }

            // Pick next operation from the pool
            int id = random.nextInt(pool.size());
            ids.add(id);

            // Remove from pool when not allowing repeats
            if (!this.allowRepeats) {
                ids.remove(new Integer(id));
            }
        }

        return ids;
    }

    protected <T extends Options> List<Integer> getPool(T options, OperationMix mix) {
        List<Integer> pool = new ArrayList<Integer>();
        Set<Integer> excludes = this.getOperationExcludes(options);
        Iterator<Operation> ops = mix.getOperations();
        while (ops.hasNext()) {
            int id = ops.next().getId();
            if (excludes.contains(id))
                continue;
            pool.add(id);
        }
        return pool;
    }

    protected <T extends Options> List<Integer> getInOrderSample(T options, OperationMix mix) {
        List<Integer> ids = new ArrayList<Integer>();
        int limit = this.sampleSize > 0 ? this.sampleSize : mix.size();

        // Prepare the pool of eligible IDs
        List<Integer> pool = getPool(options, mix);

        for (int i = 0; ids.size() < limit; i++) {
            if (i >= pool.size()) {
                // If the desired sample size is greater than the mix size then
                // we either wrap around if repeats are allowed or we return a
                // sample that is of the size of the mix
                if (this.allowRepeats) {
                    i = 0;
                } else {
                    return ids;
                }
            }
            ids.add(pool.get(i));
        }
        return ids;
    }
}
