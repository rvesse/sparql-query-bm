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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.atlas.web.auth.ApacheModAuthFormLogin;
import org.apache.jena.atlas.web.auth.FormLogin;
import org.apache.jena.atlas.web.auth.FormsAuthenticator;
import org.apache.jena.atlas.web.auth.HttpAuthenticator;
import org.apache.jena.atlas.web.auth.PreemptiveBasicAuthenticator;
import org.apache.jena.atlas.web.auth.ScopedAuthenticator;

/**
 * Helper class with authentication related utilities
 * 
 * @author rvesse
 * 
 */
public class AuthUtils {

    /**
     * Private constructor prevents direction instantiation
     */
    private AuthUtils() {
    }

    /**
     * Prepares an authenticator that may use simple/form based authentication
     * 
     * @param username
     *            User name
     * @param password
     *            Password
     * @param preemptive
     *            Preemptive auth?
     * @param formUrl
     *            Form login URL
     * @param formUserField
     *            Form user field
     * @param formPwdField
     *            Form password field
     * @param endpoints
     *            Endpoints to configure authentication for
     * @return Authenticator or null if insufficient information to authenticate
     */
    public HttpAuthenticator prepareAuthenticator(String username, String password, boolean preemptive, String formUrl,
            String formUserField, String formPwdField, String... endpoints) {
        if (username != null && password != null) {
            if (formUrl != null) {
                // Configure forms auth
                if (formUserField == null)
                    formUserField = ApacheModAuthFormLogin.USER_FIELD;
                if (formPwdField == null)
                    formPwdField = ApacheModAuthFormLogin.PASSWORD_FIELD;

                FormLogin login = new FormLogin(formUrl, formUserField, formPwdField, username, password.toCharArray());
                try {
                    Map<URI, FormLogin> logins = new HashMap<URI, FormLogin>();
                    for (String endpoint : endpoints) {
                        if (endpoint == null)
                            continue;
                        logins.put(new URI(endpoint), login);
                    }
                    return new FormsAuthenticator(logins);
                } catch (URISyntaxException e) {
                    throw new IllegalArgumentException("Invalid Endpoint URL, unable to configure form based authentication: "
                            + e.getMessage(), e);
                }
            } else {
                // Use standard HTTP authentication
                return prepareAuthentication(username, password, preemptive, endpoints);
            }
        }

        // Insufficient options to configure authentication
        return null;
    }

    /**
     * Prepares an authenticator that may use simple authentication
     * 
     * @param username
     *            User name
     * @param password
     *            Password
     * @param preemptive
     *            Preemptive auth?
     * @param endpoints
     *            Endpoints to configure authentication for
     * @return Authenticator or null if insufficient information to authenticate
     */
    public HttpAuthenticator prepareAuthentication(String username, String password, boolean preemptive, String... endpoints) {
        if (username != null && password != null) {
            try {
                Map<URI, Pair<String, char[]>> logins = new HashMap<URI, Pair<String, char[]>>();
                for (String endpoint : endpoints) {
                    if (endpoint == null)
                        continue;
                    logins.put(new URI(endpoint), Pair.create(username, password.toCharArray()));
                }

                HttpAuthenticator authenticator = new ScopedAuthenticator(logins);
                if (preemptive) {
                    authenticator = new PreemptiveBasicAuthenticator(authenticator);
                }
                return authenticator;
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("Invalid Endpoint URL, unable to configure authentication: " + e.getMessage(),
                        e);
            }
        }
        return null;
    }
}
