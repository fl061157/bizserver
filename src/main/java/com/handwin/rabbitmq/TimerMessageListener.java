package com.handwin.rabbitmq;

import com.handwin.packet.BasePacket;
import com.handwin.packet.SimpleMessagePacket;
import com.handwin.server.proto.FullProtoRequestMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by piguangtao on 2014/11/26.
 */
public class TimerMessageListener extends TcpMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(TcpMessageHandler.class);

    @Override
    public void map(final FullProtoRequestMessage requestMessage){
        logger.debug("enter TimerMessageListener");
        if (null != requestMessage) {
            BasePacket packet = requestMessage.getPacket();
            if (packet instanceof SimpleMessagePacket) {
                ((SimpleMessagePacket) packet).setMsgFlag((byte) 0x01);
            }
        }
    }
}
