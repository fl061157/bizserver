package com.handwin.server.controller;

import com.handwin.genericmap.GMapUtils;
import com.handwin.packet.ChannelMode;
import com.handwin.packet.HeartbeatPacket;
import com.handwin.protocal.v5.codec.V5GenericPacket;
import com.handwin.protocal.v5.codec.V5PacketHead;
import com.handwin.server.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Created by Danny on 2014-12-03.
 */
@Service
public class HeartbeatServiceController extends AbstractProxyServiceController<HeartbeatPacket> {

    private static final Logger logger = LoggerFactory.getLogger(HeartbeatServiceController.class);

    @Override
    public void handle(Channel channel, V5PacketHead packetHead, V5GenericPacket genericPacket) {
        logger.debug("heartbeat from {}", packetHead.getFrom());
        HeartbeatPacket heartbeatPacket = new HeartbeatPacket();
        int modeId = GMapUtils.getInt(genericPacket.getBodyMap(), "mode");
        heartbeatPacket.setChannelMode(ChannelMode.getInstance((byte) modeId));
        v1Handle(channel, heartbeatPacket);
    }
}
