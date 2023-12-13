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

package com.antgroup.openspg.reasoner.kggraph.impl;

import com.antgroup.openspg.reasoner.common.graph.vertex.IVertexId;
import java.util.Arrays;

public class KgGraphKey {
  private final IVertexId[] vertexIds;

  public KgGraphKey(IVertexId[] vertexIds) {
    this.vertexIds = vertexIds;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof KgGraphKey)) {
      return false;
    }
    KgGraphKey other = (KgGraphKey) obj;
    return Arrays.equals(this.vertexIds, other.vertexIds);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(vertexIds);
  }
}
