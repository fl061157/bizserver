package com.handwin.service;

import com.handwin.bean.Platform;
import com.handwin.entity.ChannelInfo;
import com.handwin.entity.User;
import com.handwin.exception.ServerException;
import com.handwin.packet.ChannelMode;
import com.handwin.server.Channel;

import java.util.List;
import java.util.Map;

/**
 * @author fangliang
 */
public interface OnlineStatusService {

    public void addChannel(final ChannelInfo channelInfo, final String userID, final int appID, final int ttl);

    public void addChannel(final User user, final Channel channel);

    public void cleanChannel(final String userID, final int appID);

    public ChannelInfo getChannelInfo(final String userID, final int appID, boolean isSystemAccount) throws ServerException;

    public void delChannel(final String userID, final int appID, String channelUUID, String serverNode);

    public void refreshServerHeartTTL(String tcpServerID);

    public void changeChannelMode(final ChannelInfo channelInfo, final String userID, final int appID, final int ttl);

    public void channelHeartBeat(final ChannelInfo channelInfo, final String userID, final int appID, final int ttl);

    public void refreshChannel(final ChannelInfo channelInfo, final String userID, final int appID, final int ttl) throws ServerException;

    public int getChannelModeHeartbeatTime(ChannelMode channelMode);

    public ChannelInfo getChannelInfo(String appUserID, String channelUUID);

    public List<ChannelInfo> findChannelInfo(final String userId, final int appID);

    public List<ChannelInfo> findValidChannelInfo(final String userId, final int appID);

    public Map<Platform, List<ChannelInfo>> findValidChannelInfoMap(final String userId, final int appID);

    public boolean isServerOnline(String tcpServerID) throws Exception;

}
