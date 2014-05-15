package net.sf.sparql.benchmarking.options;

import net.sf.sparql.benchmarking.runners.mix.DefaultOperationMixRunner;
import net.sf.sparql.benchmarking.runners.operations.DefaultOperationRunner;

/**
 * Options for stress testing
 * 
 * @author rvesse
 * 
 */
public class StressOptions extends OptionsImpl {

	/**
	 * Default maximum number of threads for stress testing i.e. the maximum
	 * number of parallel clients
	 */
	public static final int DEFAULT_MAX_THREADS = 1024;
	/**
	 * Default stress testing runtime threshold in minutes
	 */
	public static final long DEFAULT_RUNTIME = 15;
	/**
	 * Default stress test ramp up factor
	 */
	public static final int DEFAULT_RAMP_UP_FACTOR = 2;

	private int maxThreads = DEFAULT_MAX_THREADS;
	private long runtime = DEFAULT_RUNTIME;
	private int rampUpFactor = DEFAULT_RAMP_UP_FACTOR;

	/**
	 * Creates new stress options
	 */
	public StressOptions() {
		super.setMixRunner(new DefaultOperationMixRunner());
		super.setOperationRunner(new DefaultOperationRunner());
	}

	/**
	 * Sets the maximum number of threads for stress testing
	 * <p>
	 * May be set to 0 or a negative value to indicate there is no maximum
	 * threads
	 * </p>
	 * 
	 * @param threads
	 *            Maximum threads
	 */
	public void setMaxThreads(int threads) {
		this.maxThreads = threads;
	}

	/**
	 * Gets the maximum number of threads for stress testing
	 * <p>
	 * A value <= 0 is considered to indicate that there is no maximum number of
	 * threads
	 * </p>
	 * 
	 * @return Maximum number of threads
	 */
	public int getMaxThreads() {
		return this.maxThreads;
	}

	/**
	 * Gets the maximum stress runtime in minutes
	 * 
	 * @return Stress runtime in minutes
	 */
	public long getMaxRuntime() {
		return runtime;
	}

	/**
	 * Sets the maximum stress runtime in minutes
	 * 
	 * @param runtime
	 *            Soak runtime in minutes
	 */
	public void setMaxRuntime(long runtime) {
		if (runtime <= 0)
			throw new IllegalArgumentException("Maximum runtime must be >= 1");
		this.runtime = runtime;
	}

	/**
	 * Gets the ramp up factor
	 * 
	 * @return Ramp up factor
	 */
	public int getRampUpFactor() {
		return this.rampUpFactor;
	}

	public void setRampUpFactor(int rampUpFactor) {
		if (rampUpFactor < 2)
			throw new IllegalArgumentException("Ramp Up Factor must be >= 2");
		this.rampUpFactor = rampUpFactor;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Options> T copy() {
		StressOptions copy = new StressOptions();
		this.copyStandardOptions(copy);
		copy.setMaxThreads(this.getMaxThreads());
		copy.setMaxRuntime(this.getMaxRuntime());
		copy.setRampUpFactor(this.getRampUpFactor());
		return (T) copy;
	}
}
