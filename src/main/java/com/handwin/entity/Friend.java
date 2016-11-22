package com.handwin.entity;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.EmbeddedId;
import info.archinnov.achilles.annotations.Entity;
import info.archinnov.achilles.annotations.Index;


@Entity(table = "friends", keyspace = "faceshow", comment = "chatgame好友表")
public class Friend {
    @EmbeddedId
    private UserKey id;

    @Column(name = "update_time")
    private long updateTime;

    @Column(name = "contact_name")
    private String contactName;

    @Column(name = "resource_app_id")
    @Index
    private Integer resourceAppId;  // 来源app id


    public UserKey getId() {
        return id;
    }

    public void setId(UserKey id) {
        this.id = id;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public Integer getResourceAppId() {
        return resourceAppId;
    }

    public void setResourceAppId(Integer resourceAppId) {
        this.resourceAppId = resourceAppId;
    }

    public Friend() {
        this.updateTime = System.currentTimeMillis();
    }
    public Friend(UserKey id, String contactName) {
        this.id = id;
        this.contactName = contactName;
        this.updateTime = System.currentTimeMillis();
    }
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("id{").append("userId:").append(id.getUserId()).append("}");
        return result.toString();
    }




}
