package com.handwin.entity;

/**
 * Created by piguangtao on 14-1-20.
 */
public class PushCallBean extends PushBean {

    private CallType callType;

    private String roomId;

    public CallType getCallType() {
        return callType;
    }

    public void setCallType(CallType callType) {
        this.callType = callType;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PushCallBean{");
        sb.append("callType=").append(callType);
        sb.append(", roomId='").append(roomId).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
