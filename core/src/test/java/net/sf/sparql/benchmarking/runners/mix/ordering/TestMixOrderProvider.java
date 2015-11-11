package net.sf.sparql.benchmarking.runners.mix.ordering;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import net.sf.sparql.benchmarking.monitoring.ConsoleProgressListener;
import net.sf.sparql.benchmarking.operations.Operation;
import net.sf.sparql.benchmarking.operations.OperationMix;
import net.sf.sparql.benchmarking.operations.OperationMixImpl;
import net.sf.sparql.benchmarking.operations.util.FailOperation;
import net.sf.sparql.benchmarking.operations.util.SleepOperation;
import net.sf.sparql.benchmarking.options.BenchmarkOptions;
import net.sf.sparql.benchmarking.options.Options;
import net.sf.sparql.benchmarking.options.OptionsImpl;
import net.sf.sparql.benchmarking.runners.BenchmarkRunner;
import net.sf.sparql.benchmarking.runners.mix.IntelligentMixRunner;

public class TestMixOrderProvider {

    private OperationMix createMix(int size) {
        List<Operation> ops = new ArrayList<>();
        while (ops.size() < size) {
            ops.add(new SleepOperation(ops.size() + 1));
        }
        return new OperationMixImpl(ops);
    }

    private OperationMix createFailureMix(int size) {
        List<Operation> ops = new ArrayList<>();
        while (ops.size() < size) {
            ops.add(new FailOperation(String.format("Fail #%d", ops.size() + 1), ops.size() + 1));
        }
        return new OperationMixImpl(ops);
    }

    protected void checkDefault(int mixSize) {
        check(mixSize, new OptionsImpl(), new DefaultMixOrderProvider(), null);
    }

    protected void check(int mixSize, Options options, MixOrderProvider provider, Set<Integer> excludes) {
        OperationMix mix = this.createMix(mixSize);
        options.setOperationMix(mix);

        List<Integer> ids = provider.getOperationOrder(options, mix);
        int expectedSize = mix.size() - (excludes == null ? 0 : excludes.size());
        Assert.assertEquals(expectedSize, ids.size());

        Iterator<Operation> ops = mix.getOperations();
        while (ops.hasNext()) {
            Operation op = ops.next();
            boolean opPresent = ids.contains(op.getId());
            if (excludes != null && excludes.contains(op.getId())) {
                Assert.assertFalse(String.format("Operation ID %d was not excluded as expected", op.getId()),
                        opPresent);
            } else {
                Assert.assertTrue(String.format("Operation ID %d is missing", op.getId()), opPresent);
            }
        }

    }

    @Test
    public void default_01() {
        checkDefault(3);
    }

    @Test
    public void default_02() {
        checkDefault(10);
    }

    @Test
    public void default_03() {
        checkDefault(50);
    }

    @Test
    public void default_04() {
        MixOrderProvider provider = new DefaultMixOrderProvider();
        Options options = new OptionsImpl();
        Set<Integer> excludes = provider.getOperationExcludes(options);
        excludes.add(1);
        excludes.add(3);
        excludes.add(6);
        excludes.add(10);

        check(50, options, provider, excludes);
    }

    @Test
    public void default_05() {
        MixOrderProvider provider = new DefaultMixOrderProvider();
        Options options = new OptionsImpl();
        Set<Integer> excludes = provider.getOperationExcludes(options);
        for (int i = 0; i < 50; i++) {
            excludes.add(i);
        }

        check(50, options, provider, excludes);
    }

    @Test
    public void default_04b() {
        MixOrderProvider provider = new DefaultMixOrderProvider();
        Options options = new OptionsImpl();
        options.setRandomizeOrder(false);
        Set<Integer> excludes = provider.getOperationExcludes(options);
        excludes.add(1);
        excludes.add(3);
        excludes.add(6);
        excludes.add(10);

        check(50, options, provider, excludes);
    }

