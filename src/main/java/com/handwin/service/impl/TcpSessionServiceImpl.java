package com.handwin.service.impl;

import com.handwin.bean.Platform;
import com.handwin.entity.ChannelInfo;
import com.handwin.entity.User;
import com.handwin.exception.ServerException;
import com.handwin.packet.ChannelMode;
import com.handwin.server.Channel;
import com.handwin.service.OnlineStatusService;
import com.handwin.service.TcpSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by fangliang on 8/12/15.
 */
@Service
public class TcpSessionServiceImpl implements TcpSessionService {

    @Autowired
    @Qualifier("oldOnlineSessionService")
    private OnlineStatusService oldOnlineStatusService;

    @Autowired
    @Qualifier("newOnlineSessionService")
    private OnlineStatusService newOnlineStatusService;


    private ExecutorService asyncExecutor = Executors.newFixedThreadPool(4);

    private final boolean useNew = true;


    @Override
    public void addChannel(ChannelInfo channelInfo, String userID, int appID, int ttl) {

        if (channelInfo != null) {
            channelInfo.setCreateTime(System.currentTimeMillis());
        }

        if (useNew) {
            newOnlineStatusService.addChannel(channelInfo, userID, appID, ttl);
        } else {
            oldOnlineStatusService.addChannel(channelInfo, userID, appID, ttl);

            asyncExecutor.execute(() -> {
                try {
                    newOnlineStatusService.addChannel(channelInfo, userID, appID, ttl);
                } catch (Exception e) {
                }
            });
        }
    }

    @Override
    public void addChannel(User user, Channel channel) {

        if (channel != null && channel.getChannelInfo() != null) {
            channel.getChannelInfo().setCreateTime(System.currentTimeMillis());
        }

        if (useNew) {
            newOnlineStatusService.addChannel(user, channel);
        } else {

            oldOnlineStatusService.addChannel(user, channel);

            asyncExecutor.execute(() -> {
                try {
                    newOnlineStatusService.addChannel(user, channel);
                } catch (Exception e) {
                }
            });

        }


    }

    @Override
    public void cleanChannel(String userID, int appID) {

        if (useNew) {

            newOnlineStatusService.cleanChannel(userID, appID);

        } else {

            oldOnlineStatusService.cleanChannel(userID, appID);

            asyncExecutor.execute(() -> {
                try {
                    newOnlineStatusService.cleanChannel(userID, appID);
                } catch (Exception e) {
                }
            });

        }


    }

    @Override
    public ChannelInfo getChannelInfo(String userID, int appID, boolean isSystemAccount) throws ServerException {

        if (useNew) {
            return newOnlineStatusService.getChannelInfo(userID, appID, isSystemAccount);
        } else {
            return oldOnlineStatusService.getChannelInfo(userID, appID, isSystemAccount);
        }

    }

    @Override
    public void delChannel(String userID, int appID, String channelUUID, String serverNode) {

        if (useNew) {
            newOnlineStatusService.delChannel(userID, appID, channelUUID, serverNode);
        } else {

            oldOnlineStatusService.delChannel(userID, appID, channelUUID, serverNode);

            asyncExecutor.execute(() -> {
                try {
                    newOnlineStatusService.delChannel(userID, appID, channelUUID, serverNode);
                } catch (Exception e) {
                }
            });
        }

    }

    @Override
    public void refreshServerHeartTTL(String tcpServerID) {

        if (useNew) {
            newOnlineStatusService.refreshServerHeartTTL(tcpServerID);
        } else {
            oldOnlineStatusService.refreshServerHeartTTL(tcpServerID);
        }

    }


    @Override
    public void changeChannelMode(ChannelInfo channelInfo, String userID, int appID, int ttl) {


        if (useNew) {
            newOnlineStatusService.changeChannelMode(channelInfo, userID, appID, ttl);
        } else {

            oldOnlineStatusService.changeChannelMode(channelInfo, userID, appID, ttl);

            asyncExecutor.execute(() -> {
                try {
                    newOnlineStatusService.changeChannelMode(channelInfo, userID, appID, ttl);
                } catch (Exception e) {
                }
            });

        }

    }

    @Override
    public void channelHeartBeat(ChannelInfo channelInfo, String userID, int appID, int ttl) {

        if (useNew) {
            newOnlineStatusService.channelHeartBeat(channelInfo, userID, appID, ttl);
        } else {

            oldOnlineStatusService.channelHeartBeat(channelInfo, userID, appID, ttl);

            asyncExecutor.execute(() -> {
                try {
                    newOnlineStatusService.channelHeartBeat(channelInfo, userID, appID, ttl);
                } catch (Exception e) {
                }
            });

        }


    }

    @Override
    public void refreshChannel(ChannelInfo channelInfo, String userID, int appID, int ttl) throws ServerException {


        if (useNew) {
            newOnlineStatusService.refreshChannel(channelInfo, userID, appID, ttl);

        } else {
            oldOnlineStatusService.refreshChannel(channelInfo, userID, appID, ttl);

            asyncExecutor.execute(() -> {
                try {
                    newOnlineStatusService.refreshChannel(channelInfo, userID, appID, ttl);
                } catch (Exception e) {
                }
            });

        }


    }

    @Override
    public int getChannelModeHeartbeatTime(ChannelMode channelMode) {

        if (useNew) {
            return newOnlineStatusService.getChannelModeHeartbeatTime(channelMode);
        } else {
            return oldOnlineStatusService.getChannelModeHeartbeatTime(channelMode);
        }

    }

    @Override
    public ChannelInfo getChannelInfo(String appUserID, String channelUUID) {

        if (useNew) {
            return newOnlineStatusService.getChannelInfo(appUserID, channelUUID);
        } else {
            return oldOnlineStatusService.getChannelInfo(appUserID, channelUUID);
        }

    }

    @Override
    public List<ChannelInfo> findChannelInfo(String userId, int appID) {

        if (useNew) {
            return newOnlineStatusService.findChannelInfo(userId, appID);
        } else {
            return oldOnlineStatusService.findChannelInfo(userId, appID);
        }
    }


    @Override
    public Map<Platform, List<ChannelInfo>> findChannelInfoMap(String userId, int appID) {
        if (useNew) {
            return newOnlineStatusService.findValidChannelInfoMap(userId, appID);
        } else {
            return oldOnlineStatusService.findValidChannelInfoMap(userId, appID);
        }
    }
}
