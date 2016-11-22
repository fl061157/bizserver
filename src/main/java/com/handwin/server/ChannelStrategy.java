package com.handwin.server;

import java.util.HashMap;

/**
 * Created by fangliang on 11/5/15.
 */
public enum ChannelStrategy {

    RabbitMqChannel(1),

    ZeroMqChannel(2),

    MrChannel(3);

    private int channel;

    private ChannelStrategy(int channel) {
        this.channel = channel;
    }

    public static ChannelStrategy getChannelStrategy(Integer channel) {
        if (channel == null) {
            return MrChannel;
        }
        ChannelStrategy channelStrategy = STRATEGY_MAP.get(channel);
        return channelStrategy != null ? channelStrategy : MrChannel;
    }

    private static HashMap<Integer, ChannelStrategy> STRATEGY_MAP = new HashMap<>();

    static {
        for (ChannelStrategy channelStrategy : ChannelStrategy.values()) {
            STRATEGY_MAP.put(channelStrategy.channel, channelStrategy);
        }
    }

}
