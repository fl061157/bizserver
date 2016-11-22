package com.handwin.server.handler;

import com.handwin.entity.ChannelInfo;
import com.handwin.entity.User;
import com.handwin.exception.ServerException;
import com.handwin.packet.ChannelMode;
import com.handwin.packet.ForwardMessagePacket;
import com.handwin.packet.PacketHead;
import com.handwin.server.Channel;
import com.handwin.service.ChannelService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 先不支持群组转发
 */
@Service
public class ForwardMessageHandler extends AbstractHandler<ForwardMessagePacket> implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(ForwardMessageHandler.class);

    @Autowired
    private ChannelService channelService;


    public void afterPropertiesSet() throws Exception {
        register(ForwardMessagePacket.class);
    }

    @Override
    public void handle(Channel channel, ForwardMessagePacket p) {
        String fromUserId = channel.getChannelInfo().getUserId();
        User user;
        try {
            user = userService.findById(p.getToUser(), channel.getChannelInfo().getAppID());
        } catch (ServerException e) {
            logger.error("Forward findUser user.id:{} , error:{}", p.getToUser(), e);
            return;
        }

        if (user == null) {
            logger.error("Forward findUser user.id:{} empty", p.getToUser());
            return;
        }

        boolean isSystemAccount = userService.isSystemAccount(user);

        if (!isSystemAccount && StringUtils.isBlank(user.getCountrycode())) {
            logger.warn("Cant not forward message to empty region user.id:{}", user.getId());
            return;
        }


        if (!isSystemAccount && !userService.isLocalUser(user.getCountrycode())) {
            if (logger.isDebugEnabled()) {
                logger.debug("forward  forwardMessage to  user.id:{} ", user.getId());
            }
            proxyMessageSender.write(user.getCountrycode(), p);
            return;
        }

        Channel toChannel;
        toChannel = channelService.findChannel(user);
        ChannelInfo toChannelInfo;
        if (toChannel != null && (toChannelInfo = toChannel.getChannelInfo()) != null &&
                ChannelMode.SUSPEND != toChannelInfo.getChannelMode()) {
            ForwardMessagePacket forwardMessagePacket =
                    createPacket(fromUserId, p.isBoth(), p.getPacketHead().getSecret(), p.getData(), p.getCmsgid());
            toChannel.write(forwardMessagePacket);
            if (p.isBoth()) {
                channel.write(forwardMessagePacket);
            }
        } else {
            if (logger.isInfoEnabled()) {
                logger.info("Forward User Not Online user.id:{} ", user.getId());
            }
        }

    }

    private ForwardMessagePacket createPacket(String from, boolean both, byte secret, byte[] data, String cmsgid) {
        ForwardMessagePacket packet = new ForwardMessagePacket();
        packet.setData(data);
        packet.setFrom(from);
        packet.setBoth(both);
        packet.setCmsgid(cmsgid);
        PacketHead head = new PacketHead();
        head.setSecret(secret);
        packet.setPacketHead(head);
        return packet;
    }
}
