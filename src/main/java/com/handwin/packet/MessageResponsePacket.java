package com.handwin.packet;

public class MessageResponsePacket extends SimpleMessagePacket {

    private static final long serialVersionUID = 4148449861626375696L;
    public final static int MESSAGE_RESPONSE_PACKET_TYPE = STATUS_RESPONSE_MESSAGE_TYPE * 256 + SIMPLE_MESSAGE_PACKET_TYPE;

    public MessageResponsePacket() {
        this.setPacketType(MESSAGE_RESPONSE_PACKET_TYPE);
    }

    private MessageStatus messageStatus;

    /**
     * 该包来源IDC的国家码
     * 在BizServer之间转发需要使用，tcp客户端不需要
     */
    private String fromIdcCountryCode;

    public MessageStatus getMessageStatus() {
        return messageStatus;
    }

    public void setMessageStatus(MessageStatus messageStatus) {
        this.messageStatus = messageStatus;
    }

    public String getFromIdcCountryCode() {
        return fromIdcCountryCode;
    }

    public void setFromIdcCountryCode(String fromIdcCountryCode) {
        this.fromIdcCountryCode = fromIdcCountryCode;
    }

    @Override
    public String toString() {
        return "MessageResponsePacket{" +
                "messageStatus=" + messageStatus +
                ", fromIdcCountryCode='" + fromIdcCountryCode + '\'' +
                "} " + super.toString();
    }
}
