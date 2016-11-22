package com.handwin.server.handler;

import com.handwin.packet.BasePacket;
import com.handwin.server.Channel;

/**
 * @param <P>
 * @author fangliang
 */
public interface Handler<P extends BasePacket> {

    public void before(String traceId,P packet);

    public void after(String traceId,P packet);

    public void handle(Channel channel, P packet);
}
