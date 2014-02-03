/*
Copyright 2011-2014 Cray Inc. All Rights Reserved

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:

* Redistributions of source code must retain the above copyright
  notice, this list of conditions and the following disclaimer.

* Redistributions in binary form must reproduce the above copyright
  notice, this list of conditions and the following disclaimer in the
  documentation and/or other materials provided with the distribution.

* Neither the name Cray Inc. nor the names of its contributors may be
  used to endorse or promote products derived from this software
  without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
*/

package net.sf.sparql.benchmarking.loader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.util.FileUtils;

/**
 * Abstract operation loader that provides useful protected methods for loader
 * implementations
 * 
 * @author rvesse
 * 
 */
public abstract class AbstractOperationLoader implements OperationLoader {

    private static final Logger logger = LoggerFactory.getLogger(AbstractOperationLoader.class);

    /**
     * Try to read in a given file as UTF-8 content
     * 
     * @param baseDir
     *            Base directory
     * @param filename
     *            Filename
     * @return File contents
     * @throws FileNotFoundException
     * @throws IOException
     */
    protected String readFile(File baseDir, String filename) throws FileNotFoundException, IOException {
        File f = resolveFile(baseDir, filename);

        String query = FileUtils.readWholeFileAsUTF8(f.getPath());
        return query;
    }

    /**
     * Resolves a file to see if it a valid file that exists either on disk or
     * in the class path
     * 
     * @param file
     *            File to resolve
     * @return Resolved file if possible
     * @throws FileNotFoundException
     *             Thrown if the file cannot be resolved
     */
    protected File resolveFile(File baseDir, String filename) throws FileNotFoundException {
        File f = new File(filename);
        if (!f.isAbsolute()) {
            File base = new File(baseDir.getAbsolutePath());
            if (!base.isDirectory())
                base = base.getParentFile();
            f = new File(base.getAbsolutePath() + File.separatorChar + filename);
            logger.info("Made relative path '" + filename + "' into absolute path '" + f.getAbsolutePath() + "'");
        }
        if (!f.exists()) {
            // Try and see if this is actually a resource
            logger.info("Can't find file '" + f.getPath() + "' on disk, seeing if it is a classpath resource...");
            URL u = this.getClass().getResource(f.getPath());
            if (u != null) {
                f = new File(u.getFile());
                logger.info("Located file '" + filename + "' as a classpath resource");
            } else {
                throw new FileNotFoundException("Can't find file '" + filename + "' (" + f.getAbsolutePath()
                        + ") on disk or as a classpath resource");
            }
        }
        if (!f.isFile()) {
            throw new FileNotFoundException("Path '" + filename + "' exists but is not a file");
        }
        return f;
    }

}