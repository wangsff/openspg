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

package com.antgroup.openspg.server.api.facade;

import com.antgroup.openspg.builder.model.pipeline.NodeTypeEnum;
import com.antgroup.openspg.builder.model.pipeline.config.*;
import com.antgroup.openspg.core.schema.model.constraint.BaseConstraintItem;
import com.antgroup.openspg.core.schema.model.constraint.ConstraintTypeEnum;
import com.antgroup.openspg.core.schema.model.constraint.EnumConstraint;
import com.antgroup.openspg.core.schema.model.constraint.MultiValConstraint;
import com.antgroup.openspg.core.schema.model.constraint.NotNullConstraint;
import com.antgroup.openspg.core.schema.model.constraint.RangeConstraint;
import com.antgroup.openspg.core.schema.model.constraint.RegularConstraint;
import com.antgroup.openspg.core.schema.model.constraint.UniqueConstraint;
import com.antgroup.openspg.core.schema.model.identifier.BaseSPGIdentifier;
import com.antgroup.openspg.core.schema.model.identifier.ConceptIdentifier;
import com.antgroup.openspg.core.schema.model.identifier.OperatorIdentifier;
import com.antgroup.openspg.core.schema.model.identifier.PredicateIdentifier;
import com.antgroup.openspg.core.schema.model.identifier.SPGIdentifierTypeEnum;
import com.antgroup.openspg.core.schema.model.identifier.SPGTripleIdentifier;
import com.antgroup.openspg.core.schema.model.identifier.SPGTypeIdentifier;
import com.antgroup.openspg.core.schema.model.semantic.BaseConceptSemantic;
import com.antgroup.openspg.core.schema.model.semantic.DynamicTaxonomySemantic;
import com.antgroup.openspg.core.schema.model.semantic.LogicalCausationSemantic;
import com.antgroup.openspg.core.schema.model.type.BaseSPGType;
import com.antgroup.openspg.core.schema.model.type.BasicType.DoubleBasicType;
import com.antgroup.openspg.core.schema.model.type.BasicType.LongBasicType;
import com.antgroup.openspg.core.schema.model.type.BasicType.TextBasicType;
import com.antgroup.openspg.core.schema.model.type.BasicTypeEnum;
import com.antgroup.openspg.core.schema.model.type.ConceptType;
import com.antgroup.openspg.core.schema.model.type.EntityType;
import com.antgroup.openspg.core.schema.model.type.EventType;
import com.antgroup.openspg.core.schema.model.type.SPGTypeEnum;
import com.antgroup.openspg.core.schema.model.type.StandardType;
import com.antgroup.openspg.server.common.model.job.JobInstStatusEnum;
import com.antgroup.openspg.server.core.builder.model.service.BaseBuilderResult;
import com.antgroup.openspg.server.core.builder.model.service.FailureBuilderResult;
import com.antgroup.openspg.server.core.builder.model.service.SuccessBuilderResult;
import com.antgroup.openspg.server.core.reasoner.model.service.BaseReasonerContent;
import com.antgroup.openspg.server.core.reasoner.model.service.BaseReasonerReceipt;
import com.antgroup.openspg.server.core.reasoner.model.service.BaseReasonerResult;
import com.antgroup.openspg.server.core.reasoner.model.service.FailureReasonerResult;
import com.antgroup.openspg.server.core.reasoner.model.service.JobReasonerReceipt;
import com.antgroup.openspg.server.core.reasoner.model.service.KgdslReasonerContent;
import com.antgroup.openspg.server.core.reasoner.model.service.ReasonerContentTypeEnum;
import com.antgroup.openspg.server.core.reasoner.model.service.ReasonerReceiptTypeEnum;
import com.antgroup.openspg.server.core.reasoner.model.service.SuccessReasonerResult;
import com.antgroup.openspg.server.core.reasoner.model.service.TableReasonerReceipt;
import com.antgroup.openspg.server.core.reasoner.model.service.VertexReasonerContent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.ToNumberPolicy;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import java.lang.reflect.Type;

public class JSON {

  public static final String DATA_FORMAT = "yyyy-MM-dd HH:mm:ss";
  public static final String DEFAULT_TYPE_FIELD_NAME = "@type";

  public static Gson gson = null;

