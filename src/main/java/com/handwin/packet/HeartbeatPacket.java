package com.handwin.packet;

public class HeartbeatPacket extends CommandPacket {

	private static final long serialVersionUID = 2738513400182595528L;
	public final static int COMMAND_HEARTBEAT_TYPE = 0x03;
    public final static int COMMAND_HEARTBEAT_RESPONSE_TYPE = 0x04;
    public final static int COMMAND_HEARTBEAT_PACKET_TYPE = COMMAND_HEARTBEAT_TYPE*256 + COMMAND_PACKET_TYPE;
    
    private ChannelMode channelMode = ChannelMode.FOREGROUND;

    public HeartbeatPacket(){
        this.setPacketType(COMMAND_HEARTBEAT_PACKET_TYPE);
    }
    
    public ChannelMode getChannelMode() {
		return channelMode;
	}

    @Override
    public void attachThirdUserId(Integer appID) {

    }

    public void setChannelMode(ChannelMode channelMode) {
		this.channelMode = channelMode;
	}
    
}
