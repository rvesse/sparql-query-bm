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

import java.util.Hashtable;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.apache.jena.atlas.web.HttpException;

import com.hp.hpl.jena.sparql.engine.http.QueryExceptionHTTP;

/**
 * Helper class containing constants and methods related to error categories
 * 
 * @author rvesse
 * 
 */
public class ErrorCategories {

    private static Map<Integer, String> descriptions = new Hashtable<Integer, String>();

    static {
        init();
    }

    static synchronized void init() {
        descriptions.put(NONE, "No Error");
        descriptions.put(TIMEOUT, "Operation Timeout");
        descriptions.put(INTERRUPT, "Operation Interrupted");
        descriptions.put(EXECUTION, "Execution Error");
        descriptions.put(AUTHENTICATION, "Authentication Error");
        descriptions.put(HTTP_CLIENT_ERROR, "HTTP Client Error (4xx)");
        descriptions.put(HTTP_SERVER_ERROR, "HTTP Server Error (5xx)");
        descriptions.put(HTTP_NOT_FOUND, "HTTP Not Found (404/410)");
    }

    /**
     * Resets the descriptions to their defaults
     */
    public static void resetDescriptions() {
        descriptions.clear();
        init();
    }

    /**
     * Adds a description for a category
     * 
     * @param category
     *            Category
     * @param description
     *            Description
     */
    public static void addDescription(int category, String description) {
        descriptions.put(category, description);
    }

    /**
     * Gets the description for the category which may be null if there is no
     * registered description
     * 
     * @param category
     *            Category
     * @return Category description if available, null otherwise
     */
    public static String getDescription(int category) {
        return descriptions.get(category);
    }

    /**
     * Private constructor prevents direct instantiation
     */
    private ErrorCategories() {
    }

    /**
     * Category indicating there was no error
     */
    public static final int NONE = 0;

    /**
     * Category indicating a time out
     */
    public static final int TIMEOUT = 1;

    /**
     * Category indicating an interrupt
     */
    public static final int INTERRUPT = 2;

    /**
     * Category indicating an execution error within an operation
     */
    public static final int EXECUTION = 3;

    /**
     * Category indicating an authentication error
     */
    public static final int AUTHENTICATION = 4;

    /**
     * Category indicating a HTTP client error
     */
    public static final int HTTP_CLIENT_ERROR = 400;

    /**
     * Category indicating a HTTP server error
     */
    public static final int HTTP_SERVER_ERROR = 500;

    /**
     * Category indicating a HTTP Not Found (404/410 error)
     */
    public static final int HTTP_NOT_FOUND = 404;

    /**
     * Categorizes a {@link HttpException}
     * <p>
     * Where possible this will use specific error categories such as
     * {@link #AUTHENTICATION} or {@link #HTTP_NOT_FOUND} however many possible
     * HTTP status codes are not specifically categorized and will be bucketed
     * into {@link #HTTP_CLIENT_ERROR} or {@link #HTTP_SERVER_ERROR} as
     * appropriate.
     * </p>
     * 
     * @param httpError
     *            HTTP error
     * @return Most appropriate error category
     */
    public static int categorizeHttpError(HttpException httpError) {
        int status = httpError.getResponseCode();
        return categorizeHttpError(status);
    }

    /**
     * Categorizes a {@link QueryExceptionHTTP}
     * <p>
     * Where possible this will use specific error categories such as
     * {@link #AUTHENTICATION} or {@link #HTTP_NOT_FOUND} however many possible
     * HTTP status codes are not specifically categorized and will be bucketed
     * into {@link #HTTP_CLIENT_ERROR} or {@link #HTTP_SERVER_ERROR} as
     * appropriate.
     * </p>
     * 
     * @param httpError
     *            HTTP error
     * @return Most appropriate error category
     */
    public static int categorizeHttpError(QueryExceptionHTTP httpError) {
        int status = httpError.getResponseCode();
        return categorizeHttpError(status);
    }

    /**
     * Categorizes a HTTP error based on the status code
     * <p>
     * Where possible this will use specific error categories such as
     * {@link #AUTHENTICATION} or {@link #HTTP_NOT_FOUND} however many possible
     * HTTP status codes are not specifically categorized and will be bucketed
     * into {@link #HTTP_CLIENT_ERROR} or {@link #HTTP_SERVER_ERROR} as
     * appropriate.
     * </p>
     * 
     * @param status
     *            Status Code
     * @return Most appropriate error category
     */
    public static int categorizeHttpError(int status) {
        // Specific categories
        switch (status) {
        case -1:
        case QueryExceptionHTTP.noResponseCode:
            // Unknown HTTP status so categorize as an Execution error
            return EXECUTION;
        case HttpStatus.SC_UNAUTHORIZED:
        case HttpStatus.SC_PAYMENT_REQUIRED:
        case HttpStatus.SC_FORBIDDEN:
        case HttpStatus.SC_PROXY_AUTHENTICATION_REQUIRED:
        case 419: // 419 - Authentication Timeout - Non-standard
        case 440: // 440 - Login Timeout - Non-standard
            // These are all authentication specific errors
            return AUTHENTICATION;
        case HttpStatus.SC_REQUEST_TIMEOUT:
            // Request timed out
            return TIMEOUT;
        case HttpStatus.SC_NOT_FOUND:
        case HttpStatus.SC_GONE:
            // Resource not found
            return HTTP_NOT_FOUND;
        }

        // General categories
        if (status >= 400 && status < 500) {
            return HTTP_CLIENT_ERROR;
        } else if (status >= 500 && status < 600) {
            return HTTP_SERVER_ERROR;
        } else {
            // Assume some other execution error
            return EXECUTION;
        }
    }
}