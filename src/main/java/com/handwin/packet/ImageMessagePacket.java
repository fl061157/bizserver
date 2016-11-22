package com.handwin.packet;

public class ImageMessagePacket extends SimpleMessagePacket {
    public final static int IMAGE_MESSAGE_PACKET_TYPE = IMAGE_URL_MESSAGE_TYPE*256 + SIMPLE_MESSAGE_PACKET_TYPE;

    public ImageMessagePacket(){
        this.setPacketType(IMAGE_MESSAGE_PACKET_TYPE);
    }
}
