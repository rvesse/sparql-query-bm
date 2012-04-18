/**
 * Copyright 2012 Robert Vesse
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * you may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.sparql.query.benchmarking.parallel;

/**
 * A Timer that can be used to time runtime where runtime may be being consumed on multiple threads and overlapping, allows you to determine actual runtime used
 * <p>
 * Essentially this is just a class that tracks the number of threads that have started the timer and the time at which the timer was last started when there were no active threads
 * using {@code synchronized} to ensure that only one thread can access a method at any one time.
 * </p>
 * <p>
 * Then when the timer is stopped and there are no active threads the total time elapsed can be incremented by the difference between the last start time and the current time.  It
 * may be the case that multiple distinct threads have run and started and stopped the timer.  But provided active threads is greater than zero at least one thread is performing
 * the operation we wish to time so we are always counting actual time and not counting time multiply as we would be doing if we just summed the individual runtimes of the threads.
 * </p>
 * @author rvesse
 *
 */
public class ParallelTimer {
	
	private long total = 0;
	private long startTime;
	private long active = 0;
	
	/**
	 * Starts timing
	 */
	public synchronized void start()
	{
		if (active == 0)
		{
			//Start Timer
			active++;
			startTime = System.nanoTime();
		}
		else
		{
			//Increment Active Threads
			active++;
		}
	}
	
	/**
	 * Stops timing
	 */
	public synchronized void stop()
	{
		if (active == 0)
		{
			//Nothing to do
		}
		else if (active == 1)
		{
			//Stop Timer
			active--;
			total += System.nanoTime() - startTime;
		}
		else
		{
			//Decrement Active Threads
			active--;
		}
	}
	
	/**
	 * Gets the actual runtime consumed
	 * @return Actual Runtime consumed
	 */
	public synchronized long getActualRuntime()
	{
		if (active == 0)
		{
			return total;
		}
		else
		{
			return total + (System.nanoTime() - startTime);
		}
	}
	
	/**
	 * Gets the number of active threads currently consuming runtime in parallel
	 * @return Active Threads
	 */
	public synchronized long getActiveThreads()
	{
		return active;
	}
}
