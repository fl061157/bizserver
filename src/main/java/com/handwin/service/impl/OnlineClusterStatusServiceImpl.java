//package com.handwin.service.impl;
//
//import com.handwin.entity.ChannelInfo;
//import com.handwin.entity.User;
//import com.handwin.exception.ServerException;
//import com.handwin.localentity.ChannelInfoBean;
//import com.handwin.localentity.ChannelInfoKey;
//import com.handwin.packet.ChannelMode;
//import com.handwin.persist.ChannelPersist;
//import com.handwin.persist.StatusStore;
//import com.handwin.server.Channel;
//import com.handwin.service.OnlineStatusService;
//import com.handwin.utils.JsonUtil;
//import com.handwin.utils.UserUtils;
//import org.apache.commons.collections.CollectionUtils;
//import org.apache.commons.lang.StringUtils;
//import org.codehaus.jackson.map.ObjectMapper;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
//import javax.inject.Inject;
//import java.nio.charset.Charset;
//import java.util.List;
//import java.util.Set;
//import java.util.concurrent.ThreadLocalRandom;
//import java.util.stream.Collectors;
//
//import static java.lang.String.format;
//import static java.lang.String.valueOf;
//
///**
// * Created by fangliang on 5/1/15.
// */
//@Service("onlineClusterStatusServiceImpl")
//public class OnlineClusterStatusServiceImpl implements OnlineStatusService {
//
//    public final static String USER_TCP_CHANNEL_LIST_PREFIX = "user_tcp_channel_list_";
//    public final static String USER_TCP_CHANNEL_PREFIX = "user_tcp_channel_";
//    public final static String TCP_SERVER_STATUS_PREFIX = "tcp_server_status_";
//    private static final Logger logger = LoggerFactory.getLogger(OnlineStatusServiceImpl.class);
//    private final static byte[] TCP_SERVER_STATUS_VALUE = new byte[]{(byte) 0x01};
//
//    private final static String USER_ONLINE_STATUS = "user_online_status_";
//
//    private final static String ONLINE = "1";
//
//    private final static String OFFLINE = "0";
//
//
//    @Inject
//    @Qualifier("statusClusterStoreImpl")
//    private StatusStore statusStore;
//
//    @Autowired
//    private ChannelPersist channelPersist;
//
//    @Value("${channel.foreground.time.millisecond}")
//    private int foreGroundHeartTime;
//
//    @Value("${channel.background.time.millisecond}")
//    private int backGroundHeartTime;
//
//    @Value("${channel.hangup.time.millisecond}")
//    private int handupHeartTime;
//
//    @Value("${tcpserver.onlie.time.millisecond}")
//    private int serverHeartBeatTTL;
//    @Autowired
//    private ObjectMapper mapper;
//
//
//    @Override
//    public void addChannel(ChannelInfo channelInfo, String userID, int appID, int ttl) {
//        String appUserId = UserUtils.getAppUsergit Id(userID, appID);
//        final String listKey = format("%s%s", USER_TCP_CHANNEL_LIST_PREFIX, appUserId);
//        final String singleKey = format("%s%s%s", USER_TCP_CHANNEL_PREFIX, appUserId,
//                channelInfo.getUuid());
//        channelPersist.insert(parse(channelInfo, userID, appID), ttl);
//        try {
//            statusStore.sAdd(listKey.getBytes(), channelInfo.getUuid().getBytes());
//            statusStore.expire(listKey.getBytes(), ttl);
//            statusStore.set(singleKey.getBytes(), JsonUtil.toJson(mapper, channelInfo).getBytes(Charset.forName("UTF-8")), ttl);
//            signOnline(userID, appID);
//        } catch (Exception e) {
//            logger.error(String.format("add channel user.id: %s", userID), e);
//        }
//    }
//
//    @Override
//    public void addChannel(User user, Channel channel) {
//        ChannelInfo channelInfo = channel.getChannelInfo();
//        int ttl = getChannelModeHeartbeatTime(channelInfo.getChannelMode());
//        addChannel(channelInfo, user.getId(), (short) user.getAppId(), ttl);
//    }
//
//    @Override
//    public void cleanChannel(String userID, int appID) {
//        final String appUserID = UserUtils.getAppUserId(userID, appID);
//        final String channelListKey = format("%s%s", USER_TCP_CHANNEL_LIST_PREFIX, appUserID);
//        Set<byte[]> channels = null;
//        try {
//            channels = statusStore.smembers(channelListKey.getBytes());
//        } catch (Exception e) {
//            logger.error(String.format("cleanChannel key:%s", channelListKey), e);
//        }
//        if (CollectionUtils.isEmpty(channels)) {
//            return;
//        }
//        try {
//            statusStore.del(channelListKey.getBytes());
//            channels.stream()
//                    .map(channel -> new String(channel, Charset.forName("UTF-8")))
//                    .forEach(key -> {
//                        String channelKey = format("%s%s%s", USER_TCP_CHANNEL_PREFIX, appUserID, key);
//                        try {
//                            statusStore.del(channelKey.getBytes());
//                        } catch (Exception e) {
//                            logger.error("del error: ", e);
//                        }
//                    });
//        } catch (Exception e) {
//            logger.error(String.format("delete channelListLey:%s error", channelListKey), e);
//        }
//        channelPersist.delete(userID, appID);
//        try {
//            signOffline(userID, appID);
//        } catch (Exception e) {
//            logger.error(String.format("signOffline user.id :%s error", userID), e);
//        }
//    }
//
//    @Override
//    public ChannelInfo getChannelInfo(String userID, int appID, boolean isSystemAccount) throws ServerException {
//        try {
//            List<ChannelInfo> result = findChannelInfo(userID, appID);
//            if (CollectionUtils.isEmpty(result)) {
//                return null;
//            }
//            if (isSystemAccount) {
//                result = result.stream()
//                        .filter(channelInfo -> isServerOnline(channelInfo))
//                        .collect(Collectors.toList());
//                if (CollectionUtils.isEmpty(result)) {
//                    signOffline(userID, appID);
//                    return null;
//                }
//                return result.stream().skip(ThreadLocalRandom.current().nextInt(result.size())).findFirst().get();
//            }
//            ChannelInfo r = result.stream().filter(channelInfo -> isServerOnline(channelInfo)).findFirst().orElse(null);
//            if (r == null) {
//                signOffline(userID, appID);
//            }
//            return r;
//        } catch (Exception e) {
//            throw new ServerException(ServerException.ErrorCode.CanNotHandleIoError, e);
//        }
//    }
//
//    @Override
//    public void delChannel(String userID, int appID, String channelUUID, String serverNode) {
//        String appUserID = UserUtils.getAppUserId(userID, appID);
//        final String tcpChannelListKey = format("%s%s", USER_TCP_CHANNEL_LIST_PREFIX, appUserID);
//        final String tcpChannelListValue = channelUUID;
//        final String userTcpChannelKey = format("%s%s%s", USER_TCP_CHANNEL_PREFIX, appUserID, channelUUID);
//        try {
//            statusStore.del(userTcpChannelKey.getBytes());
//            statusStore.sRem(tcpChannelListKey.getBytes(), tcpChannelListValue.getBytes());
//        } catch (Exception e) {
//            logger.error("error:", e);
//        }
//        channelPersist.delete(userID, appID, channelUUID);
//        try {
//            if (!statusStore.exists(tcpChannelListKey.getBytes())) {
//                signOffline(userID, appID);
//            }
//        } catch (Exception e) {
//            logger.error("error:", e);
//        }
//    }
//
//    @Override
//    public void refreshServerHeartTTL(String tcpServerID) {
//        final String tcpServerStatusKey = tcpServerStatusKey(tcpServerID);
//        try {
//            statusStore.set(tcpServerStatusKey.getBytes(), TCP_SERVER_STATUS_VALUE, serverHeartBeatTTL);
//        } catch (Exception e) {
//            logger.error("error:", e);
//        }
//    }
//
//    @Override
//    public void changeChannelMode(ChannelInfo channelInfo, String userID, int appID, int ttl) {
//        channelPersist.updateChannelMode(userID, appID, channelInfo.getUuid(), channelInfo.getChannelMode(), ttl);
//        String appUserID = UserUtils.getAppUserId(userID, valueOf(appID));
//        try {
//            final String channelKey = format("%s%s%s", USER_TCP_CHANNEL_PREFIX, appUserID, channelInfo.getUuid());
//            final String tcpChannelListKey = format("%s%s", USER_TCP_CHANNEL_LIST_PREFIX, appUserID);
//            statusStore.set(channelKey.getBytes(), JsonUtil.toJson(mapper, channelInfo).getBytes(Charset.forName("UTF-8")), ttl);
//            statusStore.expire(tcpChannelListKey.getBytes(), ttl);
//            signOnline(userID, appID);
//        } catch (Exception e) {
//            logger.error("error:", e);
//        }
//    }
//
//    @Override
//    public void channelHeartBeat(ChannelInfo channelInfo, String userID, int appID, int ttl) {//TODO 心跳时 是否需要刷新在线状态 ONLINE OFFLINE
//        channelPersist.expire(userID, appID, channelInfo.getUuid(), ttl);
//        String appUserId = UserUtils.getAppUserId(userID, appID);
//        try {
//            final String listKey = format("%s%s", USER_TCP_CHANNEL_LIST_PREFIX, appUserId);
//            final String singleKey = format("%s%s%s", USER_TCP_CHANNEL_PREFIX,
//                    appUserId, channelInfo.getUuid());
//
//            final boolean exists = statusStore.expire(listKey.getBytes(), ttl);
//            if (!exists) {
//                statusStore.sAdd(listKey.getBytes(), channelInfo.getUuid().getBytes());
//                statusStore.expire(listKey.getBytes(), ttl);
//            }
//            statusStore.set(singleKey.getBytes(), JsonUtil.toJson(mapper, channelInfo).getBytes(Charset.forName("UTF-8")), ttl);
//            signOnline(userID, appID);
//        } catch (Exception e) {
//            logger.error("error:", e);
//        }
//    }
//
//
//    @Override
//    public void refreshChannel(ChannelInfo channelInfo, String userID, int appID, int ttl) throws ServerException {
//        if (logger.isDebugEnabled()) {
//            logger.debug("Refresh channel channelInfo:{}", channelInfo);
//        }
//        ChannelInfoBean channelInfoBean;
//        try {
//            channelInfoBean = channelPersist.get(userID, appID, channelInfo.getUuid());
//        } catch (Exception e) {
//            logger.error("channelPersist get error, userID:{}", userID, e);
//            throw new ServerException(ServerException.ErrorCode.CanNotHandleIoError, e);
//        }
//        if (channelInfoBean != null) {
//            try {
//                channelPersist.expire(channelInfoBean, ttl);
//            } catch (ServerException e) {
//                logger.error("channelPersist expire error, userID:{}", userID, e);
//                throw new ServerException(ServerException.ErrorCode.CanNotHandleIoError, e);
//            }
//            String appUserId = UserUtils.getAppUserId(userID, appID);
//            final String listKey = format("%s%s", USER_TCP_CHANNEL_LIST_PREFIX, appUserId);
//            final String singleKey = format("%s%s%s", USER_TCP_CHANNEL_PREFIX,
//                    appUserId, channelInfo.getUuid());
//            try {
//                final boolean exists = statusStore.expire(listKey.getBytes(), ttl);
//                if (!exists) {
//                    statusStore.sAdd(listKey.getBytes(), channelInfo.getUuid().getBytes());
//                    statusStore.expire(listKey.getBytes(), ttl);
//                }
//                statusStore.set(singleKey.getBytes(), JsonUtil.toJson(mapper, channelInfo).getBytes(Charset.forName("UTF-8")), ttl);
//                signOnline(userID, appID);
//            } catch (Exception e) {
//                logger.error("refreshChannel operate cache error", e);
//            }
//        } else {
//            addChannel(channelInfo, userID, appID, ttl);
//        }
//    }
//
//    @Override
//    public int getChannelModeHeartbeatTime(ChannelMode channelMode) {
//        int heartBeatTime = 0;
//        switch (channelMode) {
//            case FOREGROUND: {
//                heartBeatTime = foreGroundHeartTime;
//                break;
//            }
//            case BACKGROUND: {
//                heartBeatTime = backGroundHeartTime;
//                break;
//            }
//            case SUSPEND: {
//                heartBeatTime = handupHeartTime;
//                break;
//            }
//            default: {
//                //TODO 如何处理 理论上不应该
//                heartBeatTime = foreGroundHeartTime;
//                break;
//            }
//        }
//        heartBeatTime = heartBeatTime / 1000;
//        return heartBeatTime;
//    }
//
//    private String tcpServerStatusKey(String tcpServerID) {
//        return format("%s%s", TCP_SERVER_STATUS_PREFIX, tcpServerID);
//    }
//
//
//    protected List<ChannelInfo> findChannelInfo(final String userId, final int appID) {
//        List<ChannelInfo> channelInfoList = null;
//        OnlineStatus onlineStatus = getOnlineStatus(userId, appID);
//        if (onlineStatus == OnlineStatus.OFFLINE) {
//            return null;
//        }
//        try {
//            channelInfoList = findChannelInfoFromCache(userId, appID);
//        } catch (Exception e) {
//            logger.error(String.format("findChannelInfo from cache userID:%s error", userId), e);
//        }
//        if (channelInfoList == null || channelInfoList.size() == 0) {
//            List<ChannelInfoBean> channelInfoBeanList = channelPersist.find(userId, appID);
//            if (channelInfoBeanList != null) {
//                channelInfoList = parse(channelInfoBeanList);
//                if (channelInfoList != null && channelInfoList.size() > 0) {
//                    channelInfoList.stream().forEach(channelInfo -> addChannelCache(channelInfo,
//                            channelInfo.getUserId(), channelInfo.getAppID(),
//                            getChannelModeHeartbeatTime(channelInfo.getChannelMode())));
//                    signOnline(userId, appID);
//                }
//            }
//        }
//        if (channelInfoList == null) {
//            signOffline(userId, appID);
//        }
//        return channelInfoList;
//    }
//
//    protected List<ChannelInfo> findChannelInfoFromCache(final String userId, final int appID) {
//        final String appUserID = UserUtils.getAppUserId(userId, appID);
//        final String channelListKey = format("%s%s", USER_TCP_CHANNEL_LIST_PREFIX, appUserID);
//        Set<byte[]> channels = null;
//        try {
//            channels = statusStore.smembers(channelListKey.getBytes());
//        } catch (Exception e) {
//            logger.error(String.format("find channelList error : %s", channelListKey), e);
//        }
//        if (CollectionUtils.isEmpty(channels)) {
//            return null;
//        }
//        return channels.stream().filter(channel -> channel != null)
//                .map(channel -> {
//                    String channelUUID = new String(channel, Charset.forName("UTF-8"));
//                    final String channelKey = format("%s%s%s", USER_TCP_CHANNEL_PREFIX, appUserID, channelUUID);
//                    byte[] getBytes = null;
//                    try {
//                        getBytes = statusStore.get(channelKey.getBytes());
//                    } catch (Exception e) {
//                        logger.error("error: ", e);
//                    }
//                    if (getBytes != null) {
//                        return JsonUtil.fromJson(mapper, new String(getBytes, Charset.forName("UTF-8")), ChannelInfo.class);
//                    } else {
//                        return null;
//                    }
//                }).filter(channelInfo -> channelInfo != null).collect(Collectors.toList());
//    }
//
//
//    private static ChannelInfoBean parse(ChannelInfo channelInfo, String userID, int appID) {
//        ChannelInfoKey channelInfoKey = new ChannelInfoKey(userID, appID, channelInfo.getUuid());
//        ChannelInfoBean channelInfoBean = new ChannelInfoBean();
//        channelInfoBean.setId(channelInfoKey);
//        channelInfoBean.setChannelMode(channelInfo.getChannelMode().getValue());
//        channelInfoBean.setClientVersion(channelInfo.getClientVersion());
//        channelInfoBean.setIp(channelInfo.getIp());
//        channelInfoBean.setPort(channelInfo.getPort());
//        channelInfoBean.setSessionID(channelInfo.getSessonId());
//        channelInfoBean.setTcpID(channelInfo.getId());
//        channelInfoBean.setTcpZoneCode(channelInfo.getTcpZoneCode());
//        channelInfoBean.setUserZoneCode(channelInfo.getUserZoneCode());
//        channelInfoBean.setNodeID(channelInfo.getNodeId());
//        return channelInfoBean;
//    }
//
//    private static ChannelInfo parse(ChannelInfoBean channelInfoBean) {
//        ChannelInfo channelInfo = new ChannelInfo();
//        channelInfo.setUserZoneCode(channelInfoBean.getUserZoneCode());
//        channelInfo.setTcpZoneCode(channelInfoBean.getTcpZoneCode());
//        channelInfo.setIp(channelInfoBean.getIp());
//        channelInfo.setPort(channelInfoBean.getPort());
//        channelInfo.setAppID(channelInfoBean.getId().getAppID());
//        channelInfo.setChannelMode(ChannelMode.getInstance((byte) channelInfoBean.getChannelMode()));
//        channelInfo.setClientVersion(channelInfoBean.getClientVersion());
//        channelInfo.setId(channelInfoBean.getTcpID());
//        channelInfo.setNodeId(channelInfoBean.getNodeID());
//        channelInfo.setSessonId(channelInfoBean.getSessionID());
//        channelInfo.setUserId(channelInfoBean.getId().getUserID());
//        channelInfo.setUuid(channelInfoBean.getId().getChannelUUID());
//        return channelInfo;
//    }
//
//    private static List<ChannelInfo> parse(List<ChannelInfoBean> channelInfoBeanList) {
//        return channelInfoBeanList.stream().map(channelInfoBean -> parse(channelInfoBean))
//                .collect(Collectors.toList());
//    }
//
//    private boolean isServerOnline(ChannelInfo channelInfo) {
//        try {
//            final String tcpServerID = channelInfo.getNodeId();
//            byte[] result = statusStore.get(tcpServerStatusKey(tcpServerID).getBytes());
//            return result != null && result.length > 0;
//        } catch (Exception e) {
//            logger.error(String.format("isSeverOnline user.id : %s  error", channelInfo.getUserId()), e);
//            return true;
//        }
//    }
//
//    private void addChannelCache(ChannelInfo channelInfo, String userID, int appID, int ttl) {
//        String appUserId = UserUtils.getAppUserId(userID, appID);
//        final String listKey = format("%s%s", USER_TCP_CHANNEL_LIST_PREFIX, appUserId);
//        final String singleKey = format("%s%s%s", USER_TCP_CHANNEL_PREFIX, appUserId,
//                channelInfo.getUuid());
//        try {
//            statusStore.sAdd(listKey.getBytes(), channelInfo.getUuid().getBytes());
//            statusStore.expire(listKey.getBytes(), ttl);
//            statusStore.set(singleKey.getBytes(), JsonUtil.toJson(mapper, channelInfo).getBytes(Charset.forName("UTF-8")), ttl);
//        } catch (Exception e) {
//            logger.error("error: ", e);
//        }
//    }
//
//    private String onlineKey(String userID, int appID) {
//        return String.format("%s%s_%d", USER_ONLINE_STATUS, userID, appID);
//    }
//
//    private void signOnline(String userID, int appID) {
//        try {
//            String key = onlineKey(userID, appID);
//            statusStore.set(key, ONLINE);
//        } catch (Exception e) {
//            logger.error("error: ", e);
//        }
//    }
//
//    private void signOffline(String userID, int appID) {
//        try {
//            String key = onlineKey(userID, appID);
//            statusStore.set(key, OFFLINE);
//        } catch (Exception e) {
//            logger.error("error:", e);
//        }
//    }
//
//    private OnlineStatus getOnlineStatus(String userID, int appID) {
//        String status = null;
//        String key = onlineKey(userID, appID);
//        try {
//            status = statusStore.get(key);
//        } catch (Exception e) {
//            logger.error("error:", e);
//        }
//        return OnlineStatus.getOnlineStatus(status);
//    }
//
//
//    public static enum OnlineStatus {
//        ONLINE,
//        OFFLINE,
//        NOTEXISTS;
//
//        public static OnlineStatus getOnlineStatus(String status) {
//            if (StringUtils.isBlank(status)) {
//                return NOTEXISTS;
//            }
//            if (status.equals(ONLINE)) {
//                return ONLINE;
//            }
//            if (status.equals(OFFLINE)) {
//                return OFFLINE;
//            }
//            return NOTEXISTS;
//        }
//    }
//
//}
