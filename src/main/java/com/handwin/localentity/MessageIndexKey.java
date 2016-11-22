package com.handwin.localentity;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.Entity;
import info.archinnov.achilles.annotations.Order;



/**
 * 用户离线消息复合主键
 */
public class MessageIndexKey {
    @Column(name = "user_id")
    @Order(1)
    private String userId;

    @Column(name = "message_id")
    @Order(2)
    private Long messageId;



    public MessageIndexKey() {

    }

    public MessageIndexKey(String userId, Long messageId) {
        this.userId = userId;
        this.messageId = messageId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }
}
