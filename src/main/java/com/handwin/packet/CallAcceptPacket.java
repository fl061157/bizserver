package com.handwin.packet;

public class CallAcceptPacket extends CallPacket {

    private int udpHost;

    private short udpPort;

    public int getUdpHost() {
        return udpHost;
    }

    public void setUdpHost(int udpHost) {
        this.udpHost = udpHost;
    }

    public short getUdpPort() {
        return udpPort;
    }

    public void setUdpPort(short udpPort) {
        this.udpPort = udpPort;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CallAcceptPacket{");
        sb.append("udpHost=").append(udpHost);
        sb.append(", udpPort=").append(udpPort);
        sb.append(super.toString());
        sb.append('}');
        return sb.toString();
    }

}