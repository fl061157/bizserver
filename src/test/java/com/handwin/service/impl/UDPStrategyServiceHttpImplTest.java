package com.handwin.service.impl;

import com.handwin.entity.UdpStrategy2Result;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class UDPStrategyServiceHttpImplTest {

    UDPStrategyServiceHttpImpl impl = new UDPStrategyServiceHttpImpl();

    String strategyResult = "{\"user1_udps\":[{\"udp_host\":\"114.215.193.49\",\"udp_port\":\"7001\",\"node_id\":\"u0000000001\"},{\"udp_host\":\"114.215.193.49\",\"udp_port\":\"7003\",\"node_id\":\"u0000000002\"}],\"user2_udps\":[{\"udp_host\":\"114.215.193.49\",\"udp_port\":\"7001\",\"node_id\":\"u0000000001\"},{\"udp_host\":\"114.215.193.49\",\"udp_port\":\"7003\",\"node_id\":\"u0000000002\"}],\"p2p\":17}";

    @Test
    public void testJson2UdpStrategy2Result() throws IOException {
        UdpStrategy2Result result = impl.objectMapper.readValue(strategyResult, UdpStrategy2Result.class);
        assertTrue(null != result);
        assertTrue(result.getP2p() ==17);
        System.out.println(result);
    }

}