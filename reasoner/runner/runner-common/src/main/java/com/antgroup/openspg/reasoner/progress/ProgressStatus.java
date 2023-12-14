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

package com.antgroup.openspg.reasoner.progress;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/*
 * @Author peilong.zpl
 * @Description ProgressStatus.java //TODO
 * @Date 2021/2/18
 */
@Slf4j(topic = "userlogger")
public class ProgressStatus implements Serializable {
  @Getter @Setter private JobStatus status;
  @Getter @Setter private ProgressInfo progressInfo;
  @Getter private String errMsg;
  private final String instanceId;
  private final String taskKey;
  private final String persistenceWay;
  @Getter private final Map<TimeConsumeType, Long> timeConsumeMap = new HashMap<>();
  private TimeConsumeType nowType;
  private long nowTypeStartMs;

  public static ProgressStatus getProgressStatusFromOSS(String taskKey) {
    try {
      String taskStatusData = OSSClientHelper.getInstance().getFileContent(taskKey);
      return new ProgressStatus(JSON.parseObject(taskStatusData));
    } catch (Exception e) {
      log.warn("ProgressStatus::updateProgressStatus failed! " + e.getMessage());
    }
    return null;
  }

  public ProgressStatus(JSONObject context) {
    this.instanceId = context.getString("instanceId");
    this.taskKey = context.getString("taskKey");
    this.progressInfo = new ProgressInfo();
    this.errMsg = context.getString("errMsg");
    this.status = str2Status(context.getString("status"));
    this.persistenceWay = "oss";
  }

  public ProgressStatus(String instanceId, String taskKey, String persistenceWay) {
    this.status = JobStatus.pending;
    this.progressInfo = new ProgressInfo();
    this.instanceId = instanceId;
    this.taskKey = taskKey;
    this.persistenceWay = persistenceWay;
  }

  public void init(long totalSteps) {
    this.status = JobStatus.pending;
    this.progressInfo.setTotalSteps(totalSteps);
  }

  private JobStatus str2Status(String s) {
    if ("ERROR".equals(s)) {
      return JobStatus.error;
    } else if ("FINISH".equals(s)) {
      return JobStatus.finished;
    } else if ("RUNNING".equals(s)) {
      return JobStatus.running;
    } else {
      return JobStatus.pending;
    }
  }

  private String status2Str(JobStatus status) {
    if (status == JobStatus.error) {
      return "ERROR";
    } else if (status == JobStatus.finished) {
      return "FINISH";
    } else if (status == JobStatus.running) {
      return "RUNNING";
    } else {
      // pending
      return "WAIT";
    }
  }

  public String toJson() {
    Map<String, Object> context = new HashMap<>();
    context.put("instanceId", this.instanceId);
    context.put("taskKey", this.taskKey);
    context.put("errMsg", this.errMsg);
    context.put("progressInfo", this.progressInfo);
    context.put("status", status2Str(this.status));
    context.put("timeConsumeMap", this.timeConsumeMap);
    context.put("ts", System.currentTimeMillis() / 1000);
    return JSON.toJSONString(context, true);
  }

  public void persistenceProgressStatus() {
    log.info("persistenceProgressStatus: storage is " + this.persistenceWay + " " + this.toJson());
    if ("oss".equals(this.persistenceWay)) {
      persistenceProgressToOSS();
    } else if (this.persistenceWay.startsWith(FileUtil.LOCAL_FILE_PREFIX)) {
      persistenceLocalFile();
    } else {
      log.error("persistenceProgressStatus not support");
    }
  }

  private synchronized void persistenceProgressToOSS() {
    try {
      OSSClientHelper.getInstance().putFileContent(taskKey, toJson());
    } catch (Exception e) {
      log.warn("ProgressStatus::persistenceProgressStatus update failed! " + e.getMessage());
    }
  }

  private synchronized void persistenceLocalFile() {
    try {
      String filePath = taskKey.substring(FileUtil.LOCAL_FILE_PREFIX.length());
      File file = new File(filePath);
      FileOutputStream outputStream = new FileOutputStream(file);
      outputStream.write(toJson().getBytes(StandardCharsets.UTF_8));
      outputStream.close();
    } catch (Exception e) {
      log.warn("ProgressStatus::persistenceProgressStatus update failed! " + e.getMessage());
    }
  }

