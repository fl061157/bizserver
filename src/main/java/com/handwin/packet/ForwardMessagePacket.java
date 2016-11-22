package com.handwin.packet;

public class ForwardMessagePacket extends SimpleMessagePacket {

    public final static int FORWARD_MESSAGE_PACKET_TYPE = FORWARD_MESSAGE_TYPE*256 + SIMPLE_MESSAGE_PACKET_TYPE;

    private byte[] data;
    private boolean both;

    public ForwardMessagePacket(){
        this.setPacketType(FORWARD_MESSAGE_PACKET_TYPE);
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public boolean isBoth() {
        return both;
    }

    public void setBoth(boolean both) {
        this.both = both;
    }
}
