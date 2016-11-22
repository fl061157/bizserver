package com.handwin.packet;


public class LogoutPacket extends CommandPacket {

    public final static int COMMAND_LOGOUT_TYPE = 0x05;
    public final static int COMMAND_LOGOUT_PACKET_TYPE = COMMAND_LOGOUT_TYPE*256 + COMMAND_PACKET_TYPE;

    private ChannelMode channelMode;

    
    public LogoutPacket(){
        this.setPacketType(COMMAND_LOGOUT_PACKET_TYPE);
    }
    
    public ChannelMode getChannelMode() {
        return channelMode;
    }

    public void setChannelMode(ChannelMode channelMode) {
        this.channelMode = channelMode;
    }

    @Override
    public void attachThirdUserId(Integer appID) {

    }

    @Override
    public String toString() {
        return "LogoutPacket{" +
                "channelMode=" + channelMode +
                "} " + super.toString();
    }
    
}
