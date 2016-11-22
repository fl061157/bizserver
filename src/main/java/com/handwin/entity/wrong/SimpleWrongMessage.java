package com.handwin.entity.wrong;

import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * @author fangliang
 */
public class SimpleWrongMessage extends WrongMessage {


    private int appId;
    private long messageId;
    private String fromUserId;
    private String toUserId;
    private String traceId;

    private static final Logger logger = LoggerFactory.getLogger(SimpleWrongMessage.class);


    public static byte[] encode(int appID, String fromUserID, String toUserID, long messageID, String traceID) throws IOException {
        SimpleWrongMessage simpleWrongMessage = new SimpleWrongMessage();
        simpleWrongMessage.setWrongCount(0);
        simpleWrongMessage.setWrongMessageType(WrongMessage.SIMPLE_WRONG_MESSAGE_TYPE);
        simpleWrongMessage.setAppId(appID);
        simpleWrongMessage.fromUserId = fromUserID;
        simpleWrongMessage.toUserId = toUserID;
        simpleWrongMessage.messageId = messageID;
        simpleWrongMessage.traceId = traceID;
        return simpleWrongMessage.encode();
    }


    public void decode(ByteBuf buf) throws IOException {
        int contentLength = this.getContentLength();
        if (contentLength <= 0) {
            throw new IOException("contentLength <= 0 ");
        }
        byte[] bytes = new byte[contentLength];
        buf.readBytes(bytes);
        String content = new String(bytes, Charset.defaultCharset());
        this.setContent(content);
        try {
            SimpleWrongMessageContent simpleMessageContent = JSON.parseObject(content, SimpleWrongMessageContent.class);
            this.messageId = simpleMessageContent.messageId;
            this.fromUserId = simpleMessageContent.fromUserId;
            this.toUserId = simpleMessageContent.toUserId;
            this.appId = simpleMessageContent.appId;
            this.traceId = simpleMessageContent.traceId;
        } catch (Exception e) {
            logger.error("[SimpleWrong] decode error content:{}", content);
            throw new IOException("SimpleWrong decode error content: " + content);
        }
    }

    @Override
    public void encode(ByteBuf byteBuf) throws IOException {
        byteBuf.writeInt(WrongMessage.SIMPLE_WRONG_MESSAGE_TYPE);
        byteBuf.writeInt(this.getWrongCount());
        SimpleWrongMessageContent simpleWrongMessageContent = new SimpleWrongMessageContent();
        simpleWrongMessageContent.setAppId(appId);
        simpleWrongMessageContent.setFromUserId(fromUserId);
        simpleWrongMessageContent.setMessageId(messageId);
        simpleWrongMessageContent.setToUserId(toUserId);
        simpleWrongMessageContent.setTraceId(traceId);
//        String json = JsonUtil.toJson(simpleWrongMessageContent);
        String json = JSON.toJSONString(simpleWrongMessageContent);
        byte[] content = json.getBytes(Charset.defaultCharset());
        byteBuf.writeInt(content.length);
        byteBuf.writeBytes(content);
    }

    public long getMessageId() {
        return messageId;
    }

    public String getFromUserId() {
        return fromUserId;
    }

    public String getToUserId() {
        return toUserId;
    }

    public int getAppId() {
        return appId;
    }

    public void setAppId(int appId) {
        this.appId = appId;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }


    public static class SimpleWrongMessageContent {
        private int appId;
        private long messageId;
        private String fromUserId;
        private String toUserId;
        private String traceId;
        private String content;

        public void setFromUserId(String fromUserId) {
            this.fromUserId = fromUserId;
        }

        public void setMessageId(long messageId) {
            this.messageId = messageId;
        }

        public void setToUserId(String toUserId) {
            this.toUserId = toUserId;
        }

        public int getAppId() {
            return appId;
        }

        public void setAppId(int appId) {
            this.appId = appId;
        }

        public String getTraceId() {
            return traceId;
        }

        public void setTraceId(String traceId) {
            this.traceId = traceId;
        }

        public long getMessageId() {
            return messageId;
        }

        public String getFromUserId() {
            return fromUserId;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getToUserId() {
            return toUserId;
        }

    }


}
