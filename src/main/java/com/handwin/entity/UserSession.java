package com.handwin.entity;

import info.archinnov.achilles.annotations.*;

import static info.archinnov.achilles.type.ConsistencyLevel.QUORUM;

@Entity(table = "user_sessions", keyspace = "faceshow")
public class UserSession {

    @Id(name = "session_id")
    @Order(1)
    private String sessionId;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "app_id")
    private Integer appId;

    public Integer getAppId() {
        return appId;
    }

    public void setAppId(Integer appId) {
        this.appId = appId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
    public UserSession() {

    }
}