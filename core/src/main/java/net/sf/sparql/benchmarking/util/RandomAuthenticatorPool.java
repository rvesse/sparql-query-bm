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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.protocol.HttpContext;
import org.apache.jena.atlas.web.auth.HttpAuthenticator;

/**
 * An authenticator which selects from a pool of authenticators at random for
 * each request.
 * <p>
 * This can be useful when you want to perform testing that covers the use of
 * multiple authentication methods or different sets of credentials.
 * </p>
 * 
 * @author rvesse
 * 
 */
public class RandomAuthenticatorPool implements HttpAuthenticator {

    private List<HttpAuthenticator> authenticators = new ArrayList<HttpAuthenticator>();
    private Random rand = new Random();

    /**
     * Creates a new authenticator
     * 
     * @param authenticators
     *            Pool of authenticators where each represents a possible
     *            authentication method and/or set of credentials
     */
    public RandomAuthenticatorPool(Collection<HttpAuthenticator> authenticators) {
        this.authenticators.addAll(authenticators);
    }

    @Override
    public void apply(AbstractHttpClient client, HttpContext httpContext, URI target) {
        // Pick and apply an authenticator at random
        int id = this.rand.nextInt(this.authenticators.size());

        HttpAuthenticator authenticator = this.authenticators.get(id);
        if (authenticator == null)
            return;
        authenticator.apply(client, httpContext, target);
    }

}
