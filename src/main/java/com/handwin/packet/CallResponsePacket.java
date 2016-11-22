package com.handwin.packet;

public class CallResponsePacket extends CallPacket{

    public final static int COMMAND_CALL_RESPONSE_PACKET_TYPE = COMMAND_CALL_RESPONSE_TYPE*256 + COMMAND_PACKET_TYPE;

    public CallResponsePacket(){
        this.setPacketType(COMMAND_CALL_RESPONSE_PACKET_TYPE);
    }

    /*
    private Integer status;

    private String roomId;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
    */

    @Override
    public String toString() {
        return "CallResponsePacket{" +
                "status=" + status +
                ", roomId='" + roomId + '\'' +
                "} " + super.toString();
    }
}
