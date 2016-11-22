package com.handwin.packet;


import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public abstract class AbstractBasePacket implements BasePacket, Cloneable {

    private static final long serialVersionUID = -6113969695717121915L;
    protected int packetType;
    protected PacketHead packetHead;
    private byte[] srcMsgBytes;
    private String traceId;

    public AbstractBasePacket() {

    }

    public int getPacketType() {
        return packetType;
    }

    public void setPacketType(int packetType) {
        this.packetType = packetType;
    }

    public PacketHead getPacketHead() {
        return packetHead;
    }

    public void setPacketHead(PacketHead packetHead) {
        this.packetHead = packetHead;
    }

    public byte[] getSrcMsgBytes() {
        return srcMsgBytes;
    }

    public void setSrcMsgBytes(byte[] srcMsgBytes) {
        this.srcMsgBytes = srcMsgBytes;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this, false);
    }

}
