package com.handwin.entity;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.Order;
import info.archinnov.achilles.annotations.PartitionKey;

public class UserSessionIndexKey {
    @PartitionKey
    @Column(name = "user_id")
    @Order(1)
    private String userId;

    @PartitionKey
    @Column(name = "app_id")
    @Order(2)
    private Integer appId;



    public UserSessionIndexKey() {

    }
    public UserSessionIndexKey(String userId,Integer appId) {
        this.appId = appId;
        this.userId = userId;
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Integer getAppId() {
        return appId;
    }

    public void setAppId(Integer appId) {
        this.appId = appId;
    }
}
