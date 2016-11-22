package com.handwin.bean;

import java.util.Arrays;

/**
 * Created by piguangtao on 15/11/13.
 */
public class GroupCallInfo {
    private String groupId;
    private String roomId;
    private UDPServer[] udpServer;
    private Long startTime;
    private String fromUserId;

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public UDPServer[] getUdpServer() {
        return udpServer;
    }

    public void setUdpServer(UDPServer[] udpServer) {
        this.udpServer = udpServer;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public String getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("GroupCallInfo{");
        sb.append("roomId='").append(roomId).append('\'');
        sb.append(", udpServer=").append(Arrays.toString(udpServer));
        sb.append(", startTime=").append(startTime);
        sb.append(", fromUserId='").append(fromUserId).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
