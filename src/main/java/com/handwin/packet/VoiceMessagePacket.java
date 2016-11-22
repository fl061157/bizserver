package com.handwin.packet;

/**
 * Created with IntelliJ IDEA.
 * User: piguangtao
 * Date: 14-1-13
 * Time: 下午5:56
 * To change this template use File | Settings | File Templates.
 */
public class VoiceMessagePacket extends SimpleMessagePacket {

    public final static int VOICE_MESSAGE_PACKET_TYPE = VOICE_MESSAGE_TYPE*256 + SIMPLE_MESSAGE_PACKET_TYPE;

    public VoiceMessagePacket(){
        this.setPacketType(VOICE_MESSAGE_PACKET_TYPE);
    }
}
