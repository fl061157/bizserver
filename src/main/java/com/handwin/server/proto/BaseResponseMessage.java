package com.handwin.server.proto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.handwin.entity.ChannelInfo;

/**
 * @author fangliang
 */
public class BaseResponseMessage implements Serializable {

    private static final long serialVersionUID = 2359310472154248126L;

    private List<Integer> actions = new ArrayList<Integer>();
    private String tcpChannelUuid;
    private int tcpChannelId;
    private String traceId;
    private String userId;
    private int appId;
    private String sessionId;
    private boolean isLocalUser;
    private String userZonecode;
    private String tcpZonecode;
    private int channelMode;
    private String roomID;
    private String platform;

    public static BaseResponseMessage formResponseMessage(ChannelInfo channelInfo, String traceId) {
        BaseResponseMessage baseResponseMessage = new BaseResponseMessage();
        baseResponseMessage.setAppId(channelInfo.getAppID());
        baseResponseMessage.setChannelMode(channelInfo.getChannelMode().getValue());
        baseResponseMessage.setTcpChannelId(channelInfo.getId());
        baseResponseMessage.setTcpChannelUuid(channelInfo.getUuid());
        baseResponseMessage.setTcpZonecode(channelInfo.getTcpZoneCode());
        baseResponseMessage.setTraceId(traceId);
        baseResponseMessage.setUserId(channelInfo.getUserId());
        baseResponseMessage.setUserZonecode(channelInfo.getUserZoneCode());
        baseResponseMessage.setSessionId(channelInfo.getSessonId());
        baseResponseMessage.setRoomID(channelInfo.getChatRoomID());
        baseResponseMessage.setPlatform(channelInfo.getPlatform());
        return baseResponseMessage;
    }

    public BaseResponseMessage buildChannelUUID(String tcpChannelUUID) {
        this.tcpChannelUuid = tcpChannelUUID;
        return this;
    }

    public BaseResponseMessage buildUserZonecode(String userZonecode) {
        this.userZonecode = userZonecode;
        return this;
    }

    public BaseResponseMessage buildTcpZonecode(String tcpZonecode) {
        this.tcpZonecode = tcpZonecode;
        return this;
    }


    public BaseResponseMessage buildTcpChannelID(int tcpChannelID) {
        this.tcpChannelId = tcpChannelID;
        return this;
    }

    public BaseResponseMessage addAction(ChannelAction action) {
        actions.add(action.getAction());
        return this;
    }

    public BaseResponseMessage buildUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public BaseResponseMessage buildIsLocalUser(boolean isLocalUser) {
        this.isLocalUser = isLocalUser;
        return this;
    }

    public BaseResponseMessage buildChannelMode(int channelMode) {
        this.channelMode = channelMode;
        return this;
    }

    public BaseResponseMessage buildRoomID(String roomID) {
        this.roomID = roomID;
        return this;
    }


    public List<Integer> getActions() {
        return actions;
    }

    public String getTcpChannelUuid() {
        return tcpChannelUuid;
    }

    public void setTcpChannelUuid(String tcpChannelUuid) {
        this.tcpChannelUuid = tcpChannelUuid;
    }

    public int getTcpChannelId() {
        return tcpChannelId;
    }

    public void setTcpChannelId(int tcpChannelId) {
        this.tcpChannelId = tcpChannelId;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getAppId() {
        return appId;
    }

    public void setAppId(int appId) {
        this.appId = appId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public boolean isLocalUser() {
        return isLocalUser;
    }

    public void setLocalUser(boolean isLocalUser) {
        this.isLocalUser = isLocalUser;
    }

    public String getUserZonecode() {
        return userZonecode;
    }

    public void setUserZonecode(String userZonecode) {
        this.userZonecode = userZonecode;
    }

    public void setChannelMode(int channelMode) {
        this.channelMode = channelMode;
    }

    public int getChannelMode() {
        return channelMode;
    }

    public String getTcpZonecode() {
        return tcpZonecode;
    }

    public void setTcpZonecode(String tcpZonecode) {
        this.tcpZonecode = tcpZonecode;
    }

    public String getRoomID() {
        return roomID;
    }

    public void setRoomID(String roomID) {
        this.roomID = roomID;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }
}
