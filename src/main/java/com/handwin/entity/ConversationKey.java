package com.handwin.entity;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.Order;


public class ConversationKey {
    @Column(name = "user_id")
    @Order(1)
    private String userId;

    @Column(name = "entity_id")
    @Order(2)
    private String entityId;

    public ConversationKey() {

    }

    public ConversationKey(String userId, String entityId) {
        this.userId = userId;

        this.entityId = entityId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    @Override
    public String toString() {
        return "ConversationKey{" +
                "userId='" + userId + '\'' +
                ", entityId='" + entityId + '\'' +
                '}';
    }
}
