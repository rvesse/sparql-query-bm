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
