/*
 * Copyright 2023 OpenSPG Authors
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

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

import com.antgroup.openspg.reasoner.common.constants.Constants
import com.antgroup.openspg.reasoner.common.exception.UnsupportedOperationException
import com.antgroup.openspg.reasoner.common.graph.edge
import com.antgroup.openspg.reasoner.common.types.KTString
import com.antgroup.openspg.reasoner.lube.block.{AddPredicate, DDLOp}
import com.antgroup.openspg.reasoner.lube.catalog.struct.Field
import com.antgroup.openspg.reasoner.lube.common.expr.Expr
import com.antgroup.openspg.reasoner.lube.common.graph.{IRField, IRNode, IRProperty, IRVariable}
import com.antgroup.openspg.reasoner.lube.common.pattern.{Connection, NodePattern, Pattern, VariablePatternConnection}
import com.antgroup.openspg.reasoner.lube.logical.{NodeVar, PropertyVar, Var}
import com.antgroup.openspg.reasoner.lube.logical.operators._
import com.antgroup.openspg.reasoner.lube.logical.optimizer.{Direction, Rule, Up}
import com.antgroup.openspg.reasoner.lube.logical.planning.LogicalPlannerContext
import com.antgroup.openspg.reasoner.lube.utils.{ExprUtils, RuleUtils}

object NodeIdToEdgeProperty extends Rule {
  private val NODE_DEFAULT_PROPS = Set.apply(Constants.NODE_ID_KEY, Constants.CONTEXT_LABEL)

  def ruleWithContext(implicit context: LogicalPlannerContext): PartialFunction[
    (LogicalOperator, Map[String, Object]),
    (LogicalOperator, Map[String, Object])] = {
    case (expandInto: ExpandInto, map) =>
      if (!canPushDown(expandInto)) {
        expandInto -> map
      } else {
        val toEdge = targetConnection(expandInto)
        if (toEdge != null) {
          expandInto -> (map + (expandInto.pattern.root.alias -> toEdge))
        } else {
          expandInto -> map
        }
      }
    case (filter: Filter, map) =>
      if (map.isEmpty) {
        filter -> map
      } else {
        filterUpdate(filter, map) -> map
      }
    case (select: Select, map) =>
      if (map.isEmpty) {
        select -> map
      } else {
        selectUpdate(select, map) -> map
      }
    case (ddl: DDL, map) =>
      if (map.isEmpty) {
        ddl -> map
      } else {
        ddlUpdate(ddl, map) -> map
      }
    case (varLenExpand @ BoundedVarLenExpand(_, expandInto: ExpandInto, edgePattern, _), map) =>
      if (edgePattern.edge.upper == 1 && canPushDown(expandInto)) {
        val toEdge = targetConnection(expandInto)
        varLenExpand -> (map + (expandInto.pattern.root.alias -> toEdge))
      } else if (canPushDown(expandInto)) {
        varLenExpand -> (map - expandInto.pattern.root.alias)
      } else {
        varLenExpand -> map
      }
  }

  private def genField(direction: edge.Direction, fieldName: String): String = {
    (direction, fieldName) match {
      case (edge.Direction.OUT, Constants.NODE_ID_KEY) => Constants.EDGE_TO_ID_KEY
      case (edge.Direction.OUT, Constants.CONTEXT_LABEL) => Constants.EDGE_TO_ID_TYPE_KEY
      case (edge.Direction.IN, Constants.NODE_ID_KEY) => Constants.EDGE_FROM_ID_KEY
      case (edge.Direction.IN, Constants.CONTEXT_LABEL) => Constants.EDGE_FROM_ID_TYPE_KEY
      case (_, _) =>
        throw UnsupportedOperationException(s"""unsupport (${direction}, ${fieldName})""")
    }
  }

  private def filterUpdate(filter: Filter, map: Map[String, Object]): Filter = {
    val input = RuleUtils.getAllInputFieldInRule(
      filter.rule,
      filter.solved.getNodeAliasSet,
      filter.solved.getEdgeAliasSet)
    val replaceVar = new mutable.HashMap[IRField, IRField]
    for (irField <- input) {
      if (irField.isInstanceOf[IRNode] && map.contains(irField.name)) {
        for (propName <- irField.asInstanceOf[IRNode].fields) {
          if (NODE_DEFAULT_PROPS.contains(propName)) {
            val edgeInfo = map(irField.name).asInstanceOf[Connection]
            replaceVar.put(
              IRProperty(irField.name, propName),
              IRProperty(edgeInfo.alias, genField(edgeInfo.direction, propName)))
            replaceVar.put(IRVariable(irField.name), IRVariable(edgeInfo.alias))
          }
        }
      }
    }
    if (replaceVar.isEmpty) {
      filter
    } else {
      val newRule = RuleUtils.renameVariableInRule(filter.rule, replaceVar.toMap)
      filter.copy(rule = newRule)
    }
  }

  private def selectUpdate(select: Select, map: Map[String, Object]): Select = {
    val newFields = new ListBuffer[Var]()
    for (field <- select.fields) {
      if (field.isInstanceOf[PropertyVar] && map.contains(field.name)) {
        val edgeInfo = map(field.name).asInstanceOf[Connection]
        val propName = field.asInstanceOf[PropertyVar].field.name
        if (NODE_DEFAULT_PROPS.contains(propName)) {
          newFields.append(
            PropertyVar(
              edgeInfo.alias,
              new Field(genField(edgeInfo.direction, propName), KTString, true)))
        } else {
          newFields.append(field)
        }
      } else {
        newFields.append(field)
      }
    }
    select.copy(fields = newFields.toList)
  }

  private def ddlUpdate(ddl: DDL, map: Map[String, Object]): DDL = {
    val ddlOps = new mutable.HashSet[DDLOp]()
    for (ddlOp <- ddl.ddlOp) {
      ddlOp match {
        case AddPredicate(predicate) =>
          val newFields = new mutable.HashMap[String, Expr]()
          for (field <- predicate.fields) {
            val input = ExprUtils.getAllInputFieldInRule(field._2, null, null)
            val replaceVar = new mutable.HashMap[IRField, IRProperty]
            for (irField <- input) {
              if (irField.isInstanceOf[IRNode] && map.contains(irField.name)) {
                for (propName <- irField.asInstanceOf[IRNode].fields) {
                  if (NODE_DEFAULT_PROPS.contains(propName)) {
                    val edgeInfo = map(irField.name).asInstanceOf[Connection]
                    replaceVar.put(
                      IRProperty(irField.name, propName),
                      IRProperty(edgeInfo.alias, genField(edgeInfo.direction, propName)))
                  }
                }
              }
            }
            if (replaceVar.isEmpty) {
              newFields.put(field._1, field._2)
            } else {
              newFields.put(field._1, ExprUtils.renameVariableInExpr(field._2, replaceVar.toMap))
            }
          }
          ddlOps.add(AddPredicate(predicate.copy(fields = newFields.toMap)))
        case _ => ddlOps.add(ddlOp)
      }
    }
    ddl.copy(ddlOp = ddlOps.toSet)
  }

  private def targetConnection(expandInto: ExpandInto): Connection = {
    val alias = expandInto.pattern.root.alias
    val edgeAlias = expandInto.transform[Connection] {
      case (patternScan: PatternScan, _) =>
        targetConnection(alias, patternScan.pattern)
      case (expandInto: ExpandInto, list) =>
        if (!list.isEmpty && list.head != null) {
          list.head
        } else {
          targetConnection(alias, expandInto.pattern)
        }
      case (_, list) =>
        if (list.isEmpty) {
          null
        } else {
          list.head
        }
    }
    edgeAlias
  }

  private def targetConnection(alias: String, pattern: Pattern): Connection = {
    val connections = pattern.topology.values.flatten.filter(_.target.equals(alias))
    val fixedEdges = connections.filter(!_.isInstanceOf[VariablePatternConnection])
    val varEdges = connections
      .filter(_.isInstanceOf[VariablePatternConnection])
      .map(_.asInstanceOf[VariablePatternConnection])
      .filter(_.upper == 1)
    if (fixedEdges.isEmpty && varEdges.isEmpty) {
      null
    } else if (fixedEdges.isEmpty) {
      varEdges.head
    } else {
      fixedEdges.head
    }
  }

  private def canPushDown(expandInto: ExpandInto): Boolean = {
    if (!expandInto.pattern.isInstanceOf[NodePattern]) {
      false
    } else {
      val fieldNames = expandInto.refFields.head.asInstanceOf[NodeVar].fields.map(_.name)
      val normalNames = fieldNames.filter(!NODE_DEFAULT_PROPS.contains(_))
      if (normalNames.isEmpty) {
        true
      } else {
        false
      }
    }
  }

  override def direction: Direction = Up

  override def maxIterations: Int = 1
}
