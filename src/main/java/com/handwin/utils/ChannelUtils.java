package com.handwin.utils;

import com.handwin.bean.Platform;
import com.handwin.entity.ChannelInfo;
import com.handwin.packet.ChannelMode;
import com.handwin.packet.PacketHead;
import com.handwin.server.Channel;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;

import java.util.List;
import java.util.Map;

/**
 * Created by fangliang on 20/4/15.
 */
public class ChannelUtils {

    public static boolean isOffline(Channel channel) {
        if (channel == null) {
            return true;
        }
        ChannelInfo channelInfo = channel.getChannelInfo();
        if (channelInfo == null) {
            return true;
        }
        ChannelMode channelMode = channelInfo.getChannelMode();
        if (channelMode == null) {
            return true;
        }
        return channelMode.equals(ChannelMode.SUSPEND);
    }


    public static boolean isOffline(Map<Platform, List<Channel>> map) {
        if (MapUtils.isEmpty(map)) return true;
        List<Channel> channelList = map.get(Platform.Mobile);
        if (CollectionUtils.isEmpty(channelList)) return true;
        Channel channel = channelList.get(0); //TODO 不应该出现 Size > 1 情形
        ChannelInfo channelInfo = channel.getChannelInfo();
        if (channelInfo == null) return true;
        ChannelMode channelMode = channelInfo.getChannelMode();
        if (channelMode == null) return true;
        return channelMode.equals(ChannelMode.SUSPEND);
    }


    public static ChannelInfo chooseBestChannel(Map<Platform, List<Channel>> map) {
        if (MapUtils.isEmpty(map)) return null;
        List<Channel> mobileChannelList = map.get(Platform.Mobile);
        if (CollectionUtils.isNotEmpty(mobileChannelList)) return mobileChannelList.get(0).getChannelInfo();
        List<Channel> proxyChannelList = map.get(Platform.Proxy);
        if (CollectionUtils.isNotEmpty(proxyChannelList)) return proxyChannelList.get(0).getChannelInfo();
        return map.values().iterator().next().get(0).getChannelInfo();
    }


    public static boolean isNoForgeRound(Map<Platform, List<Channel>> map) { //TODO 是否在前台之状态
        if (MapUtils.isEmpty(map)) return true;
        List<Channel> mobileChannelList = map.get(Platform.Mobile);
        if (CollectionUtils.isNotEmpty(mobileChannelList) && map.size() == 1) {
            ChannelInfo cInfo = mobileChannelList.get(0).getChannelInfo();
            return cInfo == null || !cInfo.getChannelMode().equals(ChannelMode.FOREGROUND);
        }
        return false;
    }


    public static void wrapAppID(PacketHead packetHead, Channel channel) {

        if (packetHead != null && channel != null && channel.getChannelInfo() != null
                && channel.getChannelInfo().getAppID() > 0) {
            packetHead.setAppId(channel.getChannelInfo().getAppID());
        }

    }


}
