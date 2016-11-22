package com.handwin.localentity;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.Id;
import org.codehaus.jackson.annotate.JsonIgnore;

import java.util.Map;

/**
 * 消息类型内容，json序列化后保存到cassandra中
 */
public class Message {
    @Id
    private Long id;

    @Column
    private String type;   //文本 图片 语音

    @Column(name = "conversation_id")
    private String conversationId;

    @Column(name = "create_time")
    private Long createTime;

    @Column(name = "sender_id")
    private String sender;

    @Column(name = "receiver_type")
    private Integer receiverType;  //用户还是群组  '接受者类型 1:个人;2:群组',

    @JsonIgnore
    @Column(name = "receiver")
    private String receiver;

    @Column
    private int secret; //0:普通消息 1:加密消息

    @JsonIgnore
    private int isCount; //0 计数  1 或者其它 不计数

    /**
     * 消息的元信息
     */
    private Map<String, Object> meta;

    private String roomId;

    private Object content;

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    public int getSecret() {
        return secret;
    }

    public void setSecret(int secret) {
        this.secret = secret;
    }


    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public Integer getReceiverType() {
        return receiverType;
    }

    public void setReceiverType(Integer receiverType) {
        this.receiverType = receiverType;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public int getIsCount() {
        return isCount;
    }

    public void setIsCount(int isCount) {
        this.isCount = isCount;
    }

    public Map<String, Object> getMeta() {
        return meta;
    }

    public void setMeta(Map<String, Object> meta) {
        this.meta = meta;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Message{");
        sb.append("id=").append(id);
        sb.append(", type='").append(type).append('\'');
        sb.append(", conversationId='").append(conversationId).append('\'');
        sb.append(", createTime=").append(createTime);
        sb.append(", sender='").append(sender).append('\'');
        sb.append(", receiverType=").append(receiverType);
        sb.append(", receiver='").append(receiver).append('\'');
        sb.append(", secret=").append(secret);
        sb.append(", isCount=").append(isCount);
        sb.append(", meta=").append(meta);
        sb.append(", roomId='").append(roomId).append('\'');
        sb.append(", content=").append(content);
        sb.append('}');
        return sb.toString();
    }

    public Message() {

    }
}
