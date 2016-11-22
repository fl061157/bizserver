package com.handwin.entity;

import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by piguangtao on 14-3-12.
 */
public class OfflinePushPackageBean {
    public static final String SENDTYPE_GROUP = "2";
    public static final String SENDTYPE_PERSON = "1";

    private String traceId;
    private String fromUserId;
    private String toUserId;
    private String content;
    private int appId;

    /**
     * IOS离线push消息是否增加1
     */
    private boolean isUnreadInc = true;
    /**
     * IOS需求
     * 是否可以直接回复
     */
    boolean replyType = false;

    public OfflinePushPackageBean() {

    }

    /**
     * 1:个人消息;2:群组消息
     */
    private String sendType;

    String fromNickName;
    String fromMobile;
    String fromAvatarUrl;
    String fromCountryCode;
    String time;

    /**
     * tipTpe:1 表示 点击推送消息时，不需要进入到回话界面
     */
    private String tipType;

    private PushMsgMqBean.NoticeType noticeType;

    private Map<String, String> attrs = new HashMap<>();


    /**
     * 推送消息的其它描述信息
     */
    Map<String, String> metaMap = new HashMap<>();


    public void addAttr(String key, String value) {
        if (StringUtils.isNotBlank(key) && StringUtils.isNotBlank(value)) {
            attrs.put(key, value);
        }
    }

    public void addMeta(String key, String value) {
        if (StringUtils.isNotBlank(key) && StringUtils.isNotBlank(value)) {
            attrs.put(key, value);
        }
    }

    public Map<String, String> getAttrs() {
        return attrs;
    }

    public String getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
    }

    public String getToUserId() {
        return toUserId;
    }

    public void setToUserId(String toUserId) {
        this.toUserId = toUserId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public int getAppId() {
        return appId;
    }

    public void setAppId(int appId) {
        this.appId = appId;
    }

    public String getSendType() {
        return sendType;
    }

    public void setSendType(String sendType) {
        this.sendType = sendType;
    }

    public boolean isUnreadInc() {
        return isUnreadInc;
    }

    public void setUnreadInc(boolean isUnreadInc) {
        this.isUnreadInc = isUnreadInc;
    }

    public String getFromNickName() {
        return fromNickName;
    }

    public void setFromNickName(String fromNickName) {
        this.fromNickName = fromNickName;
    }

    public String getFromMobile() {
        return fromMobile;
    }

    public void setFromMobile(String fromMobile) {
        this.fromMobile = fromMobile;
    }

    public String getFromAvatarUrl() {
        return fromAvatarUrl;
    }

    public void setFromAvatarUrl(String fromAvatarUrl) {
        this.fromAvatarUrl = fromAvatarUrl;
    }

    public String getFromCountryCode() {
        return fromCountryCode;
    }

    public void setFromCountryCode(String fromCountryCode) {
        this.fromCountryCode = fromCountryCode;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isReplyType() {
        return replyType;
    }

    public void setReplyType(boolean replyType) {
        this.replyType = replyType;
    }

    public PushMsgMqBean.NoticeType getNoticeType() {
        return noticeType;
    }

    public void setNoticeType(PushMsgMqBean.NoticeType noticeType) {
        this.noticeType = noticeType;
    }

    public String getTipType() {
        return tipType;
    }

    public void setTipType(String tipType) {
        this.tipType = tipType;
    }

    public void setTimeToLiveInMillSec(Long toLiveInMillSec) {
        if (null != toLiveInMillSec) {
            this.addMeta("time_to_live", String.valueOf(toLiveInMillSec));
        }
    }

    public Map<String, String> getMetaMap() {
        return metaMap;
    }

    public void setMetaMap(Map<String, String> metaMap) {
        this.metaMap = metaMap;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("OfflinePushPackageBean{");
        sb.append("traceId='").append(traceId).append('\'');
        sb.append(", fromUserId='").append(fromUserId).append('\'');
        sb.append(", toUserId='").append(toUserId).append('\'');
        sb.append(", content='").append(content).append('\'');
        sb.append(", appId=").append(appId);
        sb.append(", isUnreadInc=").append(isUnreadInc);
        sb.append(", replyType=").append(replyType);
        sb.append(", sendType='").append(sendType).append('\'');
        sb.append(", fromNickName='").append(fromNickName).append('\'');
        sb.append(", fromMobile='").append(fromMobile).append('\'');
        sb.append(", fromAvatarUrl='").append(fromAvatarUrl).append('\'');
        sb.append(", fromCountryCode='").append(fromCountryCode).append('\'');
        sb.append(", time='").append(time).append('\'');
        sb.append(", tipType='").append(tipType).append('\'');
        sb.append(", noticeType=").append(noticeType);
        sb.append(", attrs=").append(attrs);
        sb.append('}');
        return sb.toString();
    }
}
