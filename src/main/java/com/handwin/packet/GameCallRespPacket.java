package com.handwin.packet;

/**
 * Created by wyang on 2014/8/18.
 */
public class GameCallRespPacket extends GameCallPacket {
    public final static int COMMAND_GAME_CALL_RESP_PACKET_TYPE = COMMAND_GAME_CALL_TYPE * 256 + COMMAND_PACKET_TYPE;


    public GameCallRespPacket(){
        this.setPacketType(COMMAND_GAME_CALL_RESP_PACKET_TYPE);
    }
}
