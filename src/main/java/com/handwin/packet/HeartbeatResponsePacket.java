package com.handwin.packet;

public class HeartbeatResponsePacket extends HeartbeatPacket {

    public final static int COMMAND_HEARTBEAT_RESPONSE_PACKET_TYPE = COMMAND_HEARTBEAT_RESPONSE_TYPE*256 + COMMAND_PACKET_TYPE;

    public HeartbeatResponsePacket(){
        this.setPacketType(COMMAND_HEARTBEAT_RESPONSE_PACKET_TYPE);
    }

    private long heartBeatSentTime;

    public long getHeartBeatSentTime() {
        return heartBeatSentTime;
    }

    public void setHeartBeatSentTime(long heartBeatSentTime) {
        this.heartBeatSentTime = heartBeatSentTime;
    }
}
