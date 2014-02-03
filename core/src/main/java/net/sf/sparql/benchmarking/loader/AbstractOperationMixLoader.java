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
import java.util.List;

import net.sf.sparql.benchmarking.operations.Operation;
import net.sf.sparql.benchmarking.operations.OperationMix;
import net.sf.sparql.benchmarking.operations.OperationMixImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract implementation of an operation mix loader that parses the mix file
 * as a while
 * 
 * @author rvesse
 * 
 */
public abstract class AbstractOperationMixLoader implements OperationMixLoader {

    private static final Logger logger = LoggerFactory.getLogger(AbstractOperationMixLoader.class);

    @Override
    public OperationMix load(File file) throws IOException {
        file = resolveFile(file);
        try {
            List<Operation> ops = parseFile(file);

            return new OperationMixImpl(ops);
        } catch (FileNotFoundException e) {
            logger.error("Error reading mix file: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        } catch (IOException e) {
            logger.error("Error reading mix file: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
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
    protected File resolveFile(File file) throws FileNotFoundException {
        if (!file.exists()) {
            // Try and see if this is actually a resource
            logger.info("Can't find mix file '" + file + "' on disk, seeing if it is a class path resource...");
            URL u = this.getClass().getResource(file.getPath());
            if (u != null) {
                file = new File(u.getFile());
                logger.info("Located mix file '" + file + "' as a class path resource");
            } else {
                throw new FileNotFoundException("Can't find mix file '" + file.getPath() + "' (" + file.getAbsolutePath()
                        + ") on disk or as a class path resource");
            }
        }
        if (!file.isFile()) {
            throw new FileNotFoundException("Mix file path '" + file.getPath() + "' exists but is not a file");
        }
        return file;
    }

    /**
     * Parses a file to produce an operation mix
     * 
     * @param file
     *            File
     * @return Operation Mix
     * @throws FileNotFoundException
     *             Thrown if the mix file cannot be found
     * @throws IOException
     *             Thrown if there is a problem accessing the mix file or
     *             parsing its contents
     */
    protected abstract List<Operation> parseFile(File file) throws FileNotFoundException, IOException;

}