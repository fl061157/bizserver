package com.handwin.server.controller.livechat.cross;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

/**
 * Created by fangliang on 16/7/18.
 */
@Message
public class LiveChatJoin {

    @Index(0)
    private String from;

    @Index(1)
    private String fromRegion;

    @Index(2)
    private String roomID;

    @Index(3)
    private String tcpNodeID;

    @Index(4)
    private String traceID;


    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getFromRegion() {
        return fromRegion;
    }

    public void setFromRegion(String fromRegion) {
        this.fromRegion = fromRegion;
    }

    public String getRoomID() {
        return roomID;
    }

    public void setRoomID(String roomID) {
        this.roomID = roomID;
    }

    public String getTraceID() {
        return traceID;
    }

    public void setTraceID(String traceID) {
        this.traceID = traceID;
    }

    public String getTcpNodeID() {
        return tcpNodeID;
    }

    public void setTcpNodeID(String tcpNodeID) {
        this.tcpNodeID = tcpNodeID;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}
