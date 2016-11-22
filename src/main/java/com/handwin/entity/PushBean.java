package com.handwin.entity;

import com.handwin.utils.SystemConstant;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by piguangtao on 14-1-20.
 */
public class PushBean {

    private String from;

    private String to;

    private String deviceToken;

    private String content;

    private boolean increament;

    /**
     * 消息的数目
     */
    private int msgNumber;

    /**
     * 消息跟踪id，没有业务含义，便于消息的跟踪
     */
    private String traceId;

    private DeviceType deviceType;

    private int appId;

    /**
     * 证服务提供商 1：google 2：个推（安卓必选） 3：iphone private  4：iphone enterprise
     */
    private String provider;

    String fromNickName;
    String fromMobile;
    String fromAvatarUrl;
    String fromCountryCode;
    String time;
    String tipType;

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

    public Map<String, String> getAttrs() {
        return attrs;
    }

    byte[] extraData;

    public byte[] getExtraData() {
        return extraData;
    }

    public void setExtraData(byte[] extraData) {
        this.extraData = extraData;
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

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getMsgNumber() {
        return msgNumber;
    }

    public void setMsgNumber(int msgNumber) {
        this.msgNumber = msgNumber;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    public int getAppId() {
        return appId;
    }

    public void setAppId(int appId) {
        this.appId = appId;
    }


    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
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

    public boolean isIncreament() {
        return increament;
    }

    public void setIncreament(boolean increament) {
        this.increament = increament;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTipType() {
        return tipType;
    }

    public void setTipType(String tipType) {
        this.tipType = tipType;
    }

    public PushMsgMqBean.NoticeType getNoticeType() {
        return noticeType;
    }

    public void setNoticeType(PushMsgMqBean.NoticeType noticeType) {
        this.noticeType = noticeType;
    }

    public Map<String, String> getMetaMap() {
        return metaMap;
    }

    public void setMetaMap(Map<String, String> metaMap) {
        this.metaMap = metaMap;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PushBean{");
        sb.append("from='").append(from).append('\'');
        sb.append(", to='").append(to).append('\'');
        sb.append(", deviceToken='").append(deviceToken).append('\'');
        sb.append(", content='").append(content).append('\'');
        sb.append(", msgNumber=").append(msgNumber);
        sb.append(", traceId=").append(traceId);
        sb.append(", deviceType=").append(deviceType);
        sb.append(", appId=").append(appId);
        sb.append(", provider='").append(provider).append('\'');
        sb.append(", fromNickName='").append(fromNickName).append('\'');
        sb.append(", fromMobile='").append(fromMobile).append('\'');
        sb.append(", fromAvatarUrl='").append(fromAvatarUrl).append('\'');
        sb.append(", fromCountryCode='").append(fromCountryCode).append('\'');
        sb.append(", time='").append(time).append('\'');
        sb.append(", tipType='").append(tipType).append('\'');
        sb.append(",extraData='").append(null == extraData ? "" : new String(extraData, SystemConstant.CHARSET_UTF8)).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
