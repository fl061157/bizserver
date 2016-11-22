package com.handwin.service.impl;

import com.alibaba.fastjson.JSON;
import com.handwin.bean.Platform;
import com.handwin.entity.ChannelInfo;
import com.handwin.entity.User;
import com.handwin.exception.ServerException;
import com.handwin.packet.ChannelMode;
import com.handwin.persist.StatusStore;
import com.handwin.server.Channel;
import com.handwin.service.OnlineStatusService;
import com.handwin.utils.UserUtils;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.lang.String.valueOf;

@Service("oldOnlineSessionService")
public class OnlineStatusServiceImpl implements OnlineStatusService {

    public final static String USER_TCP_CHANNEL_LIST_PREFIX = "user_tcp_channel_list_";
    public final static String USER_TCP_CHANNEL_PREFIX = "user_tcp_channel_";
    public final static String TCP_SERVER_STATUS_PREFIX = "tcp_server_status_";
    private static final Logger logger = LoggerFactory.getLogger(OnlineStatusServiceImpl.class);
    private final static byte[] TCP_SERVER_STATUS_VALUE = new byte[]{(byte) 0x01};
    @Inject
    @Qualifier(value = "statusClusterStoreImpl")
    private StatusStore statusStore;
    @Value("${channel.foreground.time.millisecond}")
    private int foreGroundHeartTime;
    @Value("${channel.background.time.millisecond}")
    private int backGroundHeartTime;
    @Value("${channel.hangup.time.millisecond}")
    private int handupHeartTime;
    @Value("${tcpserver.onlie.time.millisecond}")
    private int serverHeartBeatTTL;

    @Override
    public void refreshServerHeartTTL(String tcpServerID) {
        final String tcpServerStatusKey = tcpServerStatusKey(tcpServerID);
        try {
            statusStore.set(tcpServerStatusKey.getBytes(), TCP_SERVER_STATUS_VALUE, serverHeartBeatTTL);
        } catch (Throwable e) {
            logger.error("Refresh TcpServer Heart Error tcpServerID:{} ", tcpServerID, e);
        }
    }

    private String tcpServerStatusKey(String tcpServerID) {
        return format("%s%s", TCP_SERVER_STATUS_PREFIX, tcpServerID);
    }

    @Override
    public void delChannel(String userID, int appID, String channelUUID, String serverNode) {
        String appUserID = UserUtils.getAppUserId(userID, appID);
        final String tcpChannelListKey = format("%s%s", USER_TCP_CHANNEL_LIST_PREFIX, appUserID);
        final String tcpChannelListValue = channelUUID;
        final String userTcpChannelKey = format("%s%s%s", USER_TCP_CHANNEL_PREFIX, appUserID, channelUUID);
        try {
            statusStore.del(userTcpChannelKey.getBytes());
            statusStore.sRem(tcpChannelListKey.getBytes(), tcpChannelListValue.getBytes());
        } catch (Throwable e) {
            logger.error("Del Channel Error userID:{} , channelUUID:{} ", userID, channelUUID, e);
        }
    }

    @Override
    public void changeChannelMode(final ChannelInfo channelInfo, final String userID, final int appID, final int ttl) {
        String appUserID = UserUtils.getAppUserId(userID, valueOf(appID));
        final String channelKey = format("%s%s%s", USER_TCP_CHANNEL_PREFIX, appUserID, channelInfo.getUuid());
        final String tcpChannelListKey = format("%s%s", USER_TCP_CHANNEL_LIST_PREFIX, appUserID);
        try {
            statusStore.set(channelKey.getBytes(), JSON.toJSONString(channelInfo).getBytes(Charset.forName("UTF-8")), ttl);
            statusStore.expire(tcpChannelListKey.getBytes(), ttl);
        } catch (Throwable e) {
            logger.error("Change Channel Mode Error UserID:{} , appID:{} ", userID, appID, e);
        }
    }


