package com.handwin.entity.wrong;

import com.alibaba.fastjson.JSON;
import com.handwin.packet.CallStatus;
import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.nio.charset.Charset;


/**
 * @author fangliang
 */
public class CallWrongMessage extends WrongMessage {

    private WrongMessage wrongMessage;

    private String roomID;
    private int callStatus;
    private int screct;
    private int appID;
    private String from;
    private String to;
    private String traceId;



    public CallWrongMessage(WrongMessage wrongMessage) {
        this.wrongMessage = wrongMessage;
    }


    public static byte[] encode(int appID, String fromUserID, String toUserID, String roomID, CallStatus callStatus,
                                String traceID, int secrect) throws IOException {
        WrongMessage wrongMessage = new WrongMessage();
        wrongMessage.setWrongCount(0);
        wrongMessage.setWrongMessageType(WrongMessage.CALL_WRONG_MESSAGE_TYPE);
        CallWrongMessage callWrongMessage = new CallWrongMessage(wrongMessage);
        callWrongMessage.appID = appID;
        callWrongMessage.from = fromUserID;
        callWrongMessage.to = toUserID;
        callWrongMessage.callStatus = callStatus.id();
        callWrongMessage.roomID = roomID;
        callWrongMessage.traceId = traceID;
        callWrongMessage.screct = secrect;
        return callWrongMessage.encode();
    }


    @Override
    public void decode(ByteBuf buf) throws IOException {
        int contentLength = this.getContentLength();
        byte[] bytes = new byte[contentLength];
        buf.readBytes(bytes);
        String content = new String(bytes, Charset.defaultCharset());
//        CallWrongMessageContent callWrongMessageContent = JsonUtil.fromJson(content, CallWrongMessageContent.class);
        this.setContent( content );
        CallWrongMessageContent callWrongMessageContent = JSON.parseObject(content, CallWrongMessageContent.class);

        this.roomID = callWrongMessageContent.roomID;
        this.callStatus = callWrongMessageContent.callStatus;
        this.screct = callWrongMessageContent.screct;
        this.appID = callWrongMessageContent.appID;
        this.from = callWrongMessageContent.from;
        this.to = callWrongMessageContent.to;
        this.traceId = callWrongMessageContent.traceId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public void encode(ByteBuf byteBuf) throws IOException {
        byteBuf.writeInt(WrongMessage.CALL_WRONG_MESSAGE_TYPE);
        byteBuf.writeInt(this.getWrongCount());
        CallWrongMessageContent callWrongMessageContent = new CallWrongMessageContent();
        callWrongMessageContent.setAppID(appID);
        callWrongMessageContent.setCallStatus(callStatus);
        callWrongMessageContent.setFrom(from);
        callWrongMessageContent.setRoomID(roomID);
        callWrongMessageContent.setScrect(screct);
        callWrongMessageContent.setTo(to);
        callWrongMessageContent.setTraceId(traceId);
//        String json = JsonUtil.toJson(callWrongMessageContent);
        String json = JSON.toJSONString(callWrongMessageContent);
        byte[] content = json.getBytes(Charset.defaultCharset());
        byteBuf.writeInt(content.length);
        byteBuf.writeBytes(content);
    }

    @Override
    public int getContentLength() {
        return wrongMessage.getContentLength();
    }

    @Override
    public int getWrongCount() {
        return wrongMessage.getWrongCount();
    }

    @Override
    public void setWrongCount(int wrongCount) {
        wrongMessage.setWrongCount(wrongCount);
    }

    @Override
    public int getWrongMessageType() {
        return wrongMessage.getWrongMessageType();
    }

    public int getAppID() {
        return appID;
    }

    public int getCallStatus() {
        return callStatus;
    }

    public String getFrom() {
        return from;
    }

    public String getRoomID() {
        return roomID;
    }

    public int getScrect() {
        return screct;
    }

    public String getTo() {
        return to;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public void setAppID(int appID) {
        this.appID = appID;
    }

    public void setCallStatus(int callStatus) {
        this.callStatus = callStatus;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setRoomID(String roomID) {
        this.roomID = roomID;
    }

    public void setScrect(int screct) {
        this.screct = screct;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public static class CallWrongMessageContent {
        private String roomID;
        private int callStatus;
        private int screct;
        private int appID;
        private String from;
        private String to;
        private String traceId;

        public String getRoomID() {
            return roomID;
        }

        public void setRoomID(String roomID) {
            this.roomID = roomID;
        }

        public int getCallStatus() {
            return callStatus;
        }

        public void setCallStatus(int callStatus) {
            this.callStatus = callStatus;
        }

        public int getScrect() {
            return screct;
        }

        public void setScrect(int screct) {
            this.screct = screct;
        }

        public int getAppID() {
            return appID;
        }

        public void setAppID(int appID) {
            this.appID = appID;
        }

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getTo() {
            return to;
        }

        public void setTo(String to) {
            this.to = to;
        }

        public String getTraceId() {
            return traceId;
        }

        public void setTraceId(String traceId) {
            this.traceId = traceId;
        }

    }


}
