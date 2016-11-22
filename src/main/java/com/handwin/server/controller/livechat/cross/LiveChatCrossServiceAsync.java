package com.handwin.server.controller.livechat.cross;

import cn.v5.rpc.RpcFuture;

/**
 * Created by fangliang on 16/7/18.
 */
public interface LiveChatCrossServiceAsync {

    RpcFuture<Void> join(LiveChatJoin liveChatJoin);

    RpcFuture<Void> send(LiveChatMessage liveChatMessage);

    RpcFuture<Void> leave(LiveChatLeave liveChatLeave);


}