  /** Retrieve task status information from storage. */
  public void refresh() {
    if ("oss".equals(this.persistenceWay)) {
      String content = null;
      try {
        content = OSSClientHelper.getInstance().getFileContent(taskKey);
        if (StringUtils.isBlank(content)) {
          return;
        }
        JSONObject contentObj = JSONObject.parseObject(content);
        this.errMsg = contentObj.getString("errMsg");
        this.progressInfo = contentObj.getObject("progressInfo", ProgressInfo.class);
        this.status = contentObj.getObject("status", JobStatus.class);
      } catch (Exception ex) {
        log.error("content={} refresh error, ", content, ex);
      }
    }
  }

  public void reset() {
    if ("oss".equals(this.persistenceWay)) {
      try {
        OSSClientHelper.getInstance().removeFile(taskKey);
      } catch (Exception ex) {
        log.error("reset error, ", ex);
      }
    }
  }

  public void updateStatus(JobStatus jobStatus, String errMsg) {
    this.status = jobStatus;
    this.errMsg = errMsg;
    persistenceProgressStatus();
  }

  public void finishedProgress() {
    this.status = JobStatus.finished;
    this.progressInfo.setProcessOffset(this.progressInfo.getRealTotal());
    this.progressInfo.setReadOffset(this.progressInfo.getRealTotal());
    persistenceProgressStatus();
  }

  public boolean isFinished() {
    return JobStatus.finished == this.status;
  }

  public boolean isError() {
    return JobStatus.error == this.status;
  }

  public void setStepTotal(long step, long total) {
    if (isFinished()) {
      throw new RuntimeException(
          "task is finished, can not change status, now status=" + this.status);
    }
    if (step == this.progressInfo.getCurStep() && total == this.progressInfo.getTotal()) {
      // The same steps do not need to be reset.
      return;
    }
    this.status = JobStatus.running;
    this.progressInfo.setTotal(total);
    this.progressInfo.setCurStep(step);
    if (this.progressInfo.getTotalSteps() < this.progressInfo.getCurStep()) {
      this.progressInfo.setTotalSteps(step);
    }
    this.progressInfo.setProcessOffset(0);
    this.progressInfo.setReadOffset(0);

    persistenceProgressStatus();
  }

  public void updateProgress(long batchId, long readOffset, long processOffset) {
    if (isFinished()) {
      throw new RuntimeException(
          "task is finished, can not change status, now status=" + this.status);
    }
    this.status = JobStatus.running;
    this.progressInfo.setBatchId(batchId);
    this.progressInfo.setReadOffset(readOffset);
    this.progressInfo.setProcessOffset(processOffset);
    if (this.progressInfo.getProcessOffset() > this.progressInfo.getTotal()) {
      log.warn(
          "ProgressStatus::updateProgress ProcessOffset("
              + this.progressInfo.getProcessOffset()
              + ") > Total("
              + this.progressInfo.getTotal()
              + ")");
      this.progressInfo.setTotal(this.progressInfo.getRealProcessOffset());
    }

    persistenceProgressStatus();
  }

  public void setTimeConsumeType(TimeConsumeType type) {
    long nowMs = System.currentTimeMillis();

    if (null == nowType) {
      nowType = type;
      nowTypeStartMs = nowMs;
    } else if (!nowType.equals(type)) {
      Long oldV = timeConsumeMap.getOrDefault(nowType, 0L);
      timeConsumeMap.put(nowType, oldV + (nowMs - nowTypeStartMs));

      nowType = type;
      nowTypeStartMs = nowMs;
    }
  }

  public enum JobStatus implements Serializable {
    running,
    pending,
    error,
    finished
  }

  @Getter
  @Setter
  public static class ProgressInfo implements Serializable {
    private long batchId;
    private long totalSteps;
    private long curStep;
    private long total;
    private long readOffset;
    private long processOffset;

    private long shrinkFactor = 1;

    public long getTotal() {
      return total / this.shrinkFactor;
    }

    @JSONField(serialize = false)
    public long getRealTotal() {
      return total;
    }

    public void setTotal(long total) {
      if (total <= Integer.MAX_VALUE) {
        shrinkFactor = 1;
      } else {
        long tmpTotal = total;
        while (tmpTotal > Integer.MAX_VALUE) {
          tmpTotal = tmpTotal / 10;
          shrinkFactor = shrinkFactor * 10;
        }
      }
      this.total = total;
    }

    @JSONField(serialize = false)
    public long getRealReadOffset() {
      return readOffset;
    }

    public long getReadOffset() {
      return readOffset / this.shrinkFactor;
    }

    public long getProcessOffset() {
      return processOffset / this.shrinkFactor;
    }

    @JSONField(serialize = false)
    public long getRealProcessOffset() {
      return processOffset;
    }
  }
}
