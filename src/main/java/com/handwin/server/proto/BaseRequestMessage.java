package com.handwin.server.proto;

import com.chatgame.protobuf.TcpBiz;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.nio.charset.Charset;

/**
 * @author fangliang
 */
public class BaseRequestMessage implements Serializable {

    private static final long serialVersionUID = 5199260600906668176L;

    private String traceId;
    private String tcpServerId;
    private String tcpChannelUuid;
    private String userIp;
    private int userPort;
    private String userId;
    private int appId;
    private int tcpChannelId;
    private String sessionId;
    private boolean isLocalUser;
    private String userZoneCode;
    private String tcpZoneCode;
    private int channelMode;
    private int clientVersion;
    private String backTrackInfo;
    private String chatRoomId;
    private String platform;

    private BaseRequestMessage() {

    }

    public static BaseRequestMessage build(TcpBiz.Tcp2BizReq requestMessage) {
        BaseRequestMessage baseRequestMessage = new BaseRequestMessage();
        baseRequestMessage.traceId = requestMessage.getTraceId();
        baseRequestMessage.tcpServerId = requestMessage.getTcpServerId();
        baseRequestMessage.tcpChannelUuid = requestMessage.getTcpChannelUuid();
        baseRequestMessage.userIp = requestMessage.getUserIp();
        baseRequestMessage.userPort = requestMessage.getUserPort();
        baseRequestMessage.userId = requestMessage.getUserId();
        baseRequestMessage.appId = requestMessage.getAppId();
        baseRequestMessage.tcpChannelId = requestMessage.getTcpChannelId();
        baseRequestMessage.sessionId = requestMessage.getSessionId();
        baseRequestMessage.isLocalUser = requestMessage.getIsLocalUser();
        baseRequestMessage.channelMode = requestMessage.getChannelMode();
        baseRequestMessage.tcpZoneCode = requestMessage.getTcpChannelZoneCode();
        baseRequestMessage.userZoneCode = requestMessage.getUserZoneCode();
        if (null != requestMessage.getBackTrackInfo()) { //TODO Chnage
            baseRequestMessage.backTrackInfo = new String(requestMessage.getBackTrackInfo().getBytes(), Charset.forName("UTF-8"));
        }
        baseRequestMessage.chatRoomId = requestMessage.getChatRoomId();
        baseRequestMessage.platform = requestMessage.getPlatform();

        return baseRequestMessage;
    }

    public String getTraceId() {
        return traceId;
    }

    public String getTcpServerId() {
        return tcpServerId;
    }

    public String getTcpChannelUuid() {
        return tcpChannelUuid;
    }

    public String getUserIp() {
        return userIp;
    }

    public int getUserPort() {
        return userPort;
    }

    public String getUserId() {
        return userId;
    }

    public int getAppId() {
        return appId;
    }

    public int getTcpChannelId() {
        return tcpChannelId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public boolean isLocalUser() {
        return isLocalUser;
    }

    public int getChannelMode() {
        return channelMode;
    }

    public String getTcpZoneCode() {
        return tcpZoneCode;
    }

    public String getUserZoneCode() {
        return userZoneCode;
    }

    public int getClientVersion() {
        return clientVersion;
    }

    public void setClientVersion(int version) {
        clientVersion = version;
    }

    public String getBackTrackInfo() {
        return backTrackInfo;
    }

    public void setAppId(int appId) {
        this.appId = appId;
    }


    public String getChatRoomId() {
        return chatRoomId;
    }

    public void setChatRoomId(String chatRoomId) {
        this.chatRoomId = chatRoomId;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
    }
}
