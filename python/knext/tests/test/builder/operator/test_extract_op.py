# -*- coding: utf-8 -*-
# Copyright 2023 OpenSPG Authors
#
# Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
# in compliance with the License. You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software distributed under the License
# is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
# or implied.

from typing import List, Dict

from knext.builder.operator.spg_record import SPGRecord
from knext.builder.operator import ExtractOp


class TestExtractOp(ExtractOp):
    def __init__(self, params: Dict[str, str] = None):
        super().__init__(params)

    def invoke(self, record: Dict[str, str]) -> List[Dict[str, str]]:

        center_event = SPGRecord(spg_type_name="TEST.CenterEvent",).upsert_properties(
            {
                "id": "TestEvent1",
                "name": "TestEvent1",
                "text": "text1",
                "integer": "123",
                "float": "4.56",
                "event": "TestEvent2",
                "entity": "TestEntity1",
                "standard": "20240101",
                "concept": "TestConcept1",
            }
        )

        event = SPGRecord(spg_type_name="TEST.CenterEvent",).upsert_properties(
            properties={
                "id": "TestEvent2",
                "name": "TestEvent2",
                "text": "text2",
                "integer": "234",
                "float": "5.67",
            }
        )

        entity = SPGRecord(spg_type_name="TEST.Entity1",).upsert_properties(
            properties={
                "id": "TestEntity1",
                "name": "TestEntity1",
                "entity": "TestEntity2",
            }
        )

        concept1 = (
            SPGRecord(
                spg_type_name="TEST.Concept1",
            )
            .upsert_properties(
                {
                    "id": "TestConcept1",
                    "name": "TestConcept1",
                }
            )
            .upsert_relation("leadTo", "TEST.Concept2", "TestConcept2")
        )

        concept2 = SPGRecord(spg_type_name="TEST.Concept2",).upsert_properties(
            properties={
                "id": "TestConcept2",
                "name": "TestConcept2",
            }
        )

        return [event, center_event, entity, concept1, concept2]
