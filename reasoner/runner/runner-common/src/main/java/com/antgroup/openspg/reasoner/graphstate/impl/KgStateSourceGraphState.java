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

/*
 * Ant Group
 * Copyright (c) 2004-2023 All Rights Reserved.
 */
package com.antgroup.openspg.reasoner.graphstate.impl;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import com.antgroup.openspg.reasoner.common.graph.edge.IEdge;
import com.antgroup.openspg.reasoner.common.graph.property.IProperty;
import com.antgroup.openspg.reasoner.common.graph.vertex.IVertex;
import com.antgroup.openspg.reasoner.common.graph.vertex.IVertexId;
import com.antgroup.openspg.reasoner.warehouse.common.AbstractGraphLoader;
import com.antgroup.openspg.reasoner.warehouse.common.VertexSubGraph;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;

/**
 * Graph State based on KgState
 * The multi-version vertex is implemented through the multi-version property
 * The multi-version edge is implemented through multiple edges
 *
 * @author kejian
 * @version MemGraphState.java, v 0.1 2023年02月07日 2:36 PM kejian
 */
@Slf4j(topic = "userlogger")
public class KgStateSourceGraphState extends MemGraphState {
    private AbstractGraphLoader      kgStateGraphQuery;
    @Override
    public void init(Map<String, String> param) {

    }

    // 设置query
    public void setKgStateGraphQuery(AbstractGraphLoader kgStateGraphQuery) {
        this.kgStateGraphQuery = kgStateGraphQuery;
    }

    @Override
    public IVertex<IVertexId, IProperty> getVertex(IVertexId id, Long version) {
        if (vertexMap.containsKey(id)) {
            return super.getVertex(id, version);
        }
        log.info("KgStateSourceGraphState begin query " + id.toString());
        // query from kgstate
        VertexSubGraph vertexSubGraph = kgStateGraphQuery.queryOneHotGraphState(id);
        if (vertexSubGraph == null) {
            log.warn("KgStateSourceGraphState query id " + id.toString() + " not found");
            vertexMap.put(id, null);
            return null;
        }
        addVertex(vertexSubGraph.getVertex());
        addEdges(vertexSubGraph.getVertex().getId(), vertexSubGraph.getInEdges(), vertexSubGraph.getOutEdges());
        return getVertex(id, version);
    }

    @Override
    public Iterator<IVertex<IVertexId, IProperty>> getVertexIterator(Set<String> vertexType) {
        throw new NotImplementedException("not support getVertexIterator in KgStateSourceGraph");
    }

    @Override
    public Iterator<IVertex<IVertexId, IProperty>> getVertexIterator(Predicate<IVertex<IVertexId, IProperty>> filter) {
        throw new NotImplementedException("not support getVertexIterator in KgStateSourceGraph");
    }

    @Override
    public Iterator<IEdge<IVertexId, IProperty>> getEdgeIterator(Set<String> edgeType) {
        throw new NotImplementedException("not support getEdgeIterator in KgStateSourceGraph");
    }

    @Override
    public Iterator<IEdge<IVertexId, IProperty>> getEdgeIterator(Predicate<IEdge<IVertexId, IProperty>> filter) {
        throw new NotImplementedException("not support getEdgeIterator in KgStateSourceGraph");
    }
}