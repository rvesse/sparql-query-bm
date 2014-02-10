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

package net.sf.sparql.benchmarking.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;



/**
 * Helper class with utility methods related to files
 * 
 * @author rvesse
 * 
 */
public class FileUtils {

    /**
     * Private constructor prevents direct instantiation
     */
    private FileUtils() {
    }

    /**
     * Checks whether a given path is a file, does not exist (unless
     * {@code allowOverwrite} is true) and is writable
     * 
     * @param filename
     *            Filename to check
     * @param allowOverwrite
     *            Whether overwriting of existing files is allowed
     * @return True if the file is usable, false otherwise
     */
    public static boolean checkFile(String filename, boolean allowOverwrite) {
        File f = new File(filename);
        return FileUtils.checkFile(f, allowOverwrite);
    }

    /**
     * Checks whether a given path is a file, does not exist (unless
     * {@code allowOverwrite} is true) and is writable
     * 
     * @param f
     *            File to check
     * @param allowOverwrite
     *            Whether overwriting of existing files is allowed
     * @return True if the file is usable, false otherwise
     */
    public static boolean checkFile(File f, boolean allowOverwrite) {
    
        // Must not exist or allowOverwrite must be true
        if (f.exists() && !allowOverwrite)
            return false;
    
        // Must be a File if it exists (need exists check because isFile() will
        // return false if path doesn't exist)
        if (f.exists() && !f.isFile())
            return false;
    
        // Make sure we can write to the file
        try {
            FileWriter fw = new FileWriter(f);
            fw.write("test");
            fw.close();
            f.delete();
    
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Gets the extension for a given path
     * 
     * @param path
     *            Path
     * @param includeMultipleExtensions
     *            Whether multiple extensions are returned e.g. {@code .txt.gz}
     * @param includeLeadingDot
     *            Whether the leading {@code .} on the extension is returned
     * @return Extension
     * @throws IOException
     *             Thrown if the path is not a file
     */
    public static String getExtension(String path, boolean includeMultipleExtensions, boolean includeLeadingDot)
            throws IOException {
        return FileUtils.getExtension(new File(path), includeMultipleExtensions, includeLeadingDot);
    }

    /**
     * Gets the extension for a given file
     * 
     * @param file
     *            File
     * @param includeMultipleExtensions
     *            Whether multiple extensions are returned e.g. {@code .txt.gz}
     * @param includeLeadingDot
     *            Whether the leading {@code .} on the extension is returned
     * @return Extension
     * @throws IOException
     *             Thrown if the filename
     */
    public static String getExtension(File file, boolean includeMultipleExtensions, boolean includeLeadingDot) throws IOException {
        String name = file.getName();
        int index = includeMultipleExtensions ? name.indexOf('.') : name.lastIndexOf('.');
        String ext = includeLeadingDot ? name.substring(index) : name.substring(index + 1);
        return ext;
    }
}
