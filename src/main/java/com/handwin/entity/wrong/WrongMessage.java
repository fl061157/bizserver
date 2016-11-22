package com.handwin.entity.wrong;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * @author fangliang
 */
public class WrongMessage {

    public final static int SIMPLE_WRONG_MESSAGE_TYPE = 1;
    public final static int CALL_WRONG_MESSAGE_TYPE = 2;

    protected int wrongMessageType;
    protected int wrongCount;
    protected int contentLength;
    protected String content;

    public int getContentLength() {
        return contentLength;
    }

    public int getWrongCount() {
        return wrongCount;
    }

    public String getContent() {
        return content ;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getWrongMessageType() {
        return wrongMessageType;
    }

    public void setContentLength(int contentLength) {
        this.contentLength = contentLength;
    }

    public void setWrongCount(int wrongCount) {
        this.wrongCount = wrongCount;
    }

    public void setWrongMessageType(int wrongMessageType) {
        this.wrongMessageType = wrongMessageType;
    }

    public void decode(ByteBuf buf) throws IOException {

    }


    public byte[] encode() throws IOException {
        ByteBuf buf = Unpooled.buffer();
        try {
            encode(buf);
            return buf.array();
        } finally {
            buf.release();
        }
    }


    public void encode(ByteBuf byteBuf) throws IOException {
        //Do Nothing !
    }


}