    public List<ChannelInfo> findChannelInfo(final String userId, final int appID) {
        final String appUserID = UserUtils.getAppUserId(userId, appID);
        final String channelListKey = format("%s%s", USER_TCP_CHANNEL_LIST_PREFIX, appUserID);
        Set<byte[]> channels = null;
        try {
            channels = statusStore.smembers(channelListKey.getBytes());
        } catch (Throwable e) {
            logger.error("findChannelInfo Error userId:{}  , appID:{} ", userId, appID, e);
        }
        if (CollectionUtils.isEmpty(channels)) {
            return null;
        }
        return channels.stream().filter(channel -> channel != null)
                .map(channel -> {
                    String channelUUID = new String(channel, Charset.forName("UTF-8"));
                    return getChannelInfo(appUserID, channelUUID);
                }).filter(channelInfo -> channelInfo != null).collect(Collectors.toList());
    }


    private boolean isServerOnline(ChannelInfo channelInfo) {
        final String tcpServerID = channelInfo.getNodeId();
        byte[] result = null;
        try {
            result = statusStore.get(tcpServerStatusKey(tcpServerID).getBytes());
        } catch (Throwable e) {
            logger.error("isServerOnline Error channelInfo:{} ", channelInfo);
        }
        return result != null && result.length > 0;
    }

    @Override
    public ChannelInfo getChannelInfo(final String userID, final int appID, boolean isSystemAccount) throws ServerException {
        try {
            List<ChannelInfo> result = findChannelInfo(userID, appID);
            if (CollectionUtils.isEmpty(result)) {
                return null;
            }
            if (isSystemAccount) {
                result = result.stream()
                        .filter(channelInfo -> isServerOnline(channelInfo))
                        .collect(Collectors.toList());
                if (CollectionUtils.isEmpty(result)) {
                    return null;
                }
                Random random = RandomThreadLocal.getRandom();
                return result.stream().skip(random.nextInt(result.size())).findFirst().get();
            }
            return result.stream().filter(channelInfo -> isServerOnline(channelInfo)).findFirst().orElse(null);
        } catch (Exception e) {
            throw new ServerException(ServerException.ErrorCode.CanNotHandleIoError, e);
        }
    }

    @Override
    public ChannelInfo getChannelInfo(String appUserID, final String channelUUID) {
        final String channelKey = format("%s%s%s", USER_TCP_CHANNEL_PREFIX, appUserID, channelUUID);
        byte[] getBytes = null;
        try {
            getBytes = statusStore.get(channelKey.getBytes());
        } catch (Exception e) {
            logger.error("getChannelInfo Error appUserID:{} , channelUUID:{} ", appUserID, channelUUID);
        }
        if (getBytes != null) {
            return JSON.parseObject(new String(getBytes, Charset.forName("UTF-8")), ChannelInfo.class);
        } else {
            return null;
        }
    }


    //TODO 整理 BizServer

    @Override
    public List<ChannelInfo> findValidChannelInfo(String userId, int appID) {
        return null;
    }

    //TODO 整理 BizServer

    @Override
    public Map<Platform, List<ChannelInfo>> findValidChannelInfoMap(String userId, int appID) {
        return null;
    }

    /**
     * 重新设值
     *
     * @param channelInfo
     * @param userID
     * @param appID
     * @param ttl
     */
    @Override
    public void channelHeartBeat(final ChannelInfo channelInfo,
                                 final String userID, final int appID, final int ttl) { //TODO 可能导致不一致之情况
        String appUserId = UserUtils.getAppUserId(userID, appID);
        final String listKey = format("%s%s", USER_TCP_CHANNEL_LIST_PREFIX, appUserId);
        final String singleKey = format("%s%s%s", USER_TCP_CHANNEL_PREFIX,
                appUserId, channelInfo.getUuid());
        try {
            final boolean exists = statusStore.expire(listKey.getBytes(), ttl);

            statusStore.sAdd(listKey.getBytes(), channelInfo.getUuid().getBytes());
            if (!exists) {
                statusStore.expire(listKey.getBytes(), ttl);
                logger.debug("channelUUid:{} does not exist in cache.", channelInfo.getUuid());
            } else {
                //获取原有channle的值，然后更新channelAttr
                ChannelInfo oldChannnelInfo = getChannelInfo(appUserId, channelInfo.getUuid());
                if (null != oldChannnelInfo) {
                    channelInfo.setNetworkType(oldChannnelInfo.getNetworkType());
                }
                logger.debug("old channel info:{} .", oldChannnelInfo);
            }
            statusStore.set(singleKey.getBytes(), JSON.toJSONString(channelInfo).getBytes(Charset.forName("UTF-8")), ttl);
        } catch (Throwable e) {
            logger.error("Channel HeartBeat Error userID:{} , appID:{}", userID, appID, e);
        }
    }


