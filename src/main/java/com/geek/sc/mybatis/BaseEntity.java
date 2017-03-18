package com.geek.sc.mybatis;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.sql.Timestamp;


public abstract class BaseEntity implements Serializable {

    private static final long serialVersionUID = 8388417013613884409L;

    @JsonIgnore
    private Timestamp createTime;

    @JsonIgnore
    private Timestamp updateTime;

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    public void populateCreation() {
        this.setCreateTime(new Timestamp(System.currentTimeMillis()));
    }

    public void populateUpdate() {
        this.setUpdateTime(new Timestamp(System.currentTimeMillis()));
    }

}
