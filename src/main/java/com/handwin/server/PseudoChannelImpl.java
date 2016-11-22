package com.handwin.server;

import com.handwin.entity.ChannelInfo;
import com.handwin.packet.BasePacket;
import com.handwin.packet.ChannelMode;
import com.handwin.server.proto.ChannelAction;

/**
 * Created by piguangtao on 15/12/17.
 */
public class PseudoChannelImpl extends AbstractChannelImpl {

    public PseudoChannelImpl(String traceId, ChannelInfo channelInfo) {
        super(traceId, null);
        this.channelInfo = channelInfo;
    }

    @Override
    public void write(BasePacket packet, byte[] extraBody,
                      ChannelAction... tcpActions) {
        //ignore
    }

    @Override
    public void write(byte[] packetBody, ChannelAction... tcpActions) {
        //ignore
//        throw new ServerException("not supported.");
    }

    @Override
    public void write(byte[] packetBody, byte[] extraBody, ChannelAction... tcpActions) {
        //ignore
//        throw new ServerException("not supported.");
    }

    @Override
    public void close() {
        //ignore
    }

    @Override
    public void changeMode(ChannelMode channelMode) {
        //ignore
    }

    @Override
    public boolean isActive() {
        return true;
    }
}
