package com.handwin.service.impl;

import com.handwin.codec.PacketCodecs;
import com.handwin.entity.ChannelInfo;
import com.handwin.entity.User;
import com.handwin.server.AsyncChannelImpl;
import com.handwin.server.Channel;
import com.handwin.server.proto.BaseRequestMessage;
import com.handwin.service.TcpMessageDispatcher;
import com.handwin.server.ChannelStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by fangliang on 14/5/15.
 */

@Service
public class ChannelFactory {


    @Autowired
    private PacketCodecs packetCodecs;

//    @Autowired
//    private ChannelStrategyService channelStrategyService;

    @Autowired
    private TcpMessageDispatcher tcpMessageDispatcher;

    public Channel createChannel(ChannelStrategy channelStrategy, BaseRequestMessage baseRequest) {
        return new AsyncChannelImpl(baseRequest, packetCodecs, tcpMessageDispatcher, channelStrategy);
    }

    public Channel createChannel(ChannelStrategy channelStrategy, User user, ChannelInfo channelInfo) {
        return new AsyncChannelImpl(user, channelInfo, packetCodecs, tcpMessageDispatcher, channelStrategy);
    }


    public Channel createChannel(ChannelInfo channelInfo) {
        return new AsyncChannelImpl(channelInfo, packetCodecs, tcpMessageDispatcher, ChannelStrategy.MrChannel);
    }

}
