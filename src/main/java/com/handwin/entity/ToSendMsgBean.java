package com.handwin.entity;

import java.util.Arrays;

import com.handwin.localentity.MessageType;

/**
 * Created by piguangtao on 14-2-7.
 */
public class ToSendMsgBean {

    private String sender;

    private String receiver;

    private String receiverType;

    private Long createTime;

    private byte[] content;

    private MessageType type;

    private String appId;

    private Boolean isSecret;

    private String roomId;

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getReceiverType() {
        return receiverType;
    }

    public void setReceiverType(String receiverType) {
        this.receiverType = receiverType;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public Boolean isSecret() {
        return isSecret;
    }

    public void setSecret(Boolean isSecret) {
        this.isSecret = isSecret;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ToSendMsgBean{");
        sb.append("sender='").append(sender).append('\'');
        sb.append(", receiver='").append(receiver).append('\'');
        sb.append(", receiverType='").append(receiverType).append('\'');
        sb.append(", createTime=").append(createTime);
        sb.append(", content=").append(Arrays.toString(content));
        sb.append(", type=").append(type);
        sb.append(", appId='").append(appId).append('\'');
        sb.append(", isSecret=").append(isSecret);
        sb.append(", roomId='").append(roomId).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
