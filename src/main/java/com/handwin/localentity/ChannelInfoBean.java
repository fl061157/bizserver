package com.handwin.localentity;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.EmbeddedId;
import info.archinnov.achilles.annotations.Entity;

/**
 * Created by fangliang on 5/1/15.
 */
@Entity(table = "user_channel", comment = "user tcp info")
public class ChannelInfoBean {

    @EmbeddedId
    private ChannelInfoKey id;

    @Column(name = "channel_mode")
    private int channelMode;

    @Column(name = "client_version")
    private int clientVersion;

    @Column(name = "id")
    private int tcpID;

    @Column(name = "ip")
    private String ip;

    @Column(name = "port")
    private int port;

    @Column(name = "session_id")
    private String sessionID;

    @Column(name = "tcp_zone_code")
    private String tcpZoneCode;

    @Column(name = "user_zone_code")
    private String userZoneCode;

    @Column(name = "node_id")
    private String nodeID ;


    public ChannelInfoKey getId() {
        return id;
    }

    public int getChannelMode() {
        return channelMode;
    }

    public int getClientVersion() {
        return clientVersion;
    }

    public int getTcpID() {
        return tcpID;
    }

    public String getIp() {
        return ip;
    }

    public String getSessionID() {
        return sessionID;
    }

    public int getPort() {
        return port;
    }

    public String getTcpZoneCode() {
        return tcpZoneCode;
    }

    public String getUserZoneCode() {
        return userZoneCode;
    }

    public String getNodeID() {
        return nodeID;
    }

    public void setId(ChannelInfoKey id) {
        this.id = id;
    }

    public void setChannelMode(int channelMode) {
        this.channelMode = channelMode;
    }

    public void setClientVersion(int clientVersion) {
        this.clientVersion = clientVersion;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setTcpID(int tcpID) {
        this.tcpID = tcpID;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setTcpZoneCode(String tcpZoneCode) {
        this.tcpZoneCode = tcpZoneCode;
    }

    public void setSessionID(String sessionID) {
        this.sessionID = sessionID;
    }

    public void setUserZoneCode(String userZoneCode) {
        this.userZoneCode = userZoneCode;
    }

    public void setNodeID(String nodeID) {
        this.nodeID = nodeID;
    }

    public ChannelInfoBean() {

    }

}
