package com.handwin.server.controller;

import com.handwin.packet.GenericPacket;
import com.handwin.protocal.v5.codec.V5GenericPacket;
import com.handwin.protocal.v5.codec.V5PacketHead;
import com.handwin.server.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Created by Danny on 2014-12-07.
 */
@Service
public class EchoServiceController implements ServiceController {

    private static final Logger logger = LoggerFactory.getLogger(EchoServiceController.class);

    @Override
    public void handle(Channel channel, V5PacketHead packetHead, V5GenericPacket genericPacket) {
        logger.debug("echo to {}", packetHead.getTo());
        logger.debug("generic packet : {}", genericPacket);
        GenericPacket packet = new GenericPacket();
        channel.write(packet);
    }

    @Controller
    public void echo2Handle(Channel channel, V5PacketHead packetHead, Map map) {
        logger.debug("echo2");
    }

    @Controller("echo33")
    public void echo3Handle(Channel channel, V5PacketHead packetHead, Map map) {
        logger.debug("echo3");
    }

}
