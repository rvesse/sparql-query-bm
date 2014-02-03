/*
 * Copyright 2014 YarcData LLC All Rights Reserved.
 */

package net.sf.sparql.benchmarking.loader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.sf.sparql.benchmarking.operations.Operation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract implementation of operation mix loader that uses a line based input
 * format. The parsing of individual lines is left to derived implementations.
 * 
 * @author rvesse
 * 
 */
public abstract class AbstractLineBasedMixLoader extends AbstractOperationMixLoader implements OperationMixLoader {

    static final Logger logger = LoggerFactory.getLogger(AbstractLineBasedMixLoader.class);

    @Override
    protected List<Operation> parseFile(File file) throws FileNotFoundException, IOException {
        List<Operation> ops = new ArrayList<Operation>();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        try {
            String line = reader.readLine();
            while (line != null) {
                ops.add(this.parseLine(file.getParentFile(), line));
                line = reader.readLine();
            }
        } finally {
            reader.close();
        }
        return ops;
    }

    /**
     * Parses a line into an operation
     * 
     * @param baseDir
     *            Base directory for resolving relative paths against where
     *            necessary
     * @param line
     *            Line to parse
     * @return Operation
     * @throws IOException
     *             Thrown if the line does not represent a valid operation
     */
    protected abstract Operation parseLine(File baseDir, String line) throws IOException;

}