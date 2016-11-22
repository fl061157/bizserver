package com.handwin.server;

import com.handwin.entity.ChannelInfo;
import com.handwin.exception.ChannelException;
import com.handwin.packet.BasePacket;
import com.handwin.packet.ChannelMode;
import com.handwin.server.proto.ChannelAction;

/**
 * @author fangliang
 */
public interface Channel {

    public void write(BasePacket packet) throws ChannelException;

    public void write(BasePacket packet, ChannelAction... tcpActions) throws ChannelException;

    public void write(BasePacket packet, byte[] extraBody, ChannelAction... tcpActions) throws ChannelException;

    public void write(byte[] packetBody, byte[] extraBody, ChannelAction... tcpActions) throws ChannelException;

    public void close() throws ChannelException;

    public void changeMode(ChannelMode channelMode);

    public String getIp();

    public int getPort();

    public ChannelInfo getChannelInfo();

    public String getTraceId();

    public boolean isActive();

}
