package com.handwin.entity;

import java.util.Arrays;

/**
 * Created by piguangtao on 14-1-17.
 */
public class CallRoom {


    private String appId;

    private String roomId;

    private String[] member;

    private String udpServer;

    /**
     * udp server所在的节点id
     */
    private String nodeId;

    public String[] getMember() {
        return member;
    }

    public void setMember(String[] member) {
        this.member = member;
    }

    public String getUdpServer() {
        return udpServer;
    }

    public void setUdpServer(String udpServer) {
        this.udpServer = udpServer;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CallRoom{");
        sb.append("appId='").append(appId).append('\'');
        sb.append(", roomId='").append(roomId).append('\'');
        sb.append(", member=").append(Arrays.toString(member));
        sb.append(", udpServer='").append(udpServer).append('\'');
        sb.append(", nodeId='").append(nodeId).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
