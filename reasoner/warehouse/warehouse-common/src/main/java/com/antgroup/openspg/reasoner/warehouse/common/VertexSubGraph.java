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
package com.antgroup.openspg.reasoner.warehouse.common;

import com.antgroup.openspg.reasoner.common.graph.edge.IEdge;
import com.antgroup.openspg.reasoner.common.graph.edge.impl.Edge;
import com.antgroup.openspg.reasoner.common.graph.property.IProperty;
import com.antgroup.openspg.reasoner.common.graph.vertex.IVertex;
import com.antgroup.openspg.reasoner.common.graph.vertex.IVertexId;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;

@Getter
public class VertexSubGraph implements Serializable {
  /** center vertex in vertex subgraph */
  private final IVertex<IVertexId, IProperty> vertex;

  /** in edges for center vertex */
  private final List<IEdge<IVertexId, IProperty>> inEdges;

  private final Map<String, Long> inEdgeCntMap;

  /** out edges for center vertex */
  private final List<IEdge<IVertexId, IProperty>> outEdges;

  private final Map<String, Long> outEdgeCntMap;

  public VertexSubGraph(IVertex<IVertexId, IProperty> vertex) {
    this.vertex = vertex;
    this.inEdges = new ArrayList<>();
    this.inEdgeCntMap = new HashMap<>();
    this.outEdges = new ArrayList<>();
    this.outEdgeCntMap = new HashMap<>();
  }

  public void addInEdge(IEdge<IVertexId, IProperty> inEdge) {
    if (inEdge == null) {
      return;
    }
    inEdges.add(inEdge);

    Long edgeCnt = inEdgeCntMap.getOrDefault(inEdge.getType(), 0L);
    inEdgeCntMap.put(inEdge.getType(), edgeCnt + 1);
  }

  public void addInEdge(List<Edge<IVertexId, IProperty>> inEdgeList) {
    if (CollectionUtils.isEmpty(inEdgeList)) {
      return;
    }
    for (IEdge<IVertexId, IProperty> inEdge : inEdgeList) {
      addInEdge(inEdge);
    }
  }

  public void addOutEdge(IEdge<IVertexId, IProperty> outEdge) {
    if (outEdge == null) {
      return;
    }
    outEdges.add(outEdge);

    Long edgeCnt = outEdgeCntMap.getOrDefault(outEdge.getType(), 0L);
    outEdgeCntMap.put(outEdge.getType(), edgeCnt + 1);
  }

  public void addOutEdge(List<Edge<IVertexId, IProperty>> outEdgeList) {
    if (CollectionUtils.isEmpty(outEdgeList)) {
      return;
    }
    for (IEdge<IVertexId, IProperty> outEdge : outEdgeList) {
      addOutEdge(outEdge);
    }
  }

  public boolean vertexEquals(IVertexId vertexId) {
    return vertex.getId().equals(vertexId);
  }

  public Long getInEdgeCnt(String edgeType) {
    return inEdgeCntMap.getOrDefault(edgeType, 0L);
  }

  public Long getOutEdgeCnt(String edgeType) {
    return outEdgeCntMap.getOrDefault(edgeType, 0L);
  }

  public Long getTotalEdgeCnt() {
    final long[] count = {0};
    inEdgeCntMap.values().forEach(v -> count[0] += v);
    outEdgeCntMap.values().forEach(v -> count[0] += v);
    return count[0];
  }

  @Override
  public String toString() {
    return "vertexId=" + vertex.getId();
  }
}
