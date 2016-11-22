package com.handwin.packet;

import com.handwin.entity.UdpStrategy2Result;

import java.util.Arrays;


public class UDPServerPacket extends CommandPacket {

    public final static int COMMAND_UDP_TYPE = 0x13;

    public final static int COMMAND_UDP_PACKET_TYPE = COMMAND_UDP_TYPE * 256 + COMMAND_PACKET_TYPE;

    private byte flag;

    private String roomId;

    private UdpInfo[] udpInfo;

    public static class UdpInfo{

        private String ip;

        private int port;

        /**
         * udp server的节点id
         */
        private String nodeId;

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getNodeId() {
            return nodeId;
        }

        public void setNodeId(String nodeId) {
            this.nodeId = nodeId;
        }


        public UdpInfo() {
        }

        public UdpInfo(String ip, int port, String nodeId) {
            this.ip = ip;
            this.port = port;
            this.nodeId = nodeId;
        }

        @Override
        public String toString() {
            return "UdpInfo{" +
                    "ip='" + ip + '\'' +
                    ", port=" + port +
                    ", nodeId='" + nodeId + '\'' +
                    '}';
        }
    }

    public UDPServerPacket(){
        this.setPacketType(COMMAND_UDP_PACKET_TYPE);
    }


    public byte getFlag() {
        return flag;
    }

    public void setFlag(byte flag) {
        this.flag = flag;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }


    public UdpInfo[] getUdpInfo() {
        return udpInfo;
    }

    public void setUdpInfo(UdpInfo[] udpInfo) {
        this.udpInfo = udpInfo;
    }


    @Override
    public void attachThirdUserId(Integer appID) {

    }

    @Override
    public String toString() {
        return "UDPServerPacket{" +
                "flag=" + flag +
                ", roomId='" + roomId + '\'' +
                ", udpInfo=" + Arrays.toString(udpInfo) +
                '}';
    }
}