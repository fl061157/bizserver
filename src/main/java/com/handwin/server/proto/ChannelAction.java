package com.handwin.server.proto;

import org.apache.commons.collections.map.HashedMap;

import java.util.Map;

public enum ChannelAction {

    FREE(-1),
    SEND(0),   //发送
    CLOSE(1),  //关闭通道
    UDAPTE(2), //更新

    JOIN_CHATROOM_ACTION(3),
    QUIT_CHATROOM_ACTION(4),
    MESSAGE_CHATROOM_ACTION(5),

    BROCAST_HEART(100);


    private ChannelAction(int action) {
        this.action = action;
    }

    private int action;

    public int getAction() {
        return action;
    }

    static Map<Integer, ChannelAction> MAP = new HashedMap();

    static {

        for (ChannelAction channelAction : ChannelAction.values()) {
            MAP.put(channelAction.action, channelAction);
        }

    }

    public static ChannelAction getChannelAction(int action) {
        return MAP.get( action ) ;
    }


}
