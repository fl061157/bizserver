package com.handwin.entity;

import com.google.common.base.Joiner;
import com.handwin.utils.SystemConstant;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by piguangtao on 14-3-15.
 */
public class PushMsgMqBean {

    public static final String TYPE_CALL = "1";

    public static final String TYPE_MESSAGE = "2";

    public static final String DEVICE_TYPE_IOS = "1";

    public static final String DEVICE_TYPE_ANDRIOD = "2";

    public static final String SUBTYPE_CALL_AUDIO = "1";

    public static final String SUBTYPE_CALL_VIDEO = "2";

    public static final String SUBTYPE_GAME_CALL = "3";

    public static final String SUBTYPE_MESSAGE_PERSON = "1";

    public static final String SUBTYPE_MESSAGE_GROUP = "2";

    /**
     * google的推送服务
     */
    public static final String PROVIDER_GOOGLE = "1";

    /**
     * 个推的推送的服务
     */
    public static final String PROVIDER_IGT = "2";

    public static final String PROVIDER_IOS_PRIVATE = "3";

    public static final String PROVIDER_IOS_ENTERPRISE = "4";

    public static final Integer NONE = 0;
    public static final Integer VIDEO = 1;
    public static final Integer AUDIO = 2;

    /**
     * 大类型 1：电话 2：普通消息
     */
    String type;
    String content;
    String appId;
    String from;
    String to;
    String traceId;
    Integer unreadCount;

    String fromNickName;
    String fromMobile;
    String fromAvatarUrl;
    String fromCountryCode;

    /**
     * 消息的生成时间 格林威治标准时间
     */
    String time;

    /**
     * 设备类型 1:iphone, 2:andriod
     */
    String deviceType;

    String deviceToken;

    /**
     * 子类型
     */
    String subType;

    /**
     * 服务提供商
     */
    String provider;

    /**
     * 展示使用，没有消息内容
     */
    String tipType;

    /**
     * IOS需求
     * 是否可以直接回复
     */
    boolean replyType = false;


    /**
     * 通知类型 1：表示声音+振动 2:表示声音 2:振动 4:无声音，也无振动
     * 默认: 有声音
     */
    NoticeType noticeType = NoticeType.SOUND;


    Map<String, String> attrs = new HashMap<>();


    /**
     * 推送消息的其它描述信息
     */
    Map<String, String> metaMap = new HashMap<>();

    public Map<String, String> getAttrs() {
        return attrs;
    }

    public void setAttrs(Map<String, String> attrs) {
        this.attrs = attrs;
    }

    public void setMediaType(Integer mediaType) {
        if (null != mediaType) {
            attrs.put("media_type", mediaType.toString());
        }
    }

    public void setExtraData(byte[] extraData) {
        if (null != extraData) {
            attrs.put("extra_data", new String(extraData, SystemConstant.CHARSET_UTF8));
        }
    }

    public void setGameIds(List<String> gameIds) {
        if (null != gameIds) {
            attrs.put("game_ids", Joiner.on(",").join(gameIds));
        }
    }

    public void addAttr(String key, String value) {
        if (StringUtils.isNotBlank(key) && StringUtils.isNotBlank(value)) {
            attrs.put(key, value);
        }
    }

    public void addMeta(String key, String value) {
        if (StringUtils.isNotBlank(key) && StringUtils.isNotBlank(value)) {
            metaMap.put(key, value);
        }
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
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

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public Integer getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(Integer unreadCount) {
        this.unreadCount = unreadCount;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public NoticeType getNoticeType() {
        return noticeType;
    }

    public void setNoticeType(NoticeType noticeType) {
        this.noticeType = noticeType;
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

    public String getTipType() {
        return tipType;
    }

    public void setTipType(String tipType) {
        this.tipType = tipType;
    }

    public boolean isReplyType() {
        return replyType;
    }

    public void setReplyType(boolean replyType) {
        this.replyType = replyType;
    }

    public Map<String, String> getMetaMap() {
        return metaMap;
    }

    public void setMetaMap(Map<String, String> metaMap) {
        this.metaMap = metaMap;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PushMsgMqBean{");
        sb.append("type='").append(type).append('\'');
        sb.append(", content='").append(content).append('\'');
        sb.append(", appId='").append(appId).append('\'');
        sb.append(", from='").append(from).append('\'');
        sb.append(", to='").append(to).append('\'');
        sb.append(", traceId='").append(traceId).append('\'');
        sb.append(", unreadCount=").append(unreadCount);
        sb.append(", fromNickName='").append(fromNickName).append('\'');
        sb.append(", fromMobile='").append(fromMobile).append('\'');
        sb.append(", fromAvatarUrl='").append(fromAvatarUrl).append('\'');
        sb.append(", fromCountryCode='").append(fromCountryCode).append('\'');
        sb.append(", time='").append(time).append('\'');
        sb.append(", deviceType='").append(deviceType).append('\'');
        sb.append(", deviceToken='").append(deviceToken).append('\'');
        sb.append(", subType='").append(subType).append('\'');
        sb.append(", provider='").append(provider).append('\'');
        sb.append(", tipType='").append(tipType).append('\'');
        sb.append(", replyType=").append(replyType);
        sb.append(", noticeType=").append(noticeType);
        sb.append(", attrs=").append(attrs);
        sb.append(", metaMap=").append(metaMap);
        sb.append('}');
        return sb.toString();
    }

    public static enum NoticeType {
        SOUND_AND_VIBRATE(1),
        SOUND(2),
        VIBRATE(3),
        NO_ALERT(4);
        int value;

        NoticeType(int i) {
            this.value = i;
        }

        public int getValue() {
            return value;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("NoticeType{");
            sb.append("value=").append(value);
            sb.append('}');
            return sb.toString();
        }
    }
}
