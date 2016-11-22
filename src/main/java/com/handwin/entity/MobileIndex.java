package com.handwin.entity;

import info.archinnov.achilles.annotations.*;

import static info.archinnov.achilles.type.ConsistencyLevel.*;


import info.archinnov.achilles.annotations.*;

import static info.archinnov.achilles.type.ConsistencyLevel.*;

@Entity(table = "user_mobiles", keyspace = "faceshow", comment = "用户号码索引表")
public class MobileIndex {
    @EmbeddedId
    private MobileKey mobileKey;

    public MobileKey getMobileKey() {
        return mobileKey;
    }

    public void setMobileKey(MobileKey mobileKey) {
        this.mobileKey = mobileKey;
    }

    @Column(name = "user_id")
    private String userId;


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
    public MobileIndex() {

    }
}