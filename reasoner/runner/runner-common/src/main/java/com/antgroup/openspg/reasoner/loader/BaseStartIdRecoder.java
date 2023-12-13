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

package com.antgroup.openspg.reasoner.loader;

import com.antgroup.openspg.reasoner.common.graph.vertex.IVertexId;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class BaseStartIdRecoder implements Iterator<IVertexId> {
  private static final Map<String, BaseStartIdRecoder> START_ID_MAP = new ConcurrentHashMap<>();

  /** base recoder */
  public static BaseStartIdRecoder get(String contextId, int index, boolean init, boolean mem) {
    if (init) {
      BaseStartIdRecoder recoder;
      if (mem) {
        recoder = new MemStartIdRecoder();
      } else {
        recoder = new DiskStartIdRecorder(getKey(contextId, index));
      }
      START_ID_MAP.put(getKey(contextId, index), recoder);
    }
    return START_ID_MAP.get(getKey(contextId, index));
  }

  /** get start id recoder */
  public static BaseStartIdRecoder get(String contextId, int index) {
    return START_ID_MAP.get(getKey(contextId, index));
  }

  /** remove recoder */
  public static void remove(String contextId, int index) {
    START_ID_MAP.remove(getKey(contextId, index));
  }

  private static String getKey(String contextId, int index) {
    return contextId + index;
  }

  public abstract void addStartId(IVertexId id);

  public abstract void flush();

  public abstract long getStartIdCount();
}
