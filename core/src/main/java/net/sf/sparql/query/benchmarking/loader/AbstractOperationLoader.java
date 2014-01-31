/*
 * Copyright 2014 YarcData LLC All Rights Reserved.
 */

package net.sf.sparql.query.benchmarking.loader;

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
     * Try to read in a given file
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

        String query = FileUtils.readWholeFileAsUTF8(f.getPath());
        return query;
    }

}