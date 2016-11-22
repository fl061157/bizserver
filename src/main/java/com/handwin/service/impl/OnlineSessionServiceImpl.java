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
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import static java.lang.String.format;

/**
 * Created by fangliang on 7/12/15.
 */

@Service("newOnlineSessionService")
public class OnlineSessionServiceImpl implements OnlineStatusService {

    public final static String USER_TCP_CHANNEL_HASH_PREFIX = "user_tcp_channel_hash_";
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
    public void addChannel(ChannelInfo channelInfo, String userID, int appID, int ttl) {

        String appUserId = UserUtils.getAppUserId(userID, appID);
        final String hashKey = format("%s%s", USER_TCP_CHANNEL_HASH_PREFIX, appUserId);
        try {
            statusStore.hSet(hashKey, channelInfo.getUuid(), JSON.toJSONString(channelInfo));
            statusStore.expire(hashKey, ttl);
        } catch (Exception e) {
            logger.error("addChannel Error userID:{} , appID:{} ", userID, appID, e);
        }
    }

    @Override
    public void addChannel(User user, Channel channel) {
        ChannelInfo channelInfo = channel.getChannelInfo();
        int ttl = getChannelModeHeartbeatTime(channelInfo.getChannelMode());
        addChannel(channelInfo, user.getId(), user.getAppId(), ttl);
    }


    @Override
    public void cleanChannel(String userID, int appID) {
        final String appUserID = UserUtils.getAppUserId(userID, appID);
        final String hashKey = format("%s%s", USER_TCP_CHANNEL_HASH_PREFIX, appUserID);
        try {
            statusStore.del(hashKey);
        } catch (Throwable e) {
            logger.error("cleanChannel Error userID:{} , appID:{} ", userID, appID, e);
        }
    }

