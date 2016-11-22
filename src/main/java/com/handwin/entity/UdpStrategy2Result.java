package com.handwin.entity;

import java.util.Arrays;

/**
 * 
 * @author fangliang
 *
 */
public class UdpStrategy2Result {
	
	private byte p2p;

    private UDPInfo[] user1Udps;

    private UDPInfo[] user2Udps;

    public static class UDPInfo {
        private String udpHost;
        private String udpPort;
        private String nodeId;


        public UDPInfo() {
        }

        public UDPInfo(String udpHost, String udpPort, String nodeId) {
            this.udpHost = udpHost;
            this.udpPort = udpPort;
            this.nodeId = nodeId;
        }

        public String getUdpHost() {
            return udpHost;
        }

        public void setUdpHost(String udpHost) {
            this.udpHost = udpHost;
        }

        public String getUdpPort() {
            return udpPort;
        }

        public void setUdpPort(String udpPort) {
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
            return "UDPInfo{" +
                    "udpHost='" + udpHost + '\'' +
                    ", udpPort='" + udpPort + '\'' +
                    ", nodeId='" + nodeId + '\'' +
                    '}';
        }
    }


    public byte getP2p() {
        return p2p;
    }

    public void setP2p(byte p2p) {
        this.p2p = p2p;
    }

    public UDPInfo[] getUser1Udps() {
        return user1Udps;
    }

    public void setUser1Udps(UDPInfo[] user1Udps) {
        this.user1Udps = user1Udps;
    }

    public UDPInfo[] getUser2Udps() {
        return user2Udps;
    }

    public void setUser2Udps(UDPInfo[] user2Udps) {
        this.user2Udps = user2Udps;
    }

    @Override
    public String toString() {
        return "UdpStrategy2Result{" +
                "p2p=" + p2p +
                ", user1Udps=" + Arrays.toString(user1Udps) +
                ", user2Udps=" + Arrays.toString(user2Udps) +
                '}';
    }
}
