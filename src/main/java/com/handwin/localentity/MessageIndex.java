package com.handwin.localentity;

import info.archinnov.achilles.annotations.*;



/**
 * 用户消息表，保存离线消息
 */
@Entity(table = "user_messages", comment = "用户离线消息存储表")
public class MessageIndex {
    @EmbeddedId
    private MessageIndexKey id;

    @Column
    @JSON
    private Message message;

    @Column(name = "group_id")
    private String groupId;

    @Column
    private byte[] content;

    public MessageIndex() {

    }

    public MessageIndex(MessageIndexKey id,Message message) {
        this.id = id;
        this.message = message;
        this.groupId = message.getConversationId();
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public MessageIndexKey getId() {
        return id;
    }

    public void setId(MessageIndexKey id) {
        this.id = id;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }
}
