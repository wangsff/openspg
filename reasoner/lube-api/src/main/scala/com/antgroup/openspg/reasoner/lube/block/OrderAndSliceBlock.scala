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

package com.antgroup.openspg.reasoner.lube.block

import com.antgroup.openspg.reasoner.lube.common.expr.Expr
import com.antgroup.openspg.reasoner.lube.common.graph.IRGraph

final case class OrderAndSliceBlock(
    dependencies: List[Block],
    orderBy: Seq[SortItem],
    limit: Option[Int],
    group: List[String])
    extends BasicBlock[Binds](BlockType("order-and-slice")) {
  override def binds: Binds = dependencies.head.binds
}

sealed trait SortItem {
  def expr: Expr
}

final case class Asc(expr: Expr) extends SortItem

final case class Desc(expr: Expr) extends SortItem
