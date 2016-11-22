package com.handwin.server.controller.livechat.cross;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

/**
 * Created by fangliang on 16/7/18.
 */
@Message
public class LiveChatMessage {

    @Index(0)
    private String from;

    @Index(1)
    private String fromRegion;

    @Index(3)
    private String roomID;

    @Index(4)
    private byte[] content;

    @Index(5)
    private String traceID;

    @Index(6)
    private boolean fromMain;

    @Index(7)
    private Integer appID;

    @Index(8)
    private String service;


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

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public boolean isFromMain() {
        return fromMain;
    }

    public void setFromMain(boolean fromMain) {
        this.fromMain = fromMain;
    }

    public Integer getAppID() {
        return appID;
    }

    public void setAppID(Integer appID) {
        this.appID = appID;
    }

    public String getTraceID() {
        return traceID;
    }

    public void setTraceID(String traceID) {
        this.traceID = traceID;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}
