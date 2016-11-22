package com.handwin.server.controller;

import com.handwin.packet.BasePacket;
import com.handwin.server.Channel;
import com.handwin.server.handler.Handler;
import com.handwin.server.handler.HandlerHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by Danny on 2014-12-03.
 */
public abstract class AbstractProxyServiceController<P extends BasePacket> implements ServiceController {

    private static final Logger logger = LoggerFactory.getLogger(AbstractProxyServiceController.class);

    @Autowired
    private HandlerHolder hodler;

    protected void v1Handle(Channel channel, P basePacket){
        Handler handler = hodler.getHandler(basePacket.getClass());
        if (handler != null) {
            handler.handle(channel, basePacket);
        } else {
            logger.error("No handler for {}", basePacket.getClass());
        }
    }
}
