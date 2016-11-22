package com.handwin.entity;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.Id;
import info.archinnov.achilles.annotations.Order;

/**
 * Created by hi on 14-3-6.
 */
public class TokenKey {
    @Column(name = "user_id")
    @Order(1)
    private String userId;

    @Column(name = "app_id")
    @Order(2)
    private Integer appId;

    public TokenKey() {

    }

    public TokenKey(String userId,int appId) {
        this.userId = userId;
        this.appId = appId;
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

