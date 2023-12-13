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

package com.antgroup.openspg.reasoner.utils;

import com.antgroup.openspg.reasoner.common.graph.edge.IEdge;
import com.antgroup.openspg.reasoner.common.graph.property.IProperty;
import com.antgroup.openspg.reasoner.common.graph.vertex.IVertex;
import com.antgroup.openspg.reasoner.common.graph.vertex.IVertexId;
import com.antgroup.openspg.reasoner.graphstate.generator.AbstractGraphGenerator;
import com.antgroup.openspg.reasoner.lube.catalog.Catalog;
import com.antgroup.openspg.reasoner.lube.catalog.PropertyGraphSchema;
import com.antgroup.openspg.reasoner.lube.catalog.impl.PropertyGraphCatalog;
import com.antgroup.openspg.reasoner.util.Convert2ScalaUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import scala.Tuple2;
import scala.collection.immutable.Set;

public class ObjSerdeTest {

  private String encode;

  @Before
  public void init() throws Exception {
    Catalog catalogSer = createCatalog();
    encode = SimpleObjSerde.ser(catalogSer);
  }

  @Test
  public void decodeSchema() {
    // add mock data to job
    AbstractGraphGenerator graphGenerator =
        new AbstractGraphGenerator() {
          @Override
          public List<IVertex<String, IProperty>> genVertexList() {
            return Lists.newArrayList(
                constructionVertex("张三", "CustFundKG.Account"),
                constructionVertex("李四", "CustFundKG.Account"));
          }

          @Override
          public List<IEdge<String, IProperty>> genEdgeList() {
            return Lists.newArrayList(constructionEdge("张三", "accountFundContact", "李四"));
          }
        };
    Tuple2<List<IVertex<IVertexId, IProperty>>, List<IEdge<IVertexId, IProperty>>> graphData2 =
        graphGenerator.getGraphData();
    String encodeStr = SimpleObjSerde.ser(graphData2);

    Tuple2<List<IVertex<IVertexId, IProperty>>, List<IEdge<IVertexId, IProperty>>> graphData =
        (Tuple2<List<IVertex<IVertexId, IProperty>>, List<IEdge<IVertexId, IProperty>>>)
            SimpleObjSerde.de(encodeStr);
    Assert.assertEquals(graphData._1.size(), 2);
  }

  private Catalog createCatalog() {
    Map<String, Set<String>> schema = new HashMap<>();
    schema.put("User", Convert2ScalaUtil.toScalaImmutableSet(Sets.newHashSet("id", "zhixin")));
    schema.put(
        "creditCardPaymentBindEvent",
        Convert2ScalaUtil.toScalaImmutableSet(
            Sets.newHashSet("id", "cardBank", "accountQuery", "bindSelf", "cardNum")));
    schema.put(
        "creditCardPaymentBindEvent_relateCreditCardPaymentBindEvent_User",
        Convert2ScalaUtil.toScalaImmutableSet(Sets.newHashSet()));
    Catalog catalog = new PropertyGraphCatalog(Convert2ScalaUtil.toScalaImmutableMap(schema));
    catalog.init();
    return catalog;
  }

  @Test
  public void serdeTest() throws Exception {
    Catalog catalogSer = createCatalog();
    Catalog catalogDe = (Catalog) SimpleObjSerde.de(encode);

    PropertyGraphSchema propertyGraphSchemaSer =
        catalogSer.getGraph(Catalog.defaultGraphName()).graphSchema();
    PropertyGraphSchema propertyGraphSchemaDe =
        catalogDe.getGraph(Catalog.defaultGraphName()).graphSchema();

    Assert.assertEquals(
        propertyGraphSchemaSer.nodes().get("User"), propertyGraphSchemaDe.nodes().get("User"));
  }
}
