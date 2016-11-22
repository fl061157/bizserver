package com.handwin.server.handler;

import com.handwin.entity.User;
import com.handwin.exception.ServerException;
import com.handwin.packet.UdpServerAckPacket;
import com.handwin.server.Channel;
import com.handwin.service.ChannelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UdpServerAckHandler extends AbstractHandler<UdpServerAckPacket>
        implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(UdpServerAckHandler.class);

    @Autowired
    private ChannelService channelService;

    public void afterPropertiesSet() throws Exception {
        register(UdpServerAckPacket.class);
    }

    @Override
    public void handle(Channel channel, UdpServerAckPacket packet) {
        logger.debug("receive udp server ack. channel:{},packet:{}", channel, packet);

        String fromUserId = channel.getChannelInfo().getUserId();
        String toUserId = packet.getPeerName();
        User toUser;
        try {
            toUser = userService.findById(toUserId,channel.getChannelInfo().getAppID());
        } catch (ServerException e) {
            logger.error("[CallHandler] findUser user.id:{} to.id:{} , error:{}", fromUserId, toUserId);
            return;
        }

        UdpServerAckPacket transUdpServerAckPacket = new UdpServerAckPacket();
        transUdpServerAckPacket.setPeerName(fromUserId);
        transUdpServerAckPacket.setData(packet.getData());

        Channel toChannel = channelService.findChannel(toUser);
        if (toChannel != null) {
            toChannel.write(transUdpServerAckPacket);
            logger.debug("trans udp server ack. toChannel:{},packet:{}", toChannel, transUdpServerAckPacket);
        }
    }
}
