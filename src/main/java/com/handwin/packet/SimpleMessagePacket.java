package com.handwin.packet;


import com.handwin.bean.RichMessageInfo;
import com.handwin.utils.UserUtils;
import org.apache.commons.lang.StringUtils;

import java.nio.charset.StandardCharsets;

/**
 * 在使用过程中建议不要修改该对象
 */
public class SimpleMessagePacket extends AbstractBasePacket implements Cloneable {

    public final static byte SIMPLE_MESSAGE_PACKET_TYPE = 0x05;

    public final static byte TEXT_MESSAGE_TYPE = 0x01;
    public final static byte FORWARD_MESSAGE_TYPE = 0x02;
    public final static byte IMAGE_URL_MESSAGE_TYPE = 0x03;
    public final static byte VOICE_MESSAGE_TYPE = 0x05;
    public final static byte VIDEO_MESSAGE_TYPE = 0X06;

    public final static byte STATUS_MESSAGE_TYPE = 0x10;
    public final static byte STATUS_RESPONSE_MESSAGE_TYPE = 0x11;

    public final static byte TO_USER = 0x01;
    public final static byte TO_GROUP = 0x02;

    private byte messageType;

    /**
     * 占用协议的4bit
     */
    private byte messageServiceType;

    /**
     * 消息内容格式 0x10 表示富媒体格式
     */
    private byte entityType = 0x00;

    /**
     * toUser可能是群组ID 也可能是接受方Id
     * 不能直接使用toUser作为接受方的userId
     */
    private String toUser;
    private String toGroup;
    private String from;
    private String fromGroup;
    //    private String content;
    private byte[] content;

    private Long messageId;
    private int tempId;

    /**
     * 客户端是否重发的消息 0x00表示未重发的消息 0x01表示重发的消息
     */
    private byte msgFlag = 0x00;

    /**
     * 客户端发送消息请求的消息id（客户端生成的消息id）
     */
    private String cmsgid;

    /**
     * 客户端发送的富媒体消息格式
     * 转化后的 在离线push使用
     */
    private RichMessageInfo richMessageInfo;

    public byte getMessageType() {
        return messageType;
    }

    public void setMessageType(byte messageType) {
        this.messageType = messageType;
    }

    public byte getMessageServiceType() {
        return messageServiceType;
    }

    public void setMessageServiceType(byte messageServiceType) {
        this.messageServiceType = messageServiceType;
    }

    public String getToUser() {
        return toUser;
    }

    public void setToUser(String toUser) {
        this.toUser = toUser;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getFromGroup() {
        return fromGroup;
    }

    public void setFromGroup(String fromGroup) {
        this.fromGroup = fromGroup;
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public int getTempId() {
        return tempId;
    }

    public void setTempId(int tempId) {
        this.tempId = tempId;
    }

    public String getToGroup() {
        return toGroup;
    }

    public void setToGroup(String toGroup) {
        this.toGroup = toGroup;
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public String getCmsgid() {
        return cmsgid;
    }

    /**
     * fixed 2016-03-25 解决 android本地化时 生成的uuid后4个字符非英文数字时 导致cmsgId长度超过36个字节 ios客户端无法解析
     *
     * @param cmsgid
     */
    public void setCmsgid(String cmsgid) {
        if (StringUtils.isNotBlank(cmsgid) && cmsgid.getBytes(StandardCharsets.UTF_8).length > 36) {
            this.cmsgid = cmsgid.substring(0, 32) + "9999";
        } else {
            this.cmsgid = cmsgid;
        }
    }

    public byte getMsgFlag() {
        return msgFlag;
    }

    public void setMsgFlag(byte msgFlag) {
        this.msgFlag = msgFlag;
    }

    public byte getEntityType() {
        return entityType;
    }

    public void setEntityType(byte entityType) {
        this.entityType = entityType;
    }

    public RichMessageInfo getRichMessageInfo() {
        return richMessageInfo;
    }

    public void setRichMessageInfo(RichMessageInfo richMessageInfo) {
        this.richMessageInfo = richMessageInfo;
    }


    @Override
    public void attachThirdUserId(Integer appID) {

        if (StringUtils.isNotBlank(from)) {
            from = UserUtils.attachThirdUserID(from, appID);
        }

        if (StringUtils.isNotBlank(fromGroup)) {
            fromGroup = UserUtils.attachThirdUserID(fromGroup, appID);
        }

        if (StringUtils.isNotBlank(toUser)) {
            toUser = UserUtils.attachThirdUserID(toUser, appID);
        }

        if (StringUtils.isNotBlank(toGroup)) {
            toGroup = UserUtils.attachThirdUserID(toGroup, appID);
        }


    }
}
