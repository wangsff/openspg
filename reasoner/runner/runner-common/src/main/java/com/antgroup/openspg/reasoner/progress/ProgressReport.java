/*
 * Ant Group
 * Copyright (c) 2004-2023 All Rights Reserved.
 */
package com.antgroup.openspg.reasoner.progress;

import com.antgroup.openspg.reasoner.progress.ProgressStatus.JobStatus;
import com.antgroup.openspg.reasoner.runner.ConfigKey;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;

/**
 * @author donghai.ydh
 * @version ProgressReport.java, v 0.1 2023年04月18日 15:31 donghai.ydh
 */
public class ProgressReport {
    private static ProgressStatus progressStatus = null;

    /**
     * report error
     */
    public static void reportError(Map config, Throwable e) {
        if (!config.containsKey(ConfigKey.KG_REASONER_PARAMS)) {
            return;
        }
        if (null == progressStatus) {
            init(getProgressPath(config), 1);
        }
        updateStatus(JobStatus.error, e.getMessage());
    }

    /**
     * init progress status report
     */
    public static void init(Object path, long totalSteps) {
        if (null == path) {
            progressStatus = null;
            return;
        }
        progressStatus = new ProgressStatus("", String.valueOf(path), "oss");
        progressStatus.init(totalSteps);
    }

    /**
     * set total num
     */
    public static void setStepTotal(long step, long total) {
        if (null != progressStatus) {
            progressStatus.setStepTotal(step, total);
        }
    }

    /**
     * update progress
     */
    public static void updateProgress(long batchId, long readOffset, long processOffset) {
        if (null != progressStatus) {
            progressStatus.updateProgress(batchId, readOffset, processOffset);
        }
    }

    /**
     * finish task
     */
    public static void finishedProgress() {
        if (null != progressStatus) {
            progressStatus.finishedProgress();
        }
    }

    /**
     * report status
     */
    public static void updateStatus(JobStatus jobStatus, String errMsg) {
        if (null != progressStatus) {
            progressStatus.updateStatus(jobStatus, errMsg);
        }
    }

    /**
     * clear
     */
    public static void clear() {
        progressStatus = null;
    }

    /**
     * set time consume type
     */
    public static void setTimeConsumeType(TimeConsumeType type) {
        if (null != progressStatus) {
            progressStatus.setTimeConsumeType(type);
        }
    }

    /**
     * update
     */
    public static void persistenceProgressStatus() {
        if (null != progressStatus) {
            progressStatus.persistenceProgressStatus();
        }
    }

    private static String getProgressPath(Map config) {
        String paramStringEncoded = String.valueOf(config.get(ConfigKey.KG_REASONER_PARAMS));
        String paramsJsonString = new String(Base64.getDecoder().decode(
                paramStringEncoded), StandardCharsets.UTF_8);
        Map<String, Object> params = new HashMap<>(JSON.parseObject(paramsJsonString));
        return (String) params.get(ConfigKey.KG_REASONER_PROGRESS_PATH);
    }

}