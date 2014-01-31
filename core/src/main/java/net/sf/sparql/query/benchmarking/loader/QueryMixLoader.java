/*
 * Copyright 2014 YarcData LLC All Rights Reserved.
 */

package net.sf.sparql.query.benchmarking.loader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.sparql.query.benchmarking.operations.BenchmarkOperationMix;

public class QueryMixLoader implements OperationMixLoader {

    private static final Logger logger = LoggerFactory.getLogger(QueryMixLoader.class);

    @Override
    public BenchmarkOperationMix load(File file) throws IOException {
        if (!file.exists()) {
            // Try and see if this is actually a resource
            logger.info("Can't find query list file '" + file + "' on disk, seeing if it is a classpath resource...");
            URL u = this.getClass().getResource(file.getPath());
            if (u != null) {
                file = new File(u.getFile());
                logger.info("Located query list file '" + file + "' as a classpath resource");
            } else {
                throw new FileNotFoundException("Can't find query list file '" + file.getPath() + "' (" + file.getAbsolutePath()
                        + ") on disk or as a classpath resource");
            }
        }
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            try {
                String line = reader.readLine();
                while (line != null) {
                    // TODO Do something which each line
                    line = reader.readLine();
                }
            } finally {
                reader.close();
            }
        } catch (FileNotFoundException e) {
            logger.error("Error reading mix file: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        } catch (IOException e) {
            logger.error("Error reading mix file: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

}
