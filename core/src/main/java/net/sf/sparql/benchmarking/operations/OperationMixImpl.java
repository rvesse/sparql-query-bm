/** 
 * Copyright 2011-2014 Cray Inc. All Rights Reserved
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name Cray Inc. nor the names of its contributors may be
 *   used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 **/
package net.sf.sparql.benchmarking.operations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.sparql.benchmarking.stats.OperationMixStats;
import net.sf.sparql.benchmarking.stats.impl.OperationMixStatsImpl;

/**
 * A basic implementation of an operation mix
 * 
 * @author rvesse
 * 
 */
public class OperationMixImpl implements OperationMix {

    protected static final Logger logger = LoggerFactory.getLogger(OperationMixImpl.class);

    private OperationMixStats stats = new OperationMixStatsImpl();
    private List<Operation> operations = new ArrayList<Operation>();

    /**
     * Creates a new operation mix
     * 
     * @param ops
     *            Operations
     */
    public OperationMixImpl(Collection<Operation> ops) {
        if (ops == null)
            throw new NullPointerException("Operations cannot be null");
        this.operations.addAll(ops);
        if (this.operations.size() == 0)
            throw new IllegalArgumentException("Cannot have an empty operation mix");
        for (int i = 0; i < this.operations.size(); i++) {
            this.operations.get(i).setId(i);
        }
    }

    @Override
    public Iterator<Operation> getOperations() {
        return this.operations.iterator();
    }

    @Override
    public OperationMixStats getStats() {
        return this.stats;
    }

    @Override
    public Operation getOperation(int id) {
        if (id < 0 || id >= this.operations.size())
            throw new IllegalArgumentException("ID must be in range 0-" + this.operations.size() + " but got " + id);
        return this.operations.get(id);
    }

    @Override
    public int size() {
        return this.operations.size();
    }
}