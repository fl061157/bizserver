package com.handwin.service;

import com.handwin.entity.*;

/**
 * Created by piguangtao on 14-3-7.
 */
public interface IIpStrategyService {

    public UdpStrategy2Result getUdp(final UdpStrategyQuery query);

    public TcpStrategyResult getTcpServers(final TcpStrategyQuery query);

    public TraversingServerResult getTraversingServers(final TraversingServerQuery query);

}
