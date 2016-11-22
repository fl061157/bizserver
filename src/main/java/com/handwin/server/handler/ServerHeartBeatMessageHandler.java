package com.handwin.server.handler;

import com.chatgame.protobuf.TcpBiz.Tcp2BizReq;
import com.handwin.service.TcpSessionService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author fangliang
 */

@Service
public class ServerHeartBeatMessageHandler implements ServerMessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(ServerHeartBeatMessageHandler.class);

    @Autowired
    private TcpSessionService onlineStatusService;

    @Override
    public void handle(Tcp2BizReq protoMessage) {
        String tcpServerID = protoMessage.getTcpServerId();
        if (StringUtils.isBlank(tcpServerID)) {
            throw new RuntimeException("TcpServerId Empty Error !");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Refresh server status tcpServerId:{}", tcpServerID);
        }
        onlineStatusService.refreshServerHeartTTL(tcpServerID);
    }
}
