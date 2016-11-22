package com.handwin.packet;

import java.io.Serializable;

public interface BasePacket extends Serializable {

    byte DIRECT_TRANS_PACKET_TYPE = -99;

    int getPacketType();

    void setPacketType(int type);

    PacketHead getPacketHead();

    void setPacketHead(PacketHead head);

    byte[] getSrcMsgBytes();

    void setSrcMsgBytes(byte[] srcMsgBytes);

    String getTraceId();

    public void setTraceId(String traceId);

    public void attachThirdUserId(Integer appID);


}
