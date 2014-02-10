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

package net.sf.sparql.query.benchmarking.cmd;

import javax.inject.Inject;

import net.sf.sparql.benchmarking.options.Options;
import io.airlift.command.HelpOption;
import io.airlift.command.Option;

/**
 * Abstract command which provides all the common options
 * 
 * @author rvesse
 * 
 */
public class AbstractCommand {

    /**
     * Help option
     */
    @Inject
    public HelpOption helpOption;

    /**
     * Halt on timeout option
     */
    @Option(name = "--halt-on-timeout", description = "Sets whether the tests will halt once an operation times out")
    public boolean haltOnTimeout = false;

    /**
     * Halt on error option
     */
    @Option(name = "--halt-on-error", description = "Sets whether the tests will halt once an error is encountered")
    public boolean haltOnError = false;

    /**
     * Halt on any option
     */
    @Option(name = "--halt-any", description = "Sets whether the tests will halt once any issue is encountered")
    public boolean haltAny = false;

    /**
     * Timeout option
     */
    @Option(name = { "-t", "--timeout" }, arity = 1, title = "Seconds", description = "Sets the operation timeout in seconds, a zero/negative value is used to indicate no timeout")
    public int timeout = Options.DEFAULT_TIMEOUT;

    /**
     * Parallel threads option
     */
    @Option(name = { "-p", "--parallel" }, arity = 1, title = "Threads", description = "Sets the number of parallel threads to use for testing")
    public int parallelThreads = 1;

    /**
     * Maximum delay between operations option
     */
    @Option(name = { "-m", "--max-delay" }, arity = 1, title = "Milliseconds", description = "Sets the maximum delay between operations in milliseconds")
    public int maxDelay = Options.DEFAULT_MAX_DELAY;

    /**
     * Query endpoint option
     */
    @Option(name = { "-q", "--query-endpoint" }, arity = 1, title = "Query Endpoint URI", description = "Sets the SPARQL query endpoint URI")
    public String queryEndpoint;

    /**
     * Update endpoint option
     */
    @Option(name = { "-u", "--update-endpoint" }, arity = 1, title = "Update Endpoint URI", description = "Sets the SPARQL update endpoint URI")
    public String updateEndpoint;

    /**
     * Graph store endpoint option
     */
    @Option(name = { "-g", "--gsp-endpoint" }, arity = 1, title = "Graph Store Endpoint URI", description = "Sets the SPARQL graph store protocol endpoint URI")
    public String gspEndpoint;

    /**
     * Enable compression option
     */
    @Option(name = { "--compression", "--allow-compression" }, description = "Enables the use of GZip/Deflate compression when communicating with the server assuming the server supports it")
    public boolean enableCompression = false;

    /**
     * ASK Format option
     */
    @Option(name = { "--results-ask", "--ask-format" }, arity = 1, title = "MIME Type", description = "Sets the results format that will be requested for ASK queries")
    public String askFormat = Options.DEFAULT_FORMAT_ASK;
    
    /**
     * SELECT Format option
     */
    @Option(name = { "--results-select", "--select-format" }, arity = 1, title = "MIME Type", description = "Sets the results format that will be requested for SELECT queries")
    public String selectFormat = Options.DEFAULT_FORMAT_SELECT;
    
    /**
     * Graph Format option
     */
    @Option(name = { "--results-graph", "--graph-format" }, arity = 1, title = "MIME Type", description = "Sets the results format that will be requested for CONSTRUCT/DESCRIBE queries and operations that retrieve a graph")
    public String graphFormat = Options.DEFAULT_FORMAT_GRAPH;

    /**
     * Mix option
     */
    @Option(name = { "-m", "--mix" }, arity = 1, title = "Mix File", required = true, description = "Sets the operation mix file which provides the mix of operations to be run")
    public String mixFile;

    /**
     * Sanity checking option
     */
    @Option(name = { "-s", "--sanity-checks" }, arity = 1, title = "Level", description = "Sets the sanity checking level, this is the number of basic sanity checks the system to be tested must pass before actual tests will be started")
    public int sanityCheckLevel = Options.DEFAULT_SANITY_CHECKS;

    /**
     * Disable random order option
     */
    @Option(name = { "--norand", "--no-random" }, description = "Disables randomized ordering of operations within mixes")
    public boolean noRandom = false;
    
    /**
     * User name option
     */
    @Option(name = { "-u", "--username" }, arity = 1, title = "Username", description = "Sets the user name used for authentication")
    public String username;
    
    /**
     * Password option
     */
    @Option(name = { "-p", "--password" }, arity = 1, title = "Password", description = "Sets the password used for authentication")
    public String password;
    
    /**
     * Form URL option
     */
    @Option(name = { "--form-url" }, arity = 1, title = "Form URL", description = "Sets the URL used to login for form based authentication")
    public String formUrl;
    
    /**
     * Form user name field
     */
    @Option(name = { "--form-user-field" }, arity = 1, title = "Form User Field", description = "Sets the user name field used for form based authentication (defaults to httpd_username)")
    public String formUserField;
    
    /**
     * Form password field
     */
    @Option(name = { "--form-password-field" }, arity = 1, title = "Form Password Field", description = "Sets the password field used for form based authentication (defaults to httpd_password)")
    public String formPwdField;
    
    /**
     * Logging to console option
     */
    @Option(name = { "--logging" }, description = "Enables log output to the console")
    public boolean logToConsole = false;
    
    /**
     * Logging to file option
     */
    @Option(name = { "--log-file" }, arity = 1, title = "File", description = "Enables logging to a file")
    public String logFile;
    
    /**
     * Debug option
     */
    @Option(name = { "--debug" }, description = "Enables debug level logging, must be used with the --logging or --log-file option to have a visible effect")
    public boolean debug = false;
    
    /**
     * Trace option
     */
    @Option(name = { "--trace" }, description = "Enables trace level logging, must be used with the --logging or --log-file option to have a visible effect")
    public boolean trace = false;
}
