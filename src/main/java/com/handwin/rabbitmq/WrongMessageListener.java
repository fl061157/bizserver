package com.handwin.rabbitmq;

import com.chatgame.protobuf.TcpBiz;
import com.google.protobuf.InvalidProtocolBufferException;
import com.handwin.entity.ServerMessageType;
import com.handwin.mq.MessageListener;
import com.handwin.server.handler.WrongServerMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author fangliang
 */
public class WrongMessageListener extends MessageListener {

    private static final Logger logger = LoggerFactory.getLogger(WrongMessageListener.class);

    @Autowired
    private WrongServerMessageHandler wrongServerMessageHandler;

    @Override
    public void onMessage(byte[] message) throws Exception {

        try {
            TcpBiz.Tcp2BizReq protoMessage = TcpBiz.Tcp2BizReq.getDefaultInstance()
                    .getParserForType().parseFrom(message);
            ServerMessageType serverMessageType = ServerMessageType.getServerMessageType(protoMessage.getMessageType());
            if (serverMessageType != null) {
                MDC.put("TraceID", protoMessage.getTraceId());
                try{
                    if (serverMessageType == ServerMessageType.WrongMessage) {
                        wrongServerMessageHandler.handle(protoMessage);
                    } else {
                        logger.error("[WrongMessageListener] ServerMessageType Is Not WrongMessage ! ");
                    }
                }
                finally {
                    MDC.remove("TraceID");
                }

            } else {
                logger.error("[WrongMessageListener] ServerMessageType Empty !");
            }
        } catch (InvalidProtocolBufferException e) {
            logger.error("parse protobuf error", e);
        } catch (Throwable e) {
            logger.error("wrong message listener error", e);
        }
    }

}
