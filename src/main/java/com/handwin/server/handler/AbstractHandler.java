package com.handwin.server.handler;

import com.handwin.metric.MessageHandlerFilter;
import com.handwin.packet.BasePacket;
import com.handwin.server.ProxyMessageSender;
import com.handwin.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author fangliang
 */
public abstract class AbstractHandler<P extends BasePacket> extends MessageHandlerFilter<P> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractHandler.class);

    @Autowired
    private HandlerHolder<P> hodler;

    protected void register(Class<P> pCalss) {
        logger.info("register hander {} for packet {}", this, pCalss);
        hodler.putHandler(pCalss, this);
    }

    @Autowired
    protected UserService userService;

    @Autowired
    protected ProxyMessageSender proxyMessageSender;

}
