package com.handwin.packet;

public class TextMessagePacket extends SimpleMessagePacket {
    public final static int TEXT_MESSAGE_PACKET_TYPE = TEXT_MESSAGE_TYPE*256 + SIMPLE_MESSAGE_PACKET_TYPE;

    public TextMessagePacket(){
        this.setPacketType(TEXT_MESSAGE_PACKET_TYPE);
    }
}
