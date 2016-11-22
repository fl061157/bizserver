package com.handwin.server.handler;

import com.chatgame.protobuf.TcpBiz;

/**
 * @author fangliang
 */
public interface ServerMessageHandler {

    public void handle(TcpBiz.Tcp2BizReq protoMessage);

}