    @Test
    public void default_05b() {
        MixOrderProvider provider = new DefaultMixOrderProvider();
        Options options = new OptionsImpl();
        options.setRandomizeOrder(false);
        Set<Integer> excludes = provider.getOperationExcludes(options);
        for (int i = 0; i < 50; i++) {
            excludes.add(i);
        }

        check(50, options, provider, excludes);
    }

    @Test
    public void intelligent_01() {
        MixOrderProvider provider = new DefaultMixOrderProvider();
        BenchmarkOptions options = new BenchmarkOptions();
        options.setTimeout(3);
        options.setMaxDelay(0);
        options.setMixRunner(new IntelligentMixRunner(provider, 1, 1.5));
        options.addListener(new ConsoleProgressListener());

        OperationMix mix = this.createMix(5);
        Set<Integer> excludes = provider.getOperationExcludes(options);
        Assert.assertEquals(0, excludes.size());

        // Run the mix
        // This should result in two operations timing out and being excluded
        options.getMixRunner().warmup(new BenchmarkRunner(), options, mix);
        Assert.assertEquals(2, excludes.size());

        check(5, options, provider, excludes);
    }
    
    @Test
    public void intelligent_01b() {
        MixOrderProvider provider = new DefaultMixOrderProvider();
        BenchmarkOptions options = new BenchmarkOptions();
        options.setTimeout(20);
        options.setMaxDelay(0);
        options.setMixRunner(new IntelligentMixRunner(provider, 1, 1.5));
        options.addListener(new ConsoleProgressListener());

        OperationMix mix = this.createMix(5);
        Set<Integer> excludes = provider.getOperationExcludes(options);
        Assert.assertEquals(0, excludes.size());

        // Run the mix
        // Nothing should be excluded as it will all run within the timeout
        options.getMixRunner().warmup(new BenchmarkRunner(), options, mix);
        Assert.assertEquals(0, excludes.size());
        
        // When we do the first real run we'll adjust the timeout appropriately
        options.getMixRunner().run(new BenchmarkRunner(), options, mix);
        Assert.assertEquals(7, options.getTimeout());

        check(5, options, provider, excludes);
    }

    @Test
    public void intelligent_02() {
        MixOrderProvider provider = new DefaultMixOrderProvider();
        BenchmarkOptions options = new BenchmarkOptions();
        options.setTimeout(3);
        options.setMaxDelay(0);
        options.setMixRunner(new IntelligentMixRunner(provider, 1, 1.5));
        options.addListener(new ConsoleProgressListener());

        OperationMix mix = this.createFailureMix(5);
        Set<Integer> excludes = provider.getOperationExcludes(options);
        Assert.assertEquals(0, excludes.size());

        // Run the mix
        // This should result in all operations failing and being excluded
        options.getMixRunner().warmup(new BenchmarkRunner(), options, mix);
        Assert.assertEquals(5, excludes.size());

        check(5, options, provider, excludes);
    }

    @Test
    public void intelligent_03() {
        MixOrderProvider provider = new DefaultMixOrderProvider();
        BenchmarkOptions options = new BenchmarkOptions();
        options.setTimeout(3);
        options.setMaxDelay(0);
        options.setMixRunner(new IntelligentMixRunner(provider, 2, 1.5));
        options.addListener(new ConsoleProgressListener());

        OperationMix mix = this.createFailureMix(5);
        Set<Integer> excludes = provider.getOperationExcludes(options);
        Assert.assertEquals(0, excludes.size());

        // Run the mix
        // This should result in no operations failing enough to reach the
        // failure
        // threshold
        options.getMixRunner().warmup(new BenchmarkRunner(), options, mix);
        Assert.assertEquals(0, excludes.size());

        // Running again 4 of the operations should fail enough to reach the
        // threshold
        options.getMixRunner().warmup(new BenchmarkRunner(), options, mix);
        Assert.assertEquals(4, excludes.size());

        check(5, options, provider, excludes);
    }
}
