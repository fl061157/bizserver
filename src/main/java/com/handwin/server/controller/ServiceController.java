package com.handwin.server.controller;

import com.handwin.protocal.v5.codec.V5GenericPacket;
import com.handwin.protocal.v5.codec.V5PacketHead;
import com.handwin.server.Channel;

/**
 * Created by Danny on 2014-12-03.
 */
public interface ServiceController {
    void handle(Channel channel, V5PacketHead packetHead, V5GenericPacket genericPacket);
}
