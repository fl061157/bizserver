package com.handwin.entity;

/**
 * Created by piguangtao on 14-3-7.
 */
public class UdpStrategyResult {

    /**
     * 是否使用p2p
     */
    private byte p2p;

    private String udpHost;

    private int udpPort;

    private String nodeId;

    public byte getP2p() {
        return p2p;
    }

    public void setP2p(byte p2p) {
        this.p2p = p2p;
    }

    public String getUdpHost() {
        return udpHost;
    }

    public void setUdpHost(String udpHost) {
        this.udpHost = udpHost;
    }

    public int getUdpPort() {
        return udpPort;
    }

    public void setUdpPort(int udpPort) {
        this.udpPort = udpPort;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UdpStrategyResult{");
        sb.append("p2p=").append(p2p);
        sb.append(", udpHost='").append(udpHost).append('\'');
        sb.append(", udpPort=").append(udpPort);
        sb.append('}');
        return sb.toString();
    }
}
