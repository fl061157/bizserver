package com.handwin.server.proto;

import com.chatgame.protobuf.TcpBiz;
import com.handwin.packet.BasePacket;

/**
 * Created by piguangtao on 2014/11/27.
 */
public class FullProtoRequestMessage {
    /**
     * proto协议的头
     */
    private BaseRequestMessage baseRequestMessage;

    /**
     * proto协议体（业务协议）解码后的包
     */
    private BasePacket packet;


    private TcpBiz.Tcp2BizReq protoMessage;

    public FullProtoRequestMessage(BaseRequestMessage baseRequestMessage, BasePacket packet, TcpBiz.Tcp2BizReq protoMessage) {
        this.baseRequestMessage = baseRequestMessage;
        this.packet = packet;
        this.protoMessage = protoMessage;
    }

    public BaseRequestMessage getBaseRequestMessage() {
        return baseRequestMessage;
    }

    public void setBaseRequestMessage(BaseRequestMessage baseRequestMessage) {
        this.baseRequestMessage = baseRequestMessage;
    }

    public BasePacket getPacket() {
        return packet;
    }

    public void setPacket(BasePacket packet) {
        this.packet = packet;
    }

    public TcpBiz.Tcp2BizReq getProtoMessage() {
        return protoMessage;
    }

    public void setProtoMessage(TcpBiz.Tcp2BizReq protoMessage) {
        this.protoMessage = protoMessage;
    }
}