    @Override
    public ChannelInfo getChannelInfo(String userID, int appID, boolean isSystemAccount) throws ServerException {
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
    public void delChannel(String userID, int appID, String channelUUID, String serverNode) {

        final String appUserID = UserUtils.getAppUserId(userID, appID);
        final String hashKey = format("%s%s", USER_TCP_CHANNEL_HASH_PREFIX, appUserID);

        if (logger.isDebugEnabled()) {
            logger.debug("[DelChannel] Before HashKey:{} , ChannelUUID:{} ", hashKey, channelUUID);
        }

        try {
            statusStore.hDel(hashKey, channelUUID);
        } catch (Exception e) {
            logger.error("Del Channel Error userID:{} , channelUUID:{} ", userID, channelUUID, e);
        }
    }

    @Override
    public void refreshServerHeartTTL(String tcpServerID) {
        final String tcpServerStatusKey = tcpServerStatusKey(tcpServerID);
        try {
            statusStore.set(tcpServerStatusKey.getBytes(), TCP_SERVER_STATUS_VALUE, serverHeartBeatTTL);
        } catch (Throwable e) {
            logger.error("Refresh TcpServer Heart Error tcpServerID:{} ", tcpServerID, e);
        }
    }

    @Override
    public void changeChannelMode(ChannelInfo channelInfo, String userID, int appID, int ttl) {

        final String appUserID = UserUtils.getAppUserId(userID, appID);
        final String hashKey = format("%s%s", USER_TCP_CHANNEL_HASH_PREFIX, appUserID);
        try {
            statusStore.hSet(hashKey, channelInfo.getUuid(), JSON.toJSONString(channelInfo));
            statusStore.expire(hashKey, ttl);
        } catch (Exception e) {
            logger.error("Change Channel Mode Error UserID:{} , appID:{} ", userID, appID, e);
        }

    }

    @Override
    public void channelHeartBeat(ChannelInfo channelInfo, String userID, int appID, int ttl) {
        try {
            ChannelInfo oldInfo = getChannelInfo(UserUtils.getAppUserId(userID, appID), channelInfo.getUuid());
            if (null != oldInfo) {
                channelInfo.setNetworkType(oldInfo.getNetworkType());
                channelInfo.setPlatform(oldInfo.getPlatform());
                channelInfo.setKickId(oldInfo.getKickId());
                channelInfo.setCreateTime( oldInfo.getCreateTime() );
            }
            addChannel(channelInfo, userID, appID, ttl);
        } catch (Throwable e) {
            logger.error("Channel HeartBeat Error userID:{} , appID:{}", userID, appID, e);
        }
    }

    @Override
    public void refreshChannel(ChannelInfo channelInfo, String userID, int appID, int ttl) throws ServerException {
        channelHeartBeat(channelInfo, userID, appID, ttl);
    }

    @Override
    public int getChannelModeHeartbeatTime(ChannelMode channelMode) {
        int heartBeatTime;
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

    @Override
    public ChannelInfo getChannelInfo(String appUserID, String channelUUID) {

        final String hashKey = format("%s%s", USER_TCP_CHANNEL_HASH_PREFIX, appUserID);

        String cInfo = null;
        try {
            cInfo = statusStore.hGet(hashKey, channelUUID);
        } catch (Exception e) {
            logger.error("getChannelInfo Error appUserID:{} , channelUUID:{} ", appUserID, channelUUID);
        }

        if (StringUtils.isNotBlank(cInfo)) {
            return JSON.parseObject(cInfo, ChannelInfo.class);
        }

        return null;

    }

    @Override
    public List<ChannelInfo> findChannelInfo(String userId, int appID) {

        final String appUserID = UserUtils.getAppUserId(userId, appID);
        final String hashKey = format("%s%s", USER_TCP_CHANNEL_HASH_PREFIX, appUserID);
        try {
            Map<String, String> mCInfo = statusStore.hGetAll(hashKey);
            if (mCInfo != null) {
                return mCInfo.values().stream().map(sc -> {
                    ChannelInfo channelInfo = JSON.parseObject(sc, ChannelInfo.class);
                    return channelInfo;
                }).filter(ci -> ci != null).collect(Collectors.toList());
            }
        } catch (Exception e) {
            logger.error("findChannelInfo Error userId:{}  , appID:{} ", userId, appID, e);
            return null;
        }
        return null;
    }


    @Override
    public List<ChannelInfo> findValidChannelInfo(String userId, int appID) {

        List<ChannelInfo> channelInfoList = findChannelInfo(userId, appID);

        if (CollectionUtils.isEmpty(channelInfoList)) {
            return null;
        }

        return channelInfoList.stream().filter(channelInfo -> isServerOnline(channelInfo)).collect(Collectors.toList());
    }

    @Override
    public Map<Platform, List<ChannelInfo>> findValidChannelInfoMap(String userId, int appID) {

        List<ChannelInfo> channelInfoList = findChannelInfo(userId, appID);

        if (CollectionUtils.isEmpty(channelInfoList)) {
            return null;
        }

        channelInfoList = channelInfoList.stream().filter(channelInfo -> isServerOnline(channelInfo)).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(channelInfoList)) {
            return null;
        }

        Map<Platform, List<ChannelInfo>> result = new HashedMap();

        for (ChannelInfo channelInfo : channelInfoList) {
            Platform platform = channelInfo.findPlatform();
            List<ChannelInfo> infoList = result.get(platform);
            if (infoList == null) {
                infoList = new ArrayList<>();
                result.put(platform, infoList);
            }
            infoList.add(channelInfo);
        }

        return result;
    }

    private String tcpServerStatusKey(String tcpServerID) {
        return format("%s%s", TCP_SERVER_STATUS_PREFIX, tcpServerID);
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


    public boolean isServerOnline(String tcpServerID) throws Exception {
        byte[] result;
        try {
            result = statusStore.get(tcpServerStatusKey(tcpServerID).getBytes());
        } catch (Exception e) {
            logger.error("isServerOnline Error tcpServerID:{} ", tcpServerID);
            throw e;
        }
        return result != null && result.length > 0;
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

}
