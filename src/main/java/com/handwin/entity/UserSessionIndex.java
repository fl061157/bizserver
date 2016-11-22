package com.handwin.entity;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.EmbeddedId;
import info.archinnov.achilles.annotations.Entity;

/**
 * 用户+APP对应的session索引
 */
@Entity(table = "user_session_indexes", keyspace = "faceshow", comment = "用户+APP对应的session索引")
public class UserSessionIndex {
    @EmbeddedId
    private UserSessionIndexKey id;

    @Column(name = "session_id")
    private String sessionId;

    public UserSessionIndexKey getId() {
        return id;
    }

    public void setId(UserSessionIndexKey id) {
        this.id = id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    public UserSessionIndex() {
    	
    }
}
