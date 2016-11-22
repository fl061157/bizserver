package com.handwin.server.controller;

import com.handwin.protocal.v5.codec.V5GenericPacket;
import com.handwin.protocal.v5.codec.V5PacketHead;
import com.handwin.server.Channel;

/**
 * Created by Danny on 2014-12-06.
 */
public interface ServiceInterceptor {
    boolean preHanle(Channel channel, V5PacketHead head, V5GenericPacket genericPacket);
    boolean postHanle(Channel channel, V5PacketHead head, V5GenericPacket genericPacket);
}
