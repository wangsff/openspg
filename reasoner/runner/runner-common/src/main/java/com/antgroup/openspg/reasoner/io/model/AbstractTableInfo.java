/*
 * Ant Group
 * Copyright (c) 2004-2023 All Rights Reserved.
 */
package com.antgroup.openspg.reasoner.io.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.antgroup.openspg.reasoner.common.table.Field;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author donghai.ydh
 * @version AbstractTableInfo.java, v 0.1 2023年03月06日 12:01 donghai.ydh
 */
public abstract class AbstractTableInfo implements Serializable {
    protected String project;
    protected String table;

    protected Map<String, String> partition;

    protected List<Field> columns;

    /**
     * Getter method for property <tt>project</tt>.
     * @return property value of project
     */
    public String getProject() {
        return project;
    }

    /**
     * Setter method for property <tt>project</tt>.
     * @param project value to be assigned to property project
     */
    public void setProject(String project) {
        this.project = project;
    }

    /**
     * Getter method for property <tt>table</tt>.
     * @return property value of table
     */
    public String getTable() {
        return table;
    }

    /**
     * Setter method for property <tt>table</tt>.
     * @param table value to be assigned to property table
     */
    public void setTable(String table) {
        this.table = table;
    }

    /**
     * Getter method for property <tt>partition</tt>.
     * @return property value of partition
     */
    public Map<String, String> getPartition() {
        return partition;
    }

    /**
     * Setter method for property <tt>partition</tt>.
     * @param partition value to be assigned to property partition
     */
    public void setPartition(Map<String, String> partition) {
        this.partition = partition;
    }

    /**
     * Getter method for property <tt>columns</tt>.
     * @return property value of columns
     */
    public List<Field> getColumns() {
        return columns;
    }

    /**
     * get lube field
     */
    @JSONField(serialize = false)
    public List<com.antgroup.openspg.reasoner.lube.catalog.struct.Field> getLubeColumns() {
        return this.columns.stream().map(new Function<Field, com.antgroup.openspg.reasoner.lube.catalog.struct.Field>() {
            @Override
            public com.antgroup.openspg.reasoner.lube.catalog.struct.Field apply(Field field) {
                return new com.antgroup.openspg.reasoner.lube.catalog.struct.Field(field.getName(), field.getType().getKgType(), true);
            }
        }).collect(Collectors.toList());
    }

    /**
     * Setter method for property <tt>columns</tt>.
     * @param columns value to be assigned to property columns
     */
    public void setColumns(List<Field> columns) {
        this.columns = columns;
    }

    /**
     * hash code
     */
    @Override
    public int hashCode() {
        return this.getTableInfoKeyString().hashCode();
    }

    /**
     * equals
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof OdpsTableInfo)) {
            return false;
        }
        OdpsTableInfo that = (OdpsTableInfo) obj;
        return that.getTableInfoKeyString().equals(that.getTableInfoKeyString());
    }

    /**
     * get key
     */
    @JSONField(serialize = false)
    public String getTableInfoKeyString() {
        String str = "table=" + this.project + "." + this.table;
        String partitionString = getPartitionString();
        if (StringUtils.isNotEmpty(partitionString)) {
            str += ",partition[" + partitionString + "]";
        }
        return str;
    }

    /**
     * convert map partition info to string
     */
    @JSONField(serialize = false)
    public String getPartitionString() {
        if (null == this.partition) {
            return null;
        }

        List<String> partitionKeys = Lists.newArrayList(this.partition.keySet());
        partitionKeys.sort(String::compareTo);

        StringBuilder sb = new StringBuilder();
        for (String partitionKey : partitionKeys) {
            String partitionValue = this.partition.get(partitionKey);
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(partitionKey).append("='").append(partitionValue).append("'");
        }
        return sb.toString();
    }

}