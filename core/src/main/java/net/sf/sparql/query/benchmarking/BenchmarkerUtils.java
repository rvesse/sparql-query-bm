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

package net.sf.sparql.query.benchmarking;

import java.io.File;
import java.io.FileWriter;
import java.util.concurrent.TimeUnit;

/**
 * Utility Functions for Benchmarking
 * <p>
 * Note some of these functions mimic the functionality of {@link TimeUnit#convert()} except they keep the calculations as doubles which is important for us because we want as much precision in our measurements as possible
 * </p>
 * @author rvesse
 *
 */
public class BenchmarkerUtils {

	public static final double NANOSECONDS_PER_MILLISECONDS = 1000000,
			MILLISECONDS_PER_SECONDS = 1000,
			SECONDS_PER_HOUR = 3600;
	
	/**
	 * Converts nanoseconds to milliseconds
	 * @param nanoseconds
	 * @return
	 */
	public static double toMilliseconds(long nanoseconds)
	{
		return toMilliseconds((double)nanoseconds);
	}
	
	/**
	 * Converts nanoseconds to milliseconds
	 * @param nanoseconds
	 * @return
	 */
	public static double toMilliseconds(double nanoseconds)
	{
		return nanoseconds / NANOSECONDS_PER_MILLISECONDS;
	}
	
	/**
	 * Converts nanoseconds to seconds
	 * @param nanoseconds
	 * @return
	 */
	public static double toSeconds(long nanoseconds)
	{
		return toSeconds((double)nanoseconds);
	}
	
	/**
	 * Converts nanoseconds to seconds
	 * @param nanoseconds
	 * @return
	 */
	public static double toSeconds(double nanoseconds)
	{
		double ms = nanoseconds / NANOSECONDS_PER_MILLISECONDS;
		return ms / MILLISECONDS_PER_SECONDS;
	}
	
	/**
	 * Formats Time to show as seconds
	 * @param time Time in nanoseconds
	 * @return Time in seconds
	 */
	public static String formatTime(long time)
	{
		return BenchmarkerUtils.toSeconds(time) + "s";
	}
	
	/**
	 * Formats Time to show as seconds
	 * @param time Time in nanoseconds
	 * @return Time in seconds
	 */
	public static String formatTime(double time)
	{
		return BenchmarkerUtils.toSeconds(time) + "s";
	}
	
	/**
	 * Formats a string for CSV escaping it as a double quoted CSV string if necessary
	 * @param value
	 * @return
	 */
	public static String toCsv(String value)
	{
		if (value.contains(","))
		{
			return "\"" + escapeQuotesForCsv(value) + "\"";
		}
		else
		{
			return value;
		}
	}
	
	/**
	 * Escapes quotes in a string for use in a double quoted CSV string
	 * @param value
	 * @return
	 */
	private static String escapeQuotesForCsv(String value)
	{
		if (value.contains("\""))
		{
			return value.replace("\"","\"\"");
		}
		else
		{
			return value;	
		}
	}
	
	/**
	 * Checks whether a given path is a File, does not exist (unless {@code allowOverwrite} is true) and is writable
	 * @param filename Filename to check
	 * @param allowOverwrite Whether overwriting of existing files is allowed
	 * @return True if the file is usable, false otherwise
	 */
	public static boolean checkFile(String filename, boolean allowOverwrite)
	{
		File f = new File(filename);
		
		//Must not exist or allowOverwrite must be true
		if (f.exists() && !allowOverwrite) return false;
		
		//Must be a File if it exists (need exists check because isFile() will return false if path doesn't exist)
		if (f.exists() && !f.isFile()) return false;
		
		//Make sure we can write to the file
		try 
		{
			FileWriter fw = new FileWriter(f);
			fw.write("test");
			fw.close();
			f.delete();
			
			return true;
		} 
		catch (Exception e)
		{
			return false;
		}
	}
}
