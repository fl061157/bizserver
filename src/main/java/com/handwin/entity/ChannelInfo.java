package com.handwin.entity;

import com.handwin.bean.Platform;
import com.handwin.packet.ChannelMode;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Created by piguangtao on 2014/7/9.
 */
public class ChannelInfo {
    private String ip;
    private int port;
    private String uuid;
    private String nodeId;
    private ChannelMode channelMode;
    private int id;
    private String tcpZoneCode;
    private String sessonId;
    private String userId;
    private int clientVersion;
    private int appID;
    private String userZoneCode;
    private String networkType;
    private String chatRoomID;
    private boolean isEmptyChatRoom;
    private String platform;
    private String kickId;
    private long createTime;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public ChannelMode getChannelMode() {
        return channelMode;
    }

    public void setChannelMode(ChannelMode channelMode) {
        this.channelMode = channelMode;
    }

    public String getSessonId() {
        return sessonId;
    }

    public void setSessonId(String sessonId) {
        this.sessonId = sessonId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTcpZoneCode() {
        return tcpZoneCode;
    }

    public void setTcpZoneCode(String tcpZoneCode) {
        this.tcpZoneCode = tcpZoneCode;
    }

    public int getClientVersion() {
        return clientVersion;
    }

    public void setClientVersion(int clientVersion) {
        this.clientVersion = clientVersion;
    }

    public int getAppID() {
        return appID;
    }

    public void setAppID(int appID) {
        this.appID = appID;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserZoneCode() {
        return userZoneCode;
    }

    public void setUserZoneCode(String userZoneCode) {
        this.userZoneCode = userZoneCode;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.DEFAULT_STYLE);
    }

    public String getNetworkType() {
        return networkType;
    }

    public void setNetworkType(String networkType) {
        this.networkType = networkType;
    }

    public String getChatRoomID() {
        return chatRoomID;
    }

    public void setChatRoomID(String chatRoomID) {
        this.chatRoomID = chatRoomID;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getKickId() {
        return kickId;
    }

    public void setKickId(String kickId) {
        this.kickId = kickId;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public Platform findPlatform() {
        if (StringUtils.isBlank(platform)) {
            return Platform.Mobile;
        }
        return Platform.getPlatform(platform);
    }


    public boolean isEmptyChatRoom() {
        return isEmptyChatRoom;
    }

    public void setEmptyChatRoom(boolean emptyChatRoom) {
        isEmptyChatRoom = emptyChatRoom;
    }

    public ChannelInfo copy() {
        ChannelInfo channelInfo = new ChannelInfo();
        channelInfo.setIp(this.ip);
        channelInfo.setPort(this.port);
        channelInfo.setUuid(this.uuid);
        channelInfo.setNodeId(this.nodeId);
        channelInfo.setChannelMode(this.channelMode);
        channelInfo.setId(this.id);
        channelInfo.setTcpZoneCode(this.tcpZoneCode);
        channelInfo.setSessonId(this.sessonId);
        channelInfo.setUserId(this.userId);
        channelInfo.setClientVersion(this.clientVersion);
        channelInfo.setAppID(this.appID);
        channelInfo.setUserZoneCode(this.userZoneCode);
        channelInfo.setNetworkType(this.networkType);
        channelInfo.setChatRoomID(this.chatRoomID);
        channelInfo.setEmptyChatRoom(this.isEmptyChatRoom());
        channelInfo.setCreateTime(this.createTime);
        return channelInfo;
    }

}
