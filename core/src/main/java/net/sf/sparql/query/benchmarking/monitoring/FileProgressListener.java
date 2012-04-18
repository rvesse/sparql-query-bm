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

package net.sf.sparql.query.benchmarking.monitoring;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;

/**
 * A Progress Listener that writes the informational messages to a file
 * <p>
 * File is only opened when benchmarking starts, if the file exists then it may either overwrite the existing file or append to it depending on the constructor arguments used.  Default behaviour is to overwrite.
 * </p>
 * @author rvesse
 *
 */
public class FileProgressListener extends StreamProgressListener {
	
	private boolean append = false;
	private File file;
	private static final Logger logger = Logger.getLogger(FileProgressListener.class);
	
	/**
	 * Creates a new File Progress Listener which may append to the file of the given name depending on the {@code append} parameter
	 * @param filename Filename
	 * @param append Whether to append to the file, if false file will be overwritten if it exists
	 */
	public FileProgressListener(String filename, boolean append)
	{
		super(true);
		this.file = new File(filename);
		this.append = append;
	}
	
	/**
	 * Creates a new File Progress Listener which will overwrite a file of the given name
	 * @param filename Filename
	 */
	public FileProgressListener(String filename)
	{
		this(filename, false);
	}
	
	/**
	 * Opens the File as an Output Stream returning null if the file cannot be used for output
	 * @return File Output Stream
	 */
	@Override
	protected OutputStream openStream()
	{
		try 
		{
			return new FileOutputStream(this.file, this.append);
		} 
		catch (FileNotFoundException e) 
		{
			logger.error("Unable to access file '" + this.file.getPath() + "' for use as an output stream - " + e.getMessage());
			return null;
		}
	}
	
}
