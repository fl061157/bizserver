package com.handwin.server.handler;

import com.handwin.bean.Platform;
import com.handwin.entity.ChannelInfo;
import com.handwin.packet.ChannelMode;
import com.handwin.packet.LogoutPacket;
import com.handwin.server.Channel;
import com.handwin.service.LiveChatService;
import com.handwin.service.TcpSessionService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LogoutHandler extends AbstractHandler<LogoutPacket> implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(LogoutHandler.class);

    @Autowired
    private TcpSessionService onlineStatusService;

    @Autowired
    private LiveChatService liveChatService;

    public void afterPropertiesSet() throws Exception {
        register(LogoutPacket.class);
    }

    @Override
    public void handle(Channel channel, LogoutPacket packet) {
        ChannelInfo fromChannelInfo = channel.getChannelInfo();
        String fromUserID = fromChannelInfo.getUserId();
        int fromUserAppID = fromChannelInfo.getAppID();
        //登陆地 一定是出生地 登出也一样
        ChannelMode channelMode = packet.getChannelMode();
        boolean remove = (channelMode == null) || channelMode == ChannelMode.UNKNOWN;

        if (logger.isInfoEnabled()) {
            logger.info("[Logout] channelInfo:{}", fromChannelInfo);
        }
        fromChannelInfo.setChannelMode(channelMode);
        if (remove) {
            onlineStatusService.delChannel(fromUserID, fromUserAppID,
                    fromChannelInfo.getUuid(), fromChannelInfo.getNodeId());

            //TODO 这如何处理  手机端 Or Web端 退出

            if (fromChannelInfo.findPlatform() == Platform.Mobile || fromChannelInfo.findPlatform() == Platform.Web) {
                String chatRoomID = fromChannelInfo.getChatRoomID();
                if (logger.isDebugEnabled()) {
                    logger.debug("[Logout LeaveChatRoom] chatRoomID:{} , fromUserID:{} ", chatRoomID, fromUserID);
                }
                if (StringUtils.isNotBlank(chatRoomID)) {
                    String tcpNode = fromChannelInfo.getNodeId();
                    liveChatService.leaveRoom(chatRoomID, tcpNode, fromUserID);
                }
            }


        } else {
            final int ttl = onlineStatusService.getChannelModeHeartbeatTime(channelMode);
            onlineStatusService.changeChannelMode(fromChannelInfo, fromUserID, fromUserAppID, ttl);
        }
    }
}
