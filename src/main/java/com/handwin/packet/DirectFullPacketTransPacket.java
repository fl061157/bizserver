package com.handwin.packet;

import java.util.Arrays;


public class DirectFullPacketTransPacket extends AbstractBasePacket {

    /**
     * 消息包的消息头和消息体
     */
    private byte[] headAndBody;


    public DirectFullPacketTransPacket(byte[] headAndBody){
        this.setPacketType(DIRECT_TRANS_PACKET_TYPE);
        this.headAndBody = headAndBody;
    }

    public byte[] getHeadAndBody() {
        return headAndBody;
    }

    @Override
    public void attachThirdUserId(Integer appID) {

    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DirectFullPacketTransPacket{");
        sb.append("headAndBody=").append(Arrays.toString(headAndBody));
        sb.append(super.toString());
        sb.append('}');
        return sb.toString();
    }
}
