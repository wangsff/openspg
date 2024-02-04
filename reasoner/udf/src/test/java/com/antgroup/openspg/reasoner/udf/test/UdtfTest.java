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

package com.antgroup.openspg.reasoner.udf.test;

import com.antgroup.openspg.reasoner.common.types.KTDouble$;
import com.antgroup.openspg.reasoner.common.types.KTString$;
import com.antgroup.openspg.reasoner.udf.UdfMng;
import com.antgroup.openspg.reasoner.udf.UdfMngFactory;
import com.antgroup.openspg.reasoner.udf.model.BaseUdtf;
import com.antgroup.openspg.reasoner.udf.model.LinkedUdtfResult;
import com.antgroup.openspg.reasoner.udf.model.UdtfMeta;
import com.antgroup.openspg.reasoner.udf.utils.DateUtils;
import com.google.common.collect.Lists;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class UdtfTest {
  @Before
  public void init() {
    DateUtils.timeZone = TimeZone.getTimeZone("Asia/Shanghai");
  }

  @Test
  public void testAlipayId() {
    UdfMng udfMng = UdfMngFactory.getUdfMng();
    UdtfMeta udtfMeta =
        udfMng.getUdtfMeta("expand_linked_alipay_id", Lists.newArrayList(KTString$.MODULE$));

    BaseUdtf tableFunction = udtfMeta.createTableFunction();
    try {
      tableFunction.process(Lists.newArrayList());
      Assert.fail();
    } catch (Exception e) {
      Assert.assertTrue(
          e.getMessage()
              .contains(
                  "expand_linked_alipay_id should have 1 parameters with standard alipay id"));
    }
    tableFunction.process(Lists.newArrayList(""));
  }

  @Test
  public void testGeoUdtf() {
    UdfMng udfMng = UdfMngFactory.getUdfMng();
    UdtfMeta udtfMeta =
        udfMng.getUdtfMeta(
            "geo_buffer_and_convert_2_s2CellId",
            Lists.newArrayList(KTString$.MODULE$, KTDouble$.MODULE$));
    Assert.assertTrue(udtfMeta.getCompatibleNames().isEmpty());

    BaseUdtf tableFunction = udtfMeta.createTableFunction();
    tableFunction.initialize();
    tableFunction.process(
        Lists.newArrayList(
            "MULTIPOINT(116.506619 39.945368,116.509562 39.945402,116.509474 39.943348,116.506648 39.943247)",
            100.0));
    List<List<Object>> rst = tableFunction.getCollector();
    Assert.assertNotNull(rst);
    Assert.assertEquals(4, ((LinkedUdtfResult) rst.get(0).get(0)).getTargetVertexIdList().size());
  }

  @Test
  public void testGetAllUdtf() {
    UdfMng udfMng = UdfMngFactory.getUdfMng();
    List<UdtfMeta> udtfMetaList = udfMng.getAllUdtfMeta();
    Assert.assertTrue(udtfMetaList.size() >= 1);

    Set<String> udfKeySet = new HashSet<>();
    udtfMetaList.forEach(udfMeta -> udfKeySet.add(udfMeta.toString()));
    Assert.assertTrue(
        udfKeySet.contains("concept_edge_expand(KTObject,KTString,KTObject,KTString)->KTObject"));
  }

  @Test
  public void testGeoBufferAndConvert2S2CellId() {
    UdfMng udfMng = UdfMngFactory.getUdfMng();
    UdtfMeta udtfMeta =
        udfMng.getUdtfMeta(
            "geo_buffer_and_convert_2_s2CellId",
            Lists.newArrayList(KTString$.MODULE$, KTDouble$.MODULE$));
    BaseUdtf tableFunction = udtfMeta.createTableFunction();
    try {
      tableFunction.process(Lists.newArrayList("POINT (116.458844 39.918806)"));
      Assert.assertTrue(false);
    } catch (Exception e) {
      Assert.assertTrue(
          e.getMessage().contains("geo_buffer_and_convert_2_s2CellId should have 2 parameters"));
    }
    tableFunction.process(Lists.newArrayList("", 100));
    List<List<Object>> tableResult = tableFunction.getCollector();
    Assert.assertTrue(tableResult.size() == 1);
    Assert.assertTrue(tableResult.get(0).isEmpty());

    try {
      tableFunction.process(Lists.newArrayList("POINT (116.458844 39.918806)", null));
      Assert.assertTrue(false);
    } catch (Exception e) {
      Assert.assertTrue(
          e.getMessage()
              .contains(
                  "geo_buffer_and_convert_2_s2CellId 2nd parameter distance should not empty"));
    }
  }
}
