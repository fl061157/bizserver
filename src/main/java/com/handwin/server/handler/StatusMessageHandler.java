package com.handwin.server.handler;

import com.handwin.bean.Platform;
import com.handwin.entity.ChannelInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.handwin.packet.StatusMessagePacket;
import com.handwin.server.Channel;
import com.handwin.service.MessageService;

@Service
public class StatusMessageHandler extends AbstractHandler<StatusMessagePacket> implements InitializingBean {

    private static Logger logger = LoggerFactory.getLogger(StatusMessageHandler.class);

    @Autowired
    private MessageService messageService;

    public void afterPropertiesSet() throws Exception {
        register(StatusMessagePacket.class);
    }

    @Override
    public void handle(Channel channel, StatusMessagePacket p) {
        String userId = channel.getChannelInfo().getUserId();
        //状态发送到用户 登录地IDC 一定是出生地
        long messageId = p.getMessageId();
        if (logger.isDebugEnabled()) {
            logger.debug("[StatusMessageHandler:removeMessage],userId:{},messageId:{}", userId, messageId);
        }

        ChannelInfo channelInfo = channel.getChannelInfo();
        if (channelInfo != null && channelInfo.findPlatform() == Platform.Mobile) {
            messageService.removeMessage(userId, messageId);
        }

    }
}
