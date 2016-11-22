package com.handwin.server.handler;

import com.handwin.entity.ChannelInfo;
import com.handwin.packet.ChannelMode;
import com.handwin.packet.HeartbeatPacket;
import com.handwin.server.Channel;
import com.handwin.service.TcpSessionService;
import com.handwin.service.impl.ChannelFactory;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HeartbeatHandler extends AbstractHandler<HeartbeatPacket> implements
        InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(HeartbeatHandler.class);

    @Autowired
    private TcpSessionService onlineStatusService;

    @Autowired
    private ChannelFactory channelFactory;


    public void afterPropertiesSet() throws Exception {
        register(HeartbeatPacket.class);
    }

    @Override
    public void handle(Channel channel, HeartbeatPacket p) {
        ChannelInfo channelInfo = channel.getChannelInfo();
        if (channelInfo == null || StringUtils.isBlank(channelInfo.getUserId())) {
            logger.error("channelInfo or channelInfo.userId is null  ");
            return;
        }
        //出生地 一定是 登录地
        ChannelMode channelMode = p.getChannelMode();

        int ttl = onlineStatusService.getChannelModeHeartbeatTime(channelMode);
        channelInfo.setChannelMode(channelMode);

//        if (logger.isInfoEnabled()) {
//            logger.info("Heartbeat userID:{} , channelUUID:{} , channelMode:{}", channelInfo.getUserId(), channelInfo.getUuid(), channelMode.name());
//        }

//        if (!channelInfo.getUserId().startsWith("88888888")) {
//            List<ChannelInfo> channelInfoList = onlineStatusService.findChannelInfo(channelInfo.getUserId(), 0);
//            if (channelInfoList != null && channelInfoList.size() > 0) {
//                channelInfoList.stream().filter(ci -> !ci.getUuid().equals(channelInfo.getUuid()))
//                        .forEach(cio -> {
//                            try {
//                                Channel cl = channelFactory.createChannel(cio);
//                                onlineStatusService.delChannel(cio.getUserId(), 0, cio.getUuid(), "");
//                                cl.close();
//                            } catch (Exception e) {
//                                logger.error("channel:{} close error", cio.getUuid(), e);
//                            }
//                        });
//            }
//        }

        try {
            onlineStatusService.refreshChannel(channelInfo, channelInfo.getUserId(), channelInfo.getAppID(), ttl);
        } catch (Throwable e) {
            logger.error("heartbeat error close the channel channelInfo:{}", channelInfo, e);
            channel.close();
        }

    }
}
