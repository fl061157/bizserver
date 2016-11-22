package com.handwin.packet;

/**
 * Created with IntelliJ IDEA.
 * User: piguangtao
 * Date: 14-1-13
 * Time: 下午6:33
 * To change this template use File | Settings | File Templates.
 */
public class VideoMessagePacket extends SimpleMessagePacket {

    public final static int VIDEO_MESSAGE_PACKET_TYPE = VIDEO_MESSAGE_TYPE*256 + SIMPLE_MESSAGE_PACKET_TYPE;

    public VideoMessagePacket(){
        this.setPacketType(VIDEO_MESSAGE_PACKET_TYPE);
    }
}
