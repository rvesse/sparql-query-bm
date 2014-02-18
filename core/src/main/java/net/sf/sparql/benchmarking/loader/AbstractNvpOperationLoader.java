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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import net.sf.sparql.benchmarking.util.FileUtils;

/**
 * Abstract operation loader for operations that take an NVP file as one of
 * their arguments
 * 
 * @author rvesse
 * 
 */
public abstract class AbstractNvpOperationLoader extends AbstractOperationLoader {

    /**
     * Method which parses NVPs from the given file
     * 
     * @param baseDir
     *            Base directory
     * @param nvpFile
     *            NVP file
     * @return Parsed NVPs
     * @throws IOException
     * @throws FileNotFoundException
     */
    protected final Map<String, List<String>> parseNvps(File baseDir, String nvpFile) throws IOException, FileNotFoundException {
        Map<String, List<String>> nvps;
        String nvpExt = FileUtils.getExtension(nvpFile, true, true);
        if (nvpExt.equals(".tsv")) {
            nvps = new HashMap<String, List<String>>();

            // Read in file and parse individual lines
            String tsvData = this.readFile(baseDir, nvpFile);
            BufferedReader reader = new BufferedReader(new StringReader(tsvData));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length >= 1) {
                    String key = parts[0];
                    if (!nvps.containsKey(key))
                        nvps.put(key, new ArrayList<String>());

                    if (parts.length > 1) {
                        for (int i = 1; i < parts.length; i++) {
                            nvps.get(key).add(parts[i]);
                        }
                    }
                }
            }
        } else if (nvpExt.equals(".properties")) {
            // Try to read as a Java style properties file
            Properties ps = new Properties();
            ps.load(new FileInputStream(this.resolveFile(baseDir, nvpFile)));
            nvps = toNvps(ps);
        } else {
            throw new IOException("Name value pairs are expected to be provided as either a .tsv or .properties file");
        }
        return nvps;
    }

    /**
     * Converts Java properties into the required name value pairs map format
     * 
     * @param ps
     *            Properties
     * @return Name value pairs map format
     */
    private Map<String, List<String>> toNvps(Properties ps) {
        Map<String, List<String>> nvps = new HashMap<String, List<String>>();
        for (Object key : ps.keySet()) {
            if (!nvps.containsKey(key.toString())) {
                nvps.put(key.toString(), new ArrayList<String>());
            }
            nvps.get(key.toString()).add(ps.getProperty(key.toString()));
        }
        return nvps;
    }

    /**
     * Gets the argument that describes the NVPs argument
     * 
     * @return NVPs argument
     */
    protected final OperationLoaderArgument getNvpsArgument() {
        return new OperationLoaderArgument(
                "NVPs File",
                "Provides the path to a file containing NVPs.  This file may in either TSV or Java Properties format.\n"
                        + "In TSV format the first column of each line is taken to be the property name and all subsequent columns are treated as values associated with that name.\n"
                        + "In Java properties format the standard Java properties text based format is supported, each property name is treated as a name and its value associated with that name.  "
                        + "When using Java properties format only a single value may be associated with each name due to the way that Java parses the format.",
                OperationLoaderArgument.TYPE_FILE);
    }

}