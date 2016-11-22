package com.handwin.server.handler;

import com.handwin.entity.ChannelInfo;
import com.handwin.packet.GenericPacket;
import com.handwin.protocal.v5.codec.V5PacketHead;
import com.handwin.server.Channel;
import com.handwin.server.controller.ServiceControllerManager;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Danny on 2014-12-03.
 */
@Service
public class GenericMessageHandler extends AbstractHandler<GenericPacket> implements
        InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(GenericMessageHandler.class);

    @Autowired
    ServiceControllerManager serviceControllerManager;

    public void afterPropertiesSet() throws Exception {
        register(GenericPacket.class);
    }

    @Override
    public void handle(Channel channel, GenericPacket p) {
        V5PacketHead head = p.getV5GenericPacket().getPacketHead();
        ChannelInfo channelInfo = channel.getChannelInfo();

        if (StringUtils.isBlank(head.getFrom()) && StringUtils.isNotBlank(channelInfo.getUserId())) {
            head.setFrom(channelInfo.getUserId());
        }

        if (StringUtils.isBlank(head.getFromRegion()) && StringUtils.isNotBlank(channelInfo.getUserZoneCode())) {
            head.setFromRegion( channelInfo.getUserZoneCode() );
        }
        serviceControllerManager.handler(channel, p.getV5GenericPacket());
    }
}
