package com.handwin.entity;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.Order;

/**
 * Created by lb on 14/11/6.
 */
public class GroupKey {
    @Column(name = "group_id")
    @Order(1)
    private String groupId;

    @Column(name = "user_id")
    @Order(2)
    private String userId;

    public GroupKey() {

    }
    public GroupKey(String groupId, String userId) {
        this.groupId = groupId;
        this.userId = userId;
    }
    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean equals(Object o) {
        if(!(o instanceof GroupKey)) return false;
        GroupKey ko = (GroupKey)o;

        return groupId.equals(ko.getGroupId()) && userId.equals(ko.getUserId());
    }
}

