package com.handwin.server.handler;

import com.chatgame.protobuf.TcpBiz.Tcp2BizReq;
import com.handwin.service.FailingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author fangliang
 */
@Service
public class WrongServerMessageHandler implements ServerMessageHandler {

    @Autowired
    private FailingService failingService;

    @Override
    public void handle(Tcp2BizReq protoMessage) {
        if (protoMessage == null || protoMessage.getBackTrackInfo() == null) {
            return;
        }
        byte[] packetBody = (protoMessage.getMsgBody() != null) ?
                protoMessage.getMsgBody().toByteArray() : null;
        failingService.handle(protoMessage.getMsgBody().toByteArray(),
                protoMessage.getBackTrackInfo().getBytes(), protoMessage.getTraceId());
    }

}
