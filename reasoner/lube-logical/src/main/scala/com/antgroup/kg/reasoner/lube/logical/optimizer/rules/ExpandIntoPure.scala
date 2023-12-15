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

package com.antgroup.openspg.reasoner.lube.logical.optimizer.rules

import com.antgroup.openspg.reasoner.lube.common.pattern.NodePattern
import com.antgroup.openspg.reasoner.lube.logical.operators.{ExpandInto, LogicalOperator}
import com.antgroup.openspg.reasoner.lube.logical.optimizer.{Direction, Rule, Up}
import com.antgroup.openspg.reasoner.lube.logical.planning.LogicalPlannerContext

/**
 * Prue useless expandInto
 */
object ExpandIntoPure extends Rule {

  override def rule(implicit
      context: LogicalPlannerContext): PartialFunction[LogicalOperator, LogicalOperator] = {
    case expandInto @ ExpandInto(in, target, _) =>
      val needPure = canPure(expandInto)
      if (needPure) {
        in
      } else {
        expandInto
      }
  }

  private def canPure(expandInto: ExpandInto): Boolean = {
    if (!expandInto.pattern.isInstanceOf[NodePattern]) {
      false
    } else {
      val refFields = expandInto.refFields
      if (refFields.head.isEmpty) {
        true
      } else {
        false
      }
    }
  }

  override def direction: Direction = Up

  override def maxIterations: Int = 1
}