  static {
    gson =
        new GsonBuilder()
            // BaseSPGType
            .registerTypeAdapterFactory(
                RuntimeTypeAdapterFactory.of(BaseSPGType.class, DEFAULT_TYPE_FIELD_NAME)
                    .registerSubtype(EntityType.class, SPGTypeEnum.ENTITY_TYPE.name())
                    .registerSubtype(ConceptType.class, SPGTypeEnum.CONCEPT_TYPE.name())
                    .registerSubtype(EventType.class, SPGTypeEnum.EVENT_TYPE.name())
                    .registerSubtype(StandardType.class, SPGTypeEnum.STANDARD_TYPE.name())
                    .registerSubtype(TextBasicType.class, BasicTypeEnum.TEXT.name())
                    .registerSubtype(LongBasicType.class, BasicTypeEnum.LONG.name())
                    .registerSubtype(DoubleBasicType.class, BasicTypeEnum.DOUBLE.name())
                    .recognizeSubtypes())
            // BaseConstraintItem
            .registerTypeAdapterFactory(
                RuntimeTypeAdapterFactory.of(BaseConstraintItem.class, DEFAULT_TYPE_FIELD_NAME)
                    .registerSubtype(EnumConstraint.class, ConstraintTypeEnum.ENUM.name())
                    .registerSubtype(
                        MultiValConstraint.class, ConstraintTypeEnum.MULTI_VALUE.name())
                    .registerSubtype(NotNullConstraint.class, ConstraintTypeEnum.NOT_NULL.name())
                    .registerSubtype(RangeConstraint.class, ConstraintTypeEnum.RANGE.name())
                    .registerSubtype(RegularConstraint.class, ConstraintTypeEnum.REGULAR.name())
                    .registerSubtype(UniqueConstraint.class, ConstraintTypeEnum.UNIQUE.name())
                    .recognizeSubtypes())
            // BaseSPGName
            .registerTypeAdapterFactory(
                RuntimeTypeAdapterFactory.of(BaseSPGIdentifier.class, DEFAULT_TYPE_FIELD_NAME)
                    .registerSubtype(SPGTypeIdentifier.class, SPGIdentifierTypeEnum.SPG_TYPE.name())
                    .registerSubtype(ConceptIdentifier.class, SPGIdentifierTypeEnum.CONCEPT.name())
                    .registerSubtype(
                        SPGTripleIdentifier.class, SPGIdentifierTypeEnum.SPG_TRIPLE.name())
                    .registerSubtype(
                        PredicateIdentifier.class, SPGIdentifierTypeEnum.PREDICATE.name())
                    .registerSubtype(
                        OperatorIdentifier.class, SPGIdentifierTypeEnum.OPERATOR.name())
                    .recognizeSubtypes())
            .registerTypeAdapterFactory(
                RuntimeTypeAdapterFactory.of(BaseNodeConfig.class, DEFAULT_TYPE_FIELD_NAME)
                    .registerSubtype(CsvSourceNodeConfig.class, NodeTypeEnum.CSV_SOURCE.name())
                    .registerSubtype(ExtractNodeConfig.class, NodeTypeEnum.EXTRACT.name())
                    .registerSubtype(MappingNodeConfig.class, NodeTypeEnum.MAPPING.name())
                    .registerSubtype(GraphStoreSinkNodeConfig.class, NodeTypeEnum.GRAPH_SINK.name())
                    .recognizeSubtypes())
            .registerTypeAdapterFactory(
                RuntimeTypeAdapterFactory.of(BaseBuilderResult.class, DEFAULT_TYPE_FIELD_NAME)
                    .registerSubtype(FailureBuilderResult.class, JobInstStatusEnum.FAILURE.name())
                    .registerSubtype(SuccessBuilderResult.class, JobInstStatusEnum.SUCCESS.name())
                    .recognizeSubtypes())
            .registerTypeAdapterFactory(
                RuntimeTypeAdapterFactory.of(BaseReasonerResult.class, DEFAULT_TYPE_FIELD_NAME)
                    .registerSubtype(FailureReasonerResult.class, JobInstStatusEnum.FAILURE.name())
                    .registerSubtype(SuccessReasonerResult.class, JobInstStatusEnum.SUCCESS.name())
                    .recognizeSubtypes())
            // BaseConceptSemantic
            .registerTypeAdapterFactory(
                RuntimeTypeAdapterFactory.of(BaseConceptSemantic.class, DEFAULT_TYPE_FIELD_NAME)
                    .registerSubtype(DynamicTaxonomySemantic.class)
                    .registerSubtype(LogicalCausationSemantic.class)
                    .recognizeSubtypes())
            // BaseReasonerContent
            .registerTypeAdapterFactory(
                RuntimeTypeAdapterFactory.of(BaseReasonerContent.class, DEFAULT_TYPE_FIELD_NAME)
                    .registerSubtype(
                        KgdslReasonerContent.class, ReasonerContentTypeEnum.KGDSL.name())
                    .registerSubtype(
                        VertexReasonerContent.class, ReasonerContentTypeEnum.VERTEX.name())
                    .recognizeSubtypes())
            // BaseReasonerReceipt
            .registerTypeAdapterFactory(
                RuntimeTypeAdapterFactory.of(BaseReasonerReceipt.class, DEFAULT_TYPE_FIELD_NAME)
                    .registerSubtype(
                        TableReasonerReceipt.class, ReasonerReceiptTypeEnum.TABLE.name())
                    .registerSubtype(JobReasonerReceipt.class, ReasonerReceiptTypeEnum.JOB.name())
                    .recognizeSubtypes())
            .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
            .setDateFormat(DATA_FORMAT)
            .create();
  }

  /**
   * Serialize the given Java object into JSON string.
   *
   * @param obj Object
   * @return String representation of the JSON
   */
  public static String serialize(Object obj) {
    return gson.toJson(obj);
  }

  /**
   * Deserialize the given JSON string to Java object.
   *
   * @param <T> Type
   * @param body The JSON string
   * @param type The class to deserialize into
   * @return The deserialized Java object
   */
  public static <T> T deserialize(String body, Type type) {
    return gson.fromJson(body, type);
  }

  /**
   * Deserialize the given JSON string to Java object.
   *
   * @param <T> Type
   * @param body The JSON string
   * @param clazz The class to deserialize into
   * @return The deserialized Java object
   */
  public static <T> T deserialize(String body, Class<T> clazz) {
    return gson.fromJson(body, clazz);
  }
}
