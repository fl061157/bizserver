package com.handwin.packet;

public class StatusMessagePacket extends SimpleMessagePacket {
	
	private static final long serialVersionUID = 1212093680803862536L;
	public final static int STATUS_MESSAGE_PACKET_TYPE = STATUS_MESSAGE_TYPE*256 + SIMPLE_MESSAGE_PACKET_TYPE;

    public StatusMessagePacket(){
        this.setPacketType(STATUS_MESSAGE_PACKET_TYPE);
    }

    private MessageStatus messageStatus;

    private String cmsgId;

    public MessageStatus getMessageStatus() {
        return messageStatus;
    }

    public void setMessageStatus(MessageStatus messageStatus) {
        this.messageStatus = messageStatus;
    }

    public String getCmsgId() {
        return cmsgId;
    }

    public void setCmsgId(String cmsgId) {
        this.cmsgId = cmsgId;
    }
}
