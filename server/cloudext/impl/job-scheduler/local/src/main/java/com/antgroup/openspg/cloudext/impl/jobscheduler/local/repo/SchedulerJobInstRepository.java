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

package com.antgroup.openspg.cloudext.impl.jobscheduler.local.repo;

import com.antgroup.openspg.cloudext.impl.jobscheduler.local.cmd.SchedulerJobInstQuery;
import com.antgroup.openspg.cloudext.interfaces.jobscheduler.model.SchedulerJobInst;
import com.antgroup.openspg.server.common.model.job.JobInstStatusEnum;
import java.util.List;

public interface SchedulerJobInstRepository {

  String save(SchedulerJobInst jobInst);

  List<SchedulerJobInst> query(SchedulerJobInstQuery query);

  int updateStatus(String jobInstId, JobInstStatusEnum status);
}
