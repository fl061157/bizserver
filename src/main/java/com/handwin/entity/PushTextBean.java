package com.handwin.entity;

/**
 * Created by piguangtao on 14-1-20.
 */
public class PushTextBean extends PushBean {

    /**
     * 消息发送类型 1:个人 2:群组
     */
    private String sendType;

    /**
     * IOS需求
     * 是否可以直接回复
     */
    boolean replyType = false;

    public String getSendType() {
        return sendType;
    }

    public void setSendType(String sendType) {
        this.sendType = sendType;
    }

    public boolean isReplyType() {
        return replyType;
    }

    public void setReplyType(boolean replyType) {
        this.replyType = replyType;
    }

    @Override
    public String toString() {
        return "PushTextBean{" +
                "sendType='" + sendType + '\'' +
                ", replyType=" + replyType +
                '}';
    }
}
