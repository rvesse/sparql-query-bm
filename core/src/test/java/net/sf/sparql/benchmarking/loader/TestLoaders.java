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

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import net.sf.sparql.benchmarking.loader.mix.ClassicQueryMixLoader;
import net.sf.sparql.benchmarking.loader.mix.TsvMixLoader;
import net.sf.sparql.benchmarking.loader.query.FixedQueryOperationLoader;
import net.sf.sparql.benchmarking.loader.update.FixedUpdateOperationLoader;
import net.sf.sparql.benchmarking.operations.Operation;
import net.sf.sparql.benchmarking.operations.OperationMix;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests for {@link OperationLoader} and {@link OperationMixLoader}
 * 
 * @author rvesse
 * 
 */
@SuppressWarnings("javadoc")
public class TestLoaders {

    @BeforeClass
    public static void setup() {
        OperationMixLoaderRegistry.resetLoaders();
        OperationLoaderRegistry.resetLoaders();
    }

    @Test
    public void registered_mix_loaders_01() {
        OperationMixLoader mixLoader = OperationMixLoaderRegistry.getLoader("txt");
        Assert.assertNotNull(mixLoader);
        Assert.assertTrue(mixLoader instanceof ClassicQueryMixLoader);
    }

    @Test
    public void registered_mix_loaders_02() {
        OperationMixLoader mixLoader = OperationMixLoaderRegistry.getLoader("tsv");
        Assert.assertNotNull(mixLoader);
        Assert.assertTrue(mixLoader instanceof TsvMixLoader);
    }

    @Test
    public void registered_op_loaders_01() {
        OperationLoader opLoader = OperationLoaderRegistry.getLoader("query");
        Assert.assertNotNull(opLoader);
        Assert.assertTrue(opLoader instanceof FixedQueryOperationLoader);
    }

    @Test
    public void registered_op_loaders_02() {
        OperationLoader opLoader = OperationLoaderRegistry.getLoader("update");
        Assert.assertNotNull(opLoader);
        Assert.assertTrue(opLoader instanceof FixedUpdateOperationLoader);
    }

    @Test
    public void tsv_mix_loader_01() throws IOException {
        OperationMix mix = OperationMixLoaderRegistry.getLoader("tsv").load(new File("queries/lubm.tsv"));
        Assert.assertEquals(14, mix.size());
        Iterator<Operation> ops = mix.getOperations();
        while (ops.hasNext()) {
            Assert.assertFalse(ops.next().getName().contains(".txt"));
        }
    }
}
