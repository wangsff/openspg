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

package com.antgroup.openspg.reasoner.rdg.common;

import com.antgroup.openspg.reasoner.common.exception.IllegalArgumentException;
import java.io.Serializable;
import org.apache.commons.lang3.StringUtils;

public class FoldRepeatEdgeInfo implements Serializable {
  private final String fromEdgeAlias;
  private final String toEdgeAlias;
  private final String fromVertexAlias;
  private final String toVertexAlias;

  public FoldRepeatEdgeInfo(
      String fromEdgeAlias, String toEdgeAlias, String fromVertexAlias, String toVertexAlias) {
    if (StringUtils.isEmpty(fromEdgeAlias)
        || StringUtils.isEmpty(toEdgeAlias)
        || StringUtils.isEmpty(fromVertexAlias)
        || StringUtils.isEmpty(toEdgeAlias)) {
      throw new IllegalArgumentException("no empty string", "", "", null);
    }
    this.fromEdgeAlias = fromEdgeAlias;
    this.toEdgeAlias = toEdgeAlias;
    this.fromVertexAlias = fromVertexAlias;
    this.toVertexAlias = toVertexAlias;
  }

  /**
   * Getter method for property <tt>fromEdgeAlias</tt>.
   *
   * @return property value of fromEdgeAlias
   */
  public String getFromEdgeAlias() {
    return fromEdgeAlias;
  }

  /**
   * Getter method for property <tt>toEdgeAlias</tt>.
   *
   * @return property value of toEdgeAlias
   */
  public String getToEdgeAlias() {
    return toEdgeAlias;
  }

  /**
   * Getter method for property <tt>fromVertexAlias</tt>.
   *
   * @return property value of fromVertexAlias
   */
  public String getFromVertexAlias() {
    return fromVertexAlias;
  }

  /**
   * Getter method for property <tt>toVertexAlias</tt>.
   *
   * @return property value of toVertexAlias
   */
  public String getToVertexAlias() {
    return toVertexAlias;
  }
}
