/*
 * Copyright 2023 Ant Group CO., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied.
 */
package com.antgroup.openspg.reasoner.graphstate;


import java.util.HashSet;

import com.antgroup.openspg.reasoner.common.graph.property.IProperty;
import com.antgroup.openspg.reasoner.common.graph.vertex.IVertex;
import com.antgroup.openspg.reasoner.common.graph.vertex.IVertexId;
import com.antgroup.openspg.reasoner.graphstate.impl.KgStateSourceGraphState;
import com.antgroup.openspg.reasoner.warehouse.common.AbstractGraphLoader;
import org.junit.Assert;
import org.junit.Test;


public class KgstateGraphStateTest {
    @Test
    public void testReadVertex() {
        KgStateSourceGraphState kgStateSourceGraphState = new KgStateSourceGraphState();
        AbstractGraphLoader abstractGraphLoader = new MockGraphLoader(null);
        kgStateSourceGraphState.setKgStateGraphQuery(abstractGraphLoader);
        IVertex<IVertexId, IProperty> v = kgStateSourceGraphState.getVertex(IVertexId.from("abc", "Test"), null);
        Assert.assertTrue(v != null);

        IVertex<IVertexId, IProperty> v1 = kgStateSourceGraphState.getVertex(IVertexId.from("abc2", "Test"), null);
        Assert.assertTrue(v1 == null);
    }

    @Test
    public void testException() {
        KgStateSourceGraphState kgStateSourceGraphState = new KgStateSourceGraphState();

        try {
            kgStateSourceGraphState.getVertexIterator(new HashSet<>());
            Assert.fail();
        } catch (Exception e) {
            Assert.assertTrue(true);
        }

        try {
            kgStateSourceGraphState.getVertexIterator(vertex1 -> vertex1.getId().equals("abc"));
            Assert.fail();
        } catch (Exception e) {
            Assert.assertTrue(true);
        }

        try {
            kgStateSourceGraphState.getEdgeIterator(new HashSet<>());
            Assert.fail();
        } catch (Exception e) {
            Assert.assertTrue(true);
        }

        try {
            kgStateSourceGraphState.getEdgeIterator(edge1 -> edge1.getSourceId().equals("abc"));
            Assert.fail();
        } catch (Exception e) {
            Assert.assertTrue(true);
        }
    }
}