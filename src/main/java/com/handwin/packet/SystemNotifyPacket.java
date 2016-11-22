package com.handwin.packet;


import com.handwin.bean.RichMessageInfo;
import com.handwin.utils.SystemConstant;
import com.handwin.utils.UserUtils;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by piguangtao on 14-3-10.
 * <p>
 * 临时增加系统通知的扩展属性 以便支持其它模块发送过来的系统通知
 */
public class SystemNotifyPacket extends AbstractBasePacket {

    private static final long serialVersionUID = -4269292127035786736L;

    public final static byte SYSTEM_NOTIFY_PACKAGE_TYPE = 0x08;

    public final static byte SERVICE_TYPE_NEED_SAVE = 0x01;

    public final static byte SERVICE_TYPE_NEED_READ = 0x02;

    public final static byte SERVICE_TYPE_NEED_PUSH = 0x04;

    public final static byte SERVICE_TYPE_UNREAD_INCREASE = 0x08;

    public final static byte SERVICE_TYPE_NEED_ACK = 0x10;

    public final static byte SERVICE_TYPE_GROUP = 0x20;

    private byte msgType;

    private byte serveType;

    private String from;

    private String to;

    private long expired;

    private int pushContentLength;

    private String pushContentBody;

    private int messsageLength;

    private String messageBody;

    private long msgId;

    private String cmsgId;

    private Map<String, Object> extra = new HashMap<>();

    private RichMessageInfo pushContentTemplate = null;

    public SystemNotifyPacket() {
        this.setPacketType(SYSTEM_NOTIFY_PACKAGE_TYPE);
    }

    public byte getMsgType() {
        return msgType;
    }

    public void setMsgType(byte msgType) {
        this.msgType = msgType;
    }

    public byte getServeType() {
        return serveType;
    }

    public void setServeType(byte serveType) {
        this.serveType = serveType;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public long getExpired() {
        return expired;
    }

    public void setExpired(long expired) {
        this.expired = expired;
    }

    public int getPushContentLength() {
        return pushContentLength;
    }

    public void setPushContentLength(int pushContentLength) {
        this.pushContentLength = pushContentLength;
    }

    public String getPushContentBody() {
        return pushContentBody;
    }

    public void setPushContentBody(String pushContentBody) {
        this.pushContentBody = pushContentBody;
    }

    public int getMesssageLength() {
        return messsageLength;
    }

    public void setMesssageLength(int messsageLength) {
        this.messsageLength = messsageLength;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }

    public long getMsgId() {
        return msgId;
    }

    public void setMsgId(long msgId) {
        this.msgId = msgId;
    }

    public String getCmsgId() {
        return cmsgId;
    }

    public void setCmsgId(String cmsgId) {
        this.cmsgId = cmsgId;
    }

    public Map<String, Object> getExtra() {
        return extra;
    }

    public void setExtra(Map<String, Object> extra) {
        this.extra = extra;
    }

    public RichMessageInfo getPushContentTemplate() {
        return pushContentTemplate;
    }

    public void setPushContentTemplate(RichMessageInfo pushContentTemplate) {
        this.pushContentTemplate = pushContentTemplate;
    }

    @Override
    public void attachThirdUserId(Integer appID) {

        if (StringUtils.isNotBlank(to)) {
            to = UserUtils.attachThirdUserID(to, appID);
        }

        if (StringUtils.isNotBlank(from)) {
            from = UserUtils.attachThirdUserID(from, appID);
        }

    }

    //    @Override
//    public String toString() {
//        final StringBuilder sb = new StringBuilder("SystemNotifyPackage{");
//        sb.append("msgType=").append(msgType);
//        sb.append(", serveType=").append(serveType);
//        sb.append(", from='").append(from).append('\'');
//        sb.append(", to='").append(to).append('\'');
//        sb.append(", expired=").append(expired);
//        sb.append(", pushContentLength=").append(pushContentLength);
//        sb.append(", pushContentBody='").append(pushContentBody).append('\'');
//        sb.append(", messsageLength=").append(messsageLength);
////        sb.append(", messageBody='").append(messageBody).append('\'');
//        sb.append(", msgId=").append(msgId);
//        sb.append('}');
//        return sb.toString();
//    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SystemNotifyPacket{");
        sb.append("msgType=").append(msgType);
        sb.append(", serveType=").append(serveType);
        sb.append(", from='").append(from).append('\'');
        sb.append(", to='").append(to).append('\'');
        sb.append(", expired=").append(expired);
        sb.append(", pushContentLength=").append(pushContentLength);
        sb.append(", pushContentBody='").append(pushContentBody).append('\'');
        sb.append(", messsageLength=").append(messsageLength);
//        sb.append(", messageBody='").append(messageBody).append('\'');
        sb.append(", msgId=").append(msgId);
        sb.append(", cmsgId='").append(cmsgId).append('\'');
        sb.append(", extra=").append(null != extra && extra.size() > 0 ? extra.entrySet().toString() : "");
        sb.append('}');
        return sb.toString();
    }

    public boolean isNeedSave() {
        return (serveType & SERVICE_TYPE_NEED_SAVE) == SERVICE_TYPE_NEED_SAVE;
    }

    public boolean isNeedRead() {
        return (serveType & SERVICE_TYPE_NEED_READ) == SERVICE_TYPE_NEED_READ;
    }

    public boolean isNeedPush() {
        return (serveType & SERVICE_TYPE_NEED_PUSH) == SERVICE_TYPE_NEED_PUSH;
    }

    public boolean isNeedAck() {
        return (serveType & SERVICE_TYPE_NEED_ACK) == SERVICE_TYPE_NEED_ACK;
    }


    public boolean isGroup() {
        return (serveType & SERVICE_TYPE_GROUP) == SERVICE_TYPE_GROUP;
    }


    /**
     * 离线push消息数目是否增加1
     *
     * @return
     */
    public boolean isUnreadInc() {
        return (serveType & SERVICE_TYPE_UNREAD_INCREASE) == SERVICE_TYPE_UNREAD_INCREASE;
    }


    public String getGroupId() {
        String groupId = null;
        if (null != this.getExtra()) {
            if (null != this.getExtra().get(SystemConstant.SYSTEM_NOTIFY_EXTRA_KEY_GROUP_ID)) {
                groupId = (String) this.getExtra().get(SystemConstant.SYSTEM_NOTIFY_EXTRA_KEY_GROUP_ID);
            }
        }
        return groupId;
    }
}