    @Override
    public void refreshChannel(ChannelInfo channelInfo, String userID, int appID, int ttl) {
        channelHeartBeat(channelInfo, userID, appID, ttl);
    }

    @Override
    public void cleanChannel(final String userID, final int appID) {
        //暂时非系统账号只允许一个节点登录
        final String appUserID = UserUtils.getAppUserId(userID, appID);
        final String channelListKey = format("%s%s", USER_TCP_CHANNEL_LIST_PREFIX, appUserID);
        try {
            Set<byte[]> channels = statusStore.smembers(channelListKey.getBytes());
            if (CollectionUtils.isNotEmpty(channels)) {
                statusStore.del(channelListKey.getBytes());
                channels.stream()
                        .map(channel -> new String(channel, Charset.forName("UTF-8")))
                        .forEach(key -> {
                            String channelKey = format("%s%s%s", USER_TCP_CHANNEL_PREFIX, appUserID, key);
                            try {
                                statusStore.del(channelKey.getBytes());
                            } catch (Exception e) {
                                logger.error("del error: ", e);
                            }
                        });
            }

        } catch (Throwable e) {
            logger.error("cleanChannel Error userID:{} , appID:{} ", userID, appID, e);
        }
    }

    @Override
    public void addChannel(final ChannelInfo channelInfo, final String userID, final int appID, final int ttl) {
        String appUserId = UserUtils.getAppUserId(userID, appID);
        final String listKey = format("%s%s", USER_TCP_CHANNEL_LIST_PREFIX, appUserId);
        final String singleKey = format("%s%s%s", USER_TCP_CHANNEL_PREFIX, appUserId,
                channelInfo.getUuid());
        try {
            statusStore.sAdd(listKey.getBytes(), channelInfo.getUuid().getBytes());
            statusStore.expire(listKey.getBytes(), ttl);
            statusStore.set(singleKey.getBytes(), JSON.toJSONString(channelInfo).getBytes(Charset.forName("UTF-8")), ttl);
        } catch (Exception e) {
            logger.error("addChannel Error userID:{} , appID:{} ", userID, appID, e);
        }
        logger.debug("[channel].store listKey:{},singleKey:{},channelInfo:{}", listKey, singleKey, channelInfo);
    }

    @Override
    public void addChannel(User user, Channel channel) {
        ChannelInfo channelInfo = channel.getChannelInfo();
        int ttl = getChannelModeHeartbeatTime(channelInfo.getChannelMode());
        addChannel(channelInfo, user.getId(), user.getAppId(), ttl);
    }

    @Override
    public int getChannelModeHeartbeatTime(ChannelMode channelMode) {
        int heartBeatTime = 0;
        switch (channelMode) {
            case FOREGROUND: {
                heartBeatTime = foreGroundHeartTime;
                break;
            }
            case BACKGROUND: {
                heartBeatTime = backGroundHeartTime;
                break;
            }
            case SUSPEND: {
                heartBeatTime = handupHeartTime;
                break;
            }
            default: {
                //TODO 如何处理 理论上不应该
                heartBeatTime = foreGroundHeartTime;
                break;
            }
        }
        heartBeatTime = heartBeatTime / 1000;
        return heartBeatTime;
    }


    private static class RandomThreadLocal {
        private static ThreadLocal<Random> THREAD_LOCAL = new ThreadLocal<Random>();

        public static Random getRandom() {
            Random r = THREAD_LOCAL.get();
            if (r == null) {
                synchronized (RandomThreadLocal.class) {
                    r = THREAD_LOCAL.get();
                    if (r == null) {
                        r = new Random();
                        THREAD_LOCAL.set(r);
                    }
                }
            }
            return r;
        }
    }

    @Override
    public boolean isServerOnline(String tcpServerID) throws Exception {
        return true;
    }
}
