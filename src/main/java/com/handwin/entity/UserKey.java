package com.handwin.entity;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.Order;
import info.archinnov.achilles.annotations.PartitionKey;

public class UserKey {
    @PartitionKey
    @Column(name = "user_id")
    @Order(1)
    private String userId;

    @PartitionKey
    @Column(name = "app_id")
    @Order(2)
    private Integer appId;

    @Column(name = "friend_id")
    @Order(3)
    private String friendId;

    public UserKey() {

    }

    public UserKey(String userId, Integer appId,  String friendId ) {
        this.userId = userId;
        this.appId = appId;
        this.friendId = friendId;
    }

    public Integer getAppId() {
        return appId;
    }

    public void setAppId(Integer appId) {
        this.appId = appId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFriendId() {
        return friendId;
    }

    public void setFriendId(String friendId) {
        this.friendId = friendId;
    }
}
