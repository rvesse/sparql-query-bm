/** 
 * Copyright 2011-2012 Cray Inc. All Rights Reserved
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name Cray Inc. nor the names of its contributors may be
 *   used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 **/

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
