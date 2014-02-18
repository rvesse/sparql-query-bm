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

/**
 * Represents information about an operation loader argument
 * 
 * @author rvesse
 * 
 */
public class OperationLoaderArgument {

    /**
     * Indicates the argument type is a string denoting a file
     */
    public static final int TYPE_FILE = 1;
    /**
     * Indicates the argument type is a long
     */
    public static final int TYPE_LONG = 10;
    /**
     * Indicates the argument type is a string
     */
    public static final int TYPE_STRING = 0;

    private boolean optional = false;
    private int type = OperationLoaderArgument.TYPE_STRING;
    private String name, description;

    /**
     * Creates a new argument
     * 
     * @param name
     *            Short name for the argument
     */
    public OperationLoaderArgument(String name) {
        this(name, OperationLoaderArgument.TYPE_STRING);
    }

    /**
     * Creates a new argument
     * 
     * @param name
     *            Short name for the argument
     * @param type
     *            Expected argument type
     */
    public OperationLoaderArgument(String name, int type) {
        this(name, null, type);
    }

    /**
     * Creates a new argument
     * 
     * @param name
     *            Short name for the argument
     * @param description
     *            Description of the purpose of the argument
     */
    public OperationLoaderArgument(String name, String description) {
        this(name, description, OperationLoaderArgument.TYPE_STRING);
    }

    /**
     * Creates a new argument
     * 
     * @param name
     *            Short name for the argument
     * @param description
     *            Description of the purpose of the argument
     * @param type
     *            Expected argument type
     */
    public OperationLoaderArgument(String name, String description, int type) {
        this(name, description, type, false);
    }

    /**
     * Creates a new argument
     * 
     * @param name
     *            Short name for the argument
     * @param description
     *            Description of the purpose of the argument
     * @param type
     *            Expected argument type
     * @param optional
     *            Whether the argument is optional
     */
    public OperationLoaderArgument(String name, String description, int type, boolean optional) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.optional = optional;
    }

    /**
     * Gets a short name for the argument
     * 
     * @return Name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets a description of the purpose of the argument
     * 
     * @return Description
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Gets whether the argument is optional
     * 
     * @return True if optional, false otherwise
     */
    public boolean isOptional() {
        return this.optional;
    }

    /**
     * Gets the expected type of the argument
     * 
     * @return Argument type
     */
    public int getType() {
        return this.type;
    }
}
