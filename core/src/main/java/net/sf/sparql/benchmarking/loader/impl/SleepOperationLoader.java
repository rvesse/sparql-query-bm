/*
 * Copyright 2014 YarcData LLC All Rights Reserved.
 */

package net.sf.sparql.benchmarking.loader.impl;

import java.io.File;
import java.io.IOException;

import net.sf.sparql.benchmarking.loader.AbstractOperationLoader;
import net.sf.sparql.benchmarking.operations.Operation;
import net.sf.sparql.benchmarking.operations.util.SleepOperation;

/**
 * Loader for sleep operation
 * 
 * @author rvesse
 * 
 */
public class SleepOperationLoader extends AbstractOperationLoader {

    @Override
    public Operation load(File baseDir, String[] args) throws IOException {
        try {
            switch (args.length) {
            case 0:
                throw new IOException("Insufficient arguments to load a sleep operation");
            case 1:
                return new SleepOperation(Long.parseLong(args[0]));
            default:
                return new SleepOperation(args[1], Long.parseLong(args[0]));
            }
        } catch (NumberFormatException e) {
            throw new IOException("Invalid numeric argument for sleep operation", e);
        }
    }

    @Override
    public String getPreferredName() {
        return "sleep";
    }

}
