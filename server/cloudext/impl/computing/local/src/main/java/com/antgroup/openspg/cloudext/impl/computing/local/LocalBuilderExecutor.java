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

package com.antgroup.openspg.cloudext.impl.computing.local;

import com.antgroup.openspg.cloudext.interfaces.computing.cmd.BuilderJobCanSubmitQuery;
import com.antgroup.openspg.cloudext.interfaces.computing.cmd.BuilderJobProcessQuery;
import com.antgroup.openspg.cloudext.interfaces.computing.cmd.BuilderJobSubmitCmd;
import com.antgroup.openspg.server.core.builder.model.service.BuilderStatusWithProgress;

public interface LocalBuilderExecutor {

  BuilderStatusWithProgress query(BuilderJobProcessQuery query);

  boolean canSubmit(BuilderJobCanSubmitQuery query);

  String submit(BuilderJobSubmitCmd cmd);
}
