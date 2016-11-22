package com.handwin.service.impl;

import com.handwin.entity.*;
import com.handwin.service.IIpStrategyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by piguangtao on 15/3/20.
 */
public class DefaultStrategyServiceImpl implements IIpStrategyService {
    private static final Logger logger = LoggerFactory.getLogger(DefaultStrategyServiceImpl.class);

    @Override
    public UdpStrategy2Result getUdp(UdpStrategyQuery query) {
        logger.warn("should implement the getUdp method");
        return null;
    }

    @Override
    public TcpStrategyResult getTcpServers(TcpStrategyQuery query) {
        logger.warn("should implement the getTcpServers method");
        return null;
    }

    @Override
    public TraversingServerResult getTraversingServers(TraversingServerQuery query) {
        logger.warn("should implement the getTraversingServers method");
        return null;
    }
}
