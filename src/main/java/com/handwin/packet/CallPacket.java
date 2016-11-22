package com.handwin.packet;

import com.handwin.utils.UserUtils;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;

public class CallPacket extends CommandPacket {
    public final static int COMMAND_CALL_TYPE = 0x11;
    public final static int COMMAND_CALL_RESPONSE_TYPE = 0x12;
    public final static int COMMAND_CALL_PACKET_TYPE = COMMAND_CALL_TYPE * 256 + COMMAND_PACKET_TYPE;
    public final static String CALL_ROOM_ID_PREFIX = "call_";

    private String peerName;
    private CallStatus callStatus;
    private byte[] userData;

    protected Integer status;

    protected String roomId;

    public CallPacket() {
        this.setPacketType(COMMAND_CALL_PACKET_TYPE);
    }


    @Override
    public void attachThirdUserId(Integer appID) {
        if (StringUtils.isNotBlank(peerName)) {
            peerName = UserUtils.attachThirdUserID(peerName, appID);
        }
    }

    public String getPeerName() {
        return peerName;
    }

    public void setPeerName(String peerName) {
        this.peerName = peerName;
    }

    public CallStatus getCallStatus() {
        return callStatus;
    }

    public void setCallStatus(CallStatus callStatus) {
        this.callStatus = callStatus;
    }

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

    public String getContent() {
        return "";
    }

    public byte[] getUserData() {
        return userData;
    }

    public void setUserData(byte[] userData) {
        this.userData = userData;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CallPacket{");
        sb.append("peerName='").append(peerName).append('\'');
        sb.append(", callStatus=").append(callStatus);
        sb.append(", status=").append(status);
        sb.append(", roomId='").append(roomId).append('\'');
        sb.append(", extraData=").append(Arrays.toString(userData));
        sb.append('}');
        return sb.toString();
    }
}
