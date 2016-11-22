package com.handwin.server.controller.livechat.cross;

/**
 * Created by fangliang on 16/7/18.
 */
public interface LiveChatCrossService {

    void join(LiveChatJoin liveChatJoin);

    void send(LiveChatMessage liveChatMessage);

    void leave(LiveChatLeave liveChatLeave);

}
