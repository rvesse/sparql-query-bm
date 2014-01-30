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

package net.sf.sparql.query.benchmarking.queries;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;

import net.sf.sparql.query.benchmarking.operations.BenchmarkOperationMixImpl;

import com.hp.hpl.jena.query.QueryParseException;
import com.hp.hpl.jena.util.FileUtils;

/**
 * Represents a set of queries that form a Benchmark
 * 
 * @author rvesse
 * 
 */
public class BenchmarkQueryMix extends BenchmarkOperationMixImpl {

    /**
     * Creates a Query Mix from a set of Query Strings
     * 
     * @param queries
     */
    public BenchmarkQueryMix(String[] queries) {
        for (String query : queries) {
            this.addOperation(new BenchmarkQuery("", query));
        }
    }

    /**
     * Creates a Query Mix from a File
     * <p>
     * The file is assumed to contain a list of paths to actual query files
     * </p>
     * 
     * @param file
     */
    public BenchmarkQueryMix(String file) {
        try {
            // Try to read the Query List File
            File f = new File(file);
            if (!f.exists()) {
                // Try and see if this is actually a resource
                logger.info("Can't find query list file '" + file + "' on disk, seeing if it is a classpath resource...");
                URL u = this.getClass().getResource(file);
                if (u != null) {
                    f = new File(u.getFile());
                    logger.info("Located query list file '" + file + "' as a classpath resource");
                } else {
                    throw new FileNotFoundException("Can't find query list file '" + file + "' (" + f.getAbsolutePath()
                            + ") on disk or as a classpath resource");
                }
            }
            BufferedReader reader = new BufferedReader(new FileReader(f));
            try {
                String line = reader.readLine();
                while (line != null) {
                    // For Each Query File listed try to load a Query
                    f = new File(line);
                    if (!f.isAbsolute()) {
                        File base = new File(file);
                        base = base.getParentFile();
                        f = new File(base.getPath() + File.separatorChar + line);
                        logger.info("Made relative path '" + line + "' into absolute path '" + f.getAbsolutePath() + "'");
                    }
                    if (!f.exists()) {
                        // Try and see if this is actually a resource
                        logger.info("Can't find file '" + f.getPath() + "' on disk, seeing if it is a classpath resource...");
                        URL u = this.getClass().getResource(f.getPath());
                        if (u != null) {
                            f = new File(u.getFile());
                            logger.info("Located query file '" + file + "' as a classpath resource");
                        } else {
                            throw new FileNotFoundException("Can't find query file '" + line + "' (" + f.getAbsolutePath()
                                    + ") on disk or as a classpath resource");
                        }
                    }

                    String query = FileUtils.readWholeFileAsUTF8(f.getPath());
                    try {
                        this.addOperation(new BenchmarkQuery(f.getName(), query));
                    } catch (QueryParseException e) {
                        logger.error("Error reading query file: " + e.getMessage());
                        throw new QueryParseException("Query parsing error reading query file " + f.getAbsolutePath() + "\n"
                                + e.getMessage(), e.getCause(), e.getLine(), e.getColumn());
                    }
                    line = reader.readLine();
                }
            } finally {
                reader.close();
            }
        } catch (FileNotFoundException e) {
            logger.error("Error reading query file: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        } catch (IOException e) {
            logger.error("Error reading query file: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Creates a Query Mix from an iterator
     * 
     * @param queries
     *            Queries
     */
    public BenchmarkQueryMix(Iterator<BenchmarkQuery> queries) {
        while (queries.hasNext()) {
            this.addOperation(queries.next());
        }
    }

    /**
     * Creates a query mix from a collecton of queries
     * 
     * @param queries
     *            Queries
     */
    public BenchmarkQueryMix(Collection<BenchmarkQuery> queries) {
        this(queries.iterator());
    }
}
