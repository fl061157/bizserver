package com.handwin.packet;

import com.datastax.driver.core.utils.Bytes;
import com.handwin.utils.UserUtils;
import org.apache.commons.lang.StringUtils;

public class UdpServerAckPacket extends CommandPacket {
    public final static int COMMAND_UDPSERVER_ACK_TYPE = 0x14;
    public final static int COMMAND_UDPSERVER_ACK_PACKET_TYPE = COMMAND_UDPSERVER_ACK_TYPE * 256 + COMMAND_PACKET_TYPE;

    private String peerName;

    private byte[] data;

    public UdpServerAckPacket() {
        super();
        super.commandType = COMMAND_UDPSERVER_ACK_TYPE;
        super.setPacketType(COMMAND_UDPSERVER_ACK_PACKET_TYPE);
    }

    public String getPeerName() {
        return peerName;
    }

    public void setPeerName(String peerName) {
        this.peerName = peerName;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public void attachThirdUserId(Integer appID) {

        if (StringUtils.isNotBlank(peerName)) {
            peerName = UserUtils.attachThirdUserID(peerName, appID);
        }

    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UdpServerAckPacket{");
        sb.append("peerName='").append(peerName).append('\'');
        sb.append(", data=").append(Bytes.toHexString(data));
        sb.append('}');
        return sb.toString();
    }
}
