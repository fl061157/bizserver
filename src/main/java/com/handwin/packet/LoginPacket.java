package com.handwin.packet;


import com.handwin.bean.Platform;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.util.HashMap;
import java.util.Map;

public class LoginPacket extends CommandPacket {

    public final static int COMMAND_LOGIN_TYPE = 0x01;
    public final static int COMMAND_LOGIN_RESPONSE_TYPE = 0x02;
    public final static int COMMAND_LOGIN_PACKET_TYPE = COMMAND_LOGIN_TYPE * 256 + COMMAND_PACKET_TYPE;

    private LoginStatus loginStatus;

    //采用IOS国际语言标准.i.e en/en-us/zh-cn/zh-hk等。
    private String language;

    //采用相较于格林尼治标准时间的偏差，如北京时区为+8等。
    private String timeZone;

    private String regionCode;

    private Map<String, byte[]> attrs = new HashMap<>();

    public LoginPacket() {
        this.setPacketType(COMMAND_LOGIN_PACKET_TYPE);
    }

    private String sessionId;


    //设置默认的mode为FOREGROUND
    private ChannelMode channelMode = ChannelMode.FOREGROUND;


    public final static String KICK_ID = "kick_id";

    /**
     * 第三方开发者的appkey
     */
    private Integer appKey = null;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public LoginStatus getLoginStatus() {
        return loginStatus;
    }

    public void setLoginStatus(LoginStatus loginStatus) {
        this.loginStatus = loginStatus;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public String getRegionCode() {
        return regionCode;
    }

    public void setRegionCode(String regionCode) {
        this.regionCode = regionCode;
    }

    public ChannelMode getChannelMode() {
        return channelMode;
    }

    public void setChannelMode(ChannelMode channelMode) {
        this.channelMode = channelMode;
    }

    @Override
    public void attachThirdUserId(Integer appID) {

    }

    public Platform getPlatform() {

        if (MapUtils.isEmpty(attrs)) {
            return Platform.Mobile;
        }
        byte[] pbs = attrs.get(Platform.PLATFROM);

        if (ArrayUtils.isEmpty(pbs)) {
            return Platform.Mobile;
        }

        return Platform.getPlatform(new String(pbs));

    }

    public String getKickId() {
        byte[] kib = attrs.get(KICK_ID);
        return ArrayUtils.isNotEmpty(kib) ? new String(kib) : null;
    }


    public void addAttr(String name, byte[] value) {
        this.attrs.put(name, value);
    }

    public byte[] getAttr(String name) {
        return this.attrs.get(name);
    }

    public Integer getAppKey() {
        return appKey;
    }

    public void setAppKey(Integer appKey) {
        this.appKey = appKey;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LoginPacket{");
        sb.append("loginStatus=").append(loginStatus);
        sb.append(", language='").append(language).append('\'');
        sb.append(", timeZone='").append(timeZone).append('\'');
        sb.append(", regionCode='").append(regionCode).append('\'');
        sb.append(", attrs=").append(attrs);
        sb.append(", sessionId='").append(sessionId).append('\'');
        sb.append(", channelMode=").append(channelMode);
        sb.append(", appKey=").append(appKey);
        sb.append('}');
        return sb.toString();
    }
}
