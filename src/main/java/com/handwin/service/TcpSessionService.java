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
 * Created by fangliang on 8/12/15.
 */
public interface TcpSessionService {

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

    public Map<Platform, List<ChannelInfo>> findChannelInfoMap(final String userId, final int appID);


}
