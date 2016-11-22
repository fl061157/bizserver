package com.handwin.server.controller;

import com.handwin.protocal.v5.codec.V5GenericPacket;
import com.handwin.protocal.v5.codec.V5PacketHead;
import com.handwin.server.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Created by Danny on 2014-12-06.
 */
@Service
@Interceptor("*")
public class LogInterceptor implements ServiceInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(LogInterceptor.class);

    @Override
    public boolean preHanle(Channel channel, V5PacketHead head, V5GenericPacket genericPacket) {
        logger.debug("receive service {}", head.getService());
        return true;
    }

    @Override
    public boolean postHanle(Channel channel, V5PacketHead head, V5GenericPacket genericPacket) {
        return true;
    }
}
