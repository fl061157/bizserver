package com.handwin.rabbitmq;

import com.chatgame.protobuf.TcpBiz;
import com.google.protobuf.InvalidProtocolBufferException;
import com.handwin.entity.ServerMessageType;
import com.handwin.mq.MessageListener;
import com.handwin.server.handler.ServerHeartBeatMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author fangliang
 */
public class ServerHeartbeatListener extends MessageListener {

    private static final Logger logger = LoggerFactory.getLogger(ServerHeartbeatListener.class);

    @Autowired
    private ServerHeartBeatMessageHandler serverHeartBeatMessageHandler;

    @Override
    public void onMessage(final byte[] message) throws Exception {

        try {
            TcpBiz.Tcp2BizReq protoMessage = TcpBiz.Tcp2BizReq.getDefaultInstance()
                    .getParserForType().parseFrom(message);
            ServerMessageType serverMessageType = ServerMessageType.getServerMessageType(protoMessage.getMessageType());

            if (serverMessageType != null) {
                if (serverMessageType == ServerMessageType.ServerHeartBeatMessage
                        || serverMessageType == ServerMessageType.ServerForwardHeartBeatMessage) {
                    serverHeartBeatMessageHandler.handle(protoMessage);
                } else {
                    logger.error("[WrongMessageListener] ServerMessageType Is Not ServerHeartBeatMessage ! ");
                }
            } else {
                logger.error("[ServerHeartbeatListener] ServerMessageType Empty !");
            }
        } catch (InvalidProtocolBufferException e) {
            logger.error("parse protobuf error", e);
        } catch (Throwable e) {
            logger.error("server Heartbeat listener error", e);
        }
    }

}
