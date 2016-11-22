package com.handwin.server;

import com.handwin.packet.BasePacket;

public interface ProxyMessageSender {

    public void write(String region, BasePacket basePacket);

    public void write(String region, byte[] messageBytes);

    public void writeStatus(String region, BasePacket basePacket);

    public void writeV5Protocol(String region, byte[] messageBytes);

}
