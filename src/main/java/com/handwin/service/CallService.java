package com.handwin.service;

import com.handwin.bean.Platform;
import com.handwin.entity.*;
import com.handwin.exception.ServerException;
import com.handwin.message.bean.MessageStatus;
import com.handwin.packet.*;
import com.handwin.persist.StatusStore;
import com.handwin.server.Channel;
import com.handwin.service.impl.ConversationServiceImpl;
import com.handwin.utils.ChannelUtils;
import com.handwin.utils.Snowflake;
import com.handwin.utils.SystemConstant;
import com.handwin.utils.UserUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.PropertyNamingStrategy;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.String.format;
import static java.lang.String.valueOf;


@Service
public class CallService extends AbstractCallService implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(CallService.class);
    private final static String CALL_ROOM_ID_PREFIX = "call_";
    private final static String CALL_UDP_INFO_PREFIX = "call_udp_info_roomId_";
    private static ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    protected ConversationServiceImpl conversationService;
    @Autowired
    protected TcpSessionService onlineStatusService;
    @Autowired
    protected CallStatusService callStatusService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    protected UserService userService;
    @Autowired
    protected MessageService messageService;
    @Autowired
    protected IUDPAssignService udpAssignService;
    @Value("${call.offline.msg.time.delay}")
    protected int callOfflineMsgDelay;

    @Value("${push.waitack.msg.time.delay.millisecond}")
    protected int pushMessageWaitAckDelay;

    @Autowired
    private Snowflake snowflake;

    @Autowired
    @Qualifier(value = "statusClusterStoreImpl")
    private StatusStore statusStore;

//   No Need
//    @Autowired
//    private UserCallSmSendService userCallSmSendService;

    @Autowired
    private DelayTaskService delayTaskService;

    @Autowired
    private MessageSource messageSource;

    @Override
    public void afterPropertiesSet() throws Exception {
        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setPropertyNamingStrategy(
                PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
        objectMapper.configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
    }


    public void call(final Channel fromChannel, final CallPacket packet, final Map<Platform, List<Channel>> toChannelMap,
                     final User fromUser, final User toUser) {
        if (StringUtils.isBlank(packet.getRoomId())) {
            packet.setRoomId(createCallRoomId());
        }
        final String roomID = packet.getRoomId();

        if (logger.isDebugEnabled()) {
            logger.debug("[CallHandler call].write response.roomId:{}", roomID);
        }

        callStatusService.setCallStartTime(roomID, System.currentTimeMillis());
        callStatusService.setCallFromUser(roomID, fromUser.getId());
        callStatusService.setCallInfo(roomID, fromUser.getCountrycode(), fromUser.getId(), toUser.getId());
        if (conversationService.isInBlackSheet(toUser.getId(), fromUser.getId(), fromChannel.getChannelInfo().getAppID())) {
            if (logger.isInfoEnabled()) {
                logger.info("[CallHandler call].fromUser:{} Is In toUser:{} Black List",
                        fromUser.getId(), toUser.getId());
            }
            return;
        } else {
            fromChannel.write(buildCallResponsePacket(packet.getPeerName(),
                    CallStatus.CALL_SERVER_RECEIVED, null, packet, false));
        }
        //接听方处于免打扰的时间段内，且接听方的通道状态非前台时，给呼叫发起方发送“接听方处于免打扰”的系统通知

        if (ChannelUtils.isNoForgeRound(toChannelMap) && UserUtils.isUserHideMessage(toUser.getHideTime(), toUser.getTimezone())) {
            handleReceiverNoDisturb(toUser, fromChannel, roomID);
        }
        callStatusService.setCallStatus(packet.getRoomId(), valueOf(packet.getCallStatus().id()));

        if (MapUtils.isNotEmpty(toChannelMap)) {
            for (Map.Entry<Platform, List<Channel>> entry : toChannelMap.entrySet()) {
                List<Channel> channelList = entry.getValue();
                if (CollectionUtils.isNotEmpty(channelList)) {
                    channelList.stream().filter(toChannel -> toChannel != null).forEach(toChannel -> {
                        if (logger.isDebugEnabled()) {
                            logger.debug("callPacket is {}", packet);
                        }
                        toChannel.write(buildCallResponsePacket(fromChannel.getChannelInfo().getUserId(),
                                packet.getCallStatus(), null, packet, true));
                        submitDelayTask(pushMessageWaitAckDelay, packet, fromUser, toUser, fromChannel,
                                toChannel, new AtomicInteger(0));

                        if (logger.isInfoEnabled()) {
                            logger.info("[CallHandler call].send call req to receiver.roomId:{} , fromChanelUUID:{} , toChannelUUID:{}", roomID, fromChannel.getChannelInfo().getUuid(),
                                    toChannel.getChannelInfo().getUuid());
                        }
                    });
                }
            }
        } else {
            userOfflineHandle(fromUser, toUser, packet,
                    fromChannel.getChannelInfo().getAppID(), fromChannel.getTraceId());
        }
    }


    private void submitDelayTask(int pushMessageWaitAckDelay, CallPacket packet, User fromUser, User toUser,
                                 Channel fromChannel, Channel toChannel, AtomicInteger counter) {
        delayTaskService.submitDelayTask(pushMessageWaitAckDelay, () -> {
            try {
                if (callStatusService.hasCallReceive(packet.getRoomId(), fromUser.getId(), toUser.getId())) {
                    if (logger.isInfoEnabled()) {
                        logger.info("delayTask roomID:{} call has been received!", packet.getRoomId());
                    }
                    return;
                }
                Channel newToChannel = channelService.findChannel(toUser);
                if (newToChannel != null && newToChannel.getChannelInfo().getUuid().equals(toChannel.getChannelInfo().getUuid()) && counter.incrementAndGet() < DelayTaskService.MAX_TRY_TIMES) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("delayTask roomID:{} not received already tryTimes:{} ", packet.getRoomId(), counter.get());
                    }
                    submitDelayTask(pushMessageWaitAckDelay, packet, fromUser,
                            toUser, fromChannel, toChannel, counter);
                } else {
                    if (logger.isInfoEnabled()) {
                        logger.info("delayTask roomID:{}  ,counter:{} , newToChannel:{} , oldToChannel:{} offline deal "
                                , packet.getRoomId(), counter.get(), newToChannel, toChannel);
                    }
                    userOfflineHandle(fromUser, toUser, packet,
                            fromChannel.getChannelInfo().getAppID(), fromChannel.getTraceId());
                }
            } catch (Throwable e) {
                logger.error(e.getMessage(), e);
            }
        });


    }


    public void accept(final Channel fromChannel, final CallPacket packet, final Map<Platform, List<Channel>> toChannelMap,
                       User fromUser, User toUser) {
        if (MapUtils.isEmpty(toChannelMap)) { //给发起方 发送消息
            if (logger.isInfoEnabled()) {
                logger.info("[CallHandler accept],userId:{} is Offline.roomId:{},status:{}",
                        toUser.getId(), packet.getRoomId(), packet.getCallStatus());
            }
            fromChannel.write(buildCallResponsePacket(fromUser.getId(), CallStatus.PEER_OFFLINE,
                    packet.getStatus(), packet, true));
        } else {
            notifyOnAccept(fromChannel, packet, toChannelMap, fromUser, toUser);
        }
    }

    public void notifyOnAccept(final Channel fromChannel, final CallPacket packet, final Map<Platform, List<Channel>> toChannelMap,
                               User fromUser, User toUser) {

        if (null != packet.getStatus() && packet.getStatus() == 0x04) {
            if (logger.isInfoEnabled()) {
                logger.info("[CallHandler call at onetime] from.id:{} , to.id:{}", fromUser.getId(), toUser.getId());
            }
            return;
        }

        if (MapUtils.isNotEmpty(toChannelMap)) {

            toChannelMap.values().stream().forEach(channels -> channels.stream().forEach(
                    toChannel -> toChannel.write(buildCallResponsePacket(fromUser.getId(), packet.getCallStatus(),
                            packet.getStatus(), packet, true))
            ));


        }
    }

    public void reject(final Channel fromChannel, final CallPacket packet, final Map<Platform, List<Channel>> toChannelMap,
                       User fromUser, User toUser) {

        if (MapUtils.isNotEmpty(toChannelMap)) {

            toChannelMap.values().stream().forEach(channels -> channels.stream().forEach(
                    toChannel -> toChannel.write(buildCallResponsePacket(fromUser.getId(), CallStatus.REJECT,
                            packet.getStatus(), packet, true))
            ));

        } else {
            if (logger.isInfoEnabled()) {
                logger.info("[CallHandler reject].peer is offline roomID:{}",
                        packet.getRoomId());
            }
        }
    }

    public void hangup(final Channel fromChannel, final CallPacket packet, final Map<Platform, List<Channel>> toChannelMap,
                       User fromUser, User toUser) {
        if (conversationService.isInBlackSheet(toUser.getId(), fromUser.getId(), fromUser.getAppId())) {
            if (logger.isInfoEnabled()) {
                logger.info("[CallHandler hangup].fromUser:{} Is In toUser:{} Black List",
                        fromUser.getId(), toUser.getId());
            }
            return;
        }

        if (MapUtils.isNotEmpty(toChannelMap)) {

            toChannelMap.values().stream().forEach(channels -> channels.stream().forEach(toChannel ->
                    toChannel.write(buildCallResponsePacket(fromUser.getId(), CallStatus.HANGUP, packet.getStatus(), packet, true))));

        } else {

            if (logger.isInfoEnabled()) {
                logger.info("[CallHandler hangup].toUser:{} toChannel is null", toUser.getId());
            }

        }

        //消息直接入库 呼叫方主动挂断 且接受方没有呼叫接听的相关指令时，missed call消息直接入库
        String callReqFromUser = callStatusService.getCallFromUser(packet.getRoomId());
//        if (null != packet.getStatus()
//                && (CallHandupType.OPPOSITE_UN_ACCECPT_OVERTIME.getValue() == packet.getStatus() || CallHandupType.OPPOSITE_UN_ACCECPT_ACTIVE.getValue() == packet.getStatus())) {

        //呼叫发起方发起的挂断，且呼叫方没有呼叫相关的指令，则消息直接入库
        if (fromUser.getId().equals(callReqFromUser) && (!callStatusService.hasCallReceive(packet.getRoomId(), toUser.getId(), fromUser.getId()))) {
            if (logger.isInfoEnabled()) {
                logger.info("[missed call save] from:{},to:{},roomId:{}", fromUser.getId(), toUser.getId(), packet.getRoomId());
            }

            //未接通挂断，消息立即入库
            //呼叫方未接通挂断时，立即入库
            callStatusService.setCallFromHangupUnTalkedStatus(packet.getRoomId(), fromUser.getId(),
                    toUser.getId(), valueOf(packet.getCallStatus()));
            //获取呼叫的类型
            byte[] callStatusBytes = callStatusService.getCallStatus(packet.getRoomId());
            logger.debug("[push missed call]. get. roomId:{}, result is not null? {}", packet.getRoomId(), null != callStatusBytes);
            if (null != callStatusBytes) {
                String callStatus = new String(callStatusBytes, Charset.forName("UTF-8"));
                //消息立即入库，需要根据挂断产生消息体
                Long startTime = null;
                try {
                    startTime = callStatusService.getCallStartTime(packet.getRoomId());
                } catch (Exception e) {
                    logger.warn("fails to get call startTime.roomId:", packet.getRoomId(), e);
                }

                try {
                    messageService.createCallMessage(fromUser.getId(), packet, ChannelUtils.isOffline(toChannelMap) ? MessageStatus.ONLINE : MessageStatus.UNDEAL,
                            CallStatus.getInstance(Integer.valueOf(callStatus)), null == startTime ? System.currentTimeMillis() : startTime);
                } catch (NumberFormatException e) {
                    logger.error("", e);
                } catch (ServerException e) {
                    logger.error("", e);
                }
                logger.debug("[missed call msg] save immediately when hangup. roomId:{}", packet.getRoomId());
            }
        }

    }

    public void busyCommand(final Channel fromChannel, final CallPacket packet, final Map<Platform, List<Channel>> toChannelMap,
                            User toUser) {
        if (MapUtils.isNotEmpty(toChannelMap)) {

            toChannelMap.values().stream().forEach(channels -> channels.stream().forEach(toChannel ->
                    toChannel.write(buildCallResponsePacket(fromChannel.getChannelInfo().getUserId(), packet.getCallStatus(), packet.getStatus(), packet, true))
            ));

        } else {
            if (logger.isInfoEnabled()) {
                logger.info("[CallHandler busyCommand].toUser:{} , channel is null", toUser.getId());
            }
        }
    }

    public void transCommand(final Channel fromChannel, final CallPacket packet, final Map<Platform, List<Channel>> toChannelMap,
                             User fromUser, User toUser) {
        if (MapUtils.isNotEmpty(toChannelMap)) {
            if (conversationService.isInBlackSheet(toUser.getId(), fromUser.getId(), fromUser.getAppId())) {
                if (logger.isInfoEnabled()) {
                    logger.info("[CallHandler transCommand].fromUser:{} Is In toUser:{} Black List",
                            fromUser.getId(), toUser.getId());
                }
                return;
            }

            toChannelMap.values().stream().forEach(channels -> channels.stream().forEach(
                    toChannel -> toChannel.write(buildCallResponsePacket(fromChannel.getChannelInfo().getUserId(),
                            packet.getCallStatus(), packet.getStatus(), packet, true))
            ));

        } else {

            if (logger.isInfoEnabled()) {
                logger.info("[CallHandler transCommand].toUser:{} toChannel is null ", toUser.getId());
            }

        }
    }

    public void received(final Channel fromChannel, final CallPacket packet, final Map<Platform, List<Channel>> toChannelMap,
                         User fromUser, User toUser) {
        ChannelInfo fromChannelInfo = fromChannel.getChannelInfo();
        if (fromChannelInfo != null && MapUtils.isNotEmpty(toChannelMap)) {

            ChannelInfo toChannelInfo = ChannelUtils.chooseBestChannel(toChannelMap);
            if (toChannelInfo != null) {
                final UDPServerPacket2 udpSeverPacket = getUdpServerPacket(fromChannelInfo, packet, toChannelInfo, fromUser, toUser);
                if (udpSeverPacket == null) {
                    logger.error("[CallHandler received],udpSeverPacket null room.id:{} ", packet.getRoomId());
                    return;
                }

                toChannelMap.values().stream().forEach(channels -> channels.stream().forEach(
                        toChannel -> {
                            toChannel.write(buildCallResponsePacket(fromChannel.getChannelInfo().getUserId(),
                                    CallStatus.RECEIVED, packet.getStatus(), packet, false));  //回复Received

                            if (logger.isDebugEnabled()) {
                                logger.debug("roomId:{},udpServer:{}", packet.getRoomId(), udpSeverPacket);
                            }
                            toChannel.write(udpSeverPacket.getUDPServerPacket(UserUtils.getAppUserId(toUser.getId(),
                                    valueOf(toUser.getAppId()))));
                        }
                ));

                fromChannel.write(udpSeverPacket.getUDPServerPacket(UserUtils.getAppUserId(fromUser.getId(),
                        valueOf(fromUser.getAppId()))));
            }


        } else {
            logger.error("[CallHandler received],FromChannelInfo:{} Or ToChannelInfo Is Not Online",
                    fromChannelInfo);
        }
    }


    public void userOfflineHandle(final User fromUser, final User toUser, final CallPacket packet,
                                  final int appID, final String traceId) {
        //采用延时任务处理
        delayTaskService.submitDelayTask(callOfflineMsgDelay * 1000, () -> {
            try {
                if (callStatusService.hasCallReceive(packet.getRoomId(), fromUser.getId(), toUser.getId())) {
                    return;
                }
                if (callStatusService.hasHangupUnTalked(packet.getRoomId(), fromUser.getId(), toUser.getId())) {
                    logger.debug("[cancel ackPush ].offline ackPush in wheel time.  callFrom:{},callReceiver:{}.traceId:{}.roomId:{}.in db immediately.", fromUser.getId(), toUser.getId(), traceId, packet.getRoomId());
                    return;
                }
                logger.debug("[call message save].push into db.traceId:{},callFrom:{},callReceiver:{},callStatus:{}",
                        traceId, fromUser.getId(), toUser.getId(), packet.getCallStatus());
                Long startTime = null;
                try {
                    startTime = callStatusService.getCallStartTime(packet.getRoomId());
                } catch (Exception e) {
                    logger.warn("fails to get call startTime. roomId:{}", packet.getRoomId(), e);
                }
                messageService.createCallMessage(fromUser.getId(), packet, MessageStatus.UNDEAL,
                        packet.getCallStatus(), null == startTime ? System.currentTimeMillis() : startTime);
            } catch (Throwable e) {
                logger.error("message service create message error:" + e.getMessage(), e);
            }
        });

        logger.debug("[push missed call]. save. roomId:{}", packet.getRoomId());

        if (!conversationService.isInGreySheet(toUser.getId(), fromUser.getId(), fromUser.getAppId())) {
            sendCallPush(fromUser, toUser, packet, appID, traceId);
        }
    }


    private void sendCallPush(User fromUser, User toUser, CallPacket callPacket, int appID, String traceId) {
        UserToken toUserToken = null;
        try {
            toUserToken = userService.getTokenInfo(toUser.getId(), (int) appID);
        } catch (Exception e) {
            logger.error("[CallService sendCallPush]: getUserToken userId:{}, Error:{}", toUser.getId(), e);
        }
        if (toUserToken == null) {
            logger.error("[CallService sendCallPush]: getUserToken Null userId:{} ", toUser.getId());
            //发送方在接受方的通讯录中，并且是第一条未读的消息
            //没有token 需要补送短信
//            userCallSmSendService.sendCallSm(toUser, fromUser, "call");
            return;
        }
        Integer unReadCount = null;
        if (toUserToken.getDeviceType() == DeviceType.IOS.getValue()) {
            try {
                unReadCount = messageService.updateUnreadLocalCount(toUser.getId(), false)
                        .getCounter().get().intValue();
                //呼叫push时，本次呼叫也计入数字
                unReadCount++;
            } catch (Exception e) {
                logger.error(format("[CallService sendCallPush]: getUserUnReadCount Error userId:%s , Error:%s",
                        toUser.getId(), e.getMessage()), e);
            }
        }
        String nickName = "";
        try {
            nickName = userService.getFriendNickname(fromUser, toUser.getId(), toUser.getAppId());
        } catch (ServerException e) {
            logger.error(String.format("handleOfflineMessage fromUser:%s, toUser:%s",
                    fromUser, toUser), e);
        }
        messageService.pushCall(fromUser, toUser, toUserToken, callPacket, unReadCount, traceId, nickName);

        //Android且不是小米的情况下，发送短信
        //走未接电话发送的流程
//        if (toUserToken.getDeviceType() == DeviceType.ANDRIOD.getValue()) {
//            if (!"7".equals(toUserToken.getToken())) {
//                userCallSmSendService.sendCallSm(toUser, fromUser, "call");
//            }
//        }

        //IOS需要在4s、8s、12s普通的推送，便于震动提示
        if (toUserToken.getDeviceType() == DeviceType.IOS.getValue()) {
            //先只有中国的发送
            final String nickNameFinal = nickName;
            final UserToken toUserTokenFinal = toUserToken;
            delayTaskService.submitDelayTask(1000 * 4, () -> sendWaitingForAccept(fromUser, toUser, callPacket, nickNameFinal, toUserTokenFinal, traceId));
            delayTaskService.submitDelayTask(1000 * 8, () -> sendWaitingForAccept(fromUser, toUser, callPacket, nickNameFinal, toUserTokenFinal, traceId));

            delayTaskService.submitDelayTask(1000 * 12, () -> sendWaitingForAccept(fromUser, toUser, callPacket, nickNameFinal, toUserTokenFinal, traceId));

        }

        logger.info("send push call.roomId:{}", callPacket.getRoomId());
    }


    public void sendWaitingForAccept(User fromUser, User toUser, CallPacket callPacket, String sendName, UserToken toUserToken, String traceId) {

        //用户是否以回复该通话
        if (callStatusService.hasCallReceive(callPacket.getRoomId(), fromUser.getId(), toUser.getId())) {
            logger.debug("[call push]. from:{},to:{},roomId:{} toUser has replied to the call.", fromUser.getId(), toUser.getId(), callPacket.getRoomId());
            return;
        }
        if (callStatusService.hasHangupUnTalked(callPacket.getRoomId(), fromUser.getId(), toUser.getId())) {
            logger.debug("[cancel ackPush ].offline ackPush in wheel time.  callFrom:{},callReceiver:{}.traceId:{}.roomId:{}. missed has been saveed in db yet", fromUser.getId(), toUser.getId(), traceId, callPacket.getRoomId());
            return;
        }

        String text = messageSource.getMessage("call.waiting.accept", new String[]{sendName}, toUser.getLocale());

        //补推 “正在等待接听的消息时” 呼叫消息还没有入库 ios的离线消息数目 需要加1            return;

        //需要设置消息的有效期
        messageService.pushText(fromUser, toUser, toUserToken, text, traceId, PushMsgMqBean.NoticeType.NO_ALERT, SystemConstant.PUSH_TIPTYPE_DONOT_ENTER_INTERFACE, true, 1000L);


    }


    @Override
    public CallPacket buildCallResponsePacket(String peerName, CallStatus callStatus, Integer status,
                                              CallPacket callPacket, boolean includeUserData) {
        CallResponsePacket callResponsePacket = new CallResponsePacket();
        callResponsePacket.setPeerName(peerName);
        callResponsePacket.setCallStatus(callStatus);
        callResponsePacket.setStatus(status);
        callResponsePacket.setRoomId(callPacket.getRoomId());
        if (includeUserData) {
            callResponsePacket.setUserData(callPacket.getUserData());
        }
        return callResponsePacket;
    }


    private UDPServerPacket2 formUdpPacket(final boolean isSystemAccount, final String fromeIp, final String toIp,
                                           final CallPacket packet, String fromUserNameMd5, String toUserNameMd5) {
        if (packet instanceof GameCallPacket && packet.getRoomId() == null) {
            logger.warn("game call from {} to {} not contains roomid, maybe it's a neither audio nor video call {}",
                    fromUserNameMd5, toUserNameMd5, packet);
            return null;
        }
        return formUdpPacketByIpStrategy(isSystemAccount, fromeIp, toIp, packet.getRoomId(),
                fromUserNameMd5, toUserNameMd5, packet.getPacketHead().getAppId());
    }

    private UDPServerPacket2 formUdpPacketByIpStrategy(final boolean isSystemAccount, final String fromeIp,
                                                       final String toIp, String roomId,
                                                       String fromUserNameMd5, String toUserNameMd5, Integer appID) {
        logger.debug("[udpResponsePacket].ip service. roomId:{}", roomId);
        UdpStrategyQuery udpStrategyQuery = new UdpStrategyQuery();
        udpStrategyQuery.setIp1(fromeIp);
        udpStrategyQuery.setIp2(toIp);
        if (isSystemAccount) {
            udpStrategyQuery.setNet1("-1");
            udpStrategyQuery.setNet2("-1");
        }
        try {
            udpStrategyQuery.setAppId(appID);
            UdpStrategy2Result udpStrategyResult = udpAssignService.getUdpStrategy(udpStrategyQuery);
            if (null == roomId) {
                roomId = createCallRoomId();
            }

            UDPServerPacket2 udpServerPacket2 = new UDPServerPacket2();

            UDPServerPacket packet1 = new UDPServerPacket();
            packet1.setFlag(udpStrategyResult.getP2p());
            packet1.setRoomId(roomId);
            packet1.setUdpInfo(transUdpInfos(udpStrategyResult.getUser1Udps()));

            udpServerPacket2.setId1(fromUserNameMd5);
            udpServerPacket2.setUdpServerPacket1(packet1);


            UDPServerPacket packet2 = new UDPServerPacket();
            packet2.setFlag(udpStrategyResult.getP2p());
            packet2.setRoomId(roomId);
            packet2.setUdpInfo(transUdpInfos(udpStrategyResult.getUser2Udps()));

            udpServerPacket2.setId2(toUserNameMd5);
            udpServerPacket2.setUdpServerPacket2(packet2);

            return udpServerPacket2;
        } catch (Exception e) {
            logger.error("[CallHandler received]udpResponsePacket .fails from ip service. roomId:{}.", roomId);
            return null;
        }
    }

    private UDPServerPacket.UdpInfo[] transUdpInfos(UdpStrategy2Result.UDPInfo[] userstrategyUdpInfos) {
        UDPServerPacket.UdpInfo[] result = null;
        if (null != userstrategyUdpInfos && userstrategyUdpInfos.length > 0) {
            result = new UDPServerPacket.UdpInfo[userstrategyUdpInfos.length];
            for (int i = 0; i < userstrategyUdpInfos.length; i++) {
                UdpStrategy2Result.UDPInfo udpInfo = userstrategyUdpInfos[i];
                UDPServerPacket.UdpInfo serverPacketUdpInfo = new UDPServerPacket.UdpInfo();
                serverPacketUdpInfo.setIp(udpInfo.getUdpHost());
                serverPacketUdpInfo.setPort(Integer.valueOf(udpInfo.getUdpPort()));
                serverPacketUdpInfo.setNodeId(udpInfo.getNodeId());
                result[i] = serverPacketUdpInfo;
            }
        }
        return result;
    }


    /**
     * 当呼叫接受方处于免打扰时，给呼叫发起方发送系统通知"接听方处于免打扰"
     *
     * @param receiver    呼叫接受方
     * @param fromChannel 呼叫请求
     * @param roomId      呼叫标示
     */
    public void handleReceiverNoDisturb(User receiver, Channel fromChannel, String roomId) {
        String userID = fromChannel.getChannelInfo().getUserId();
        //给呼叫发起方，发送接听方用户处于免打扰的系统通知
        SystemNotifyPacket notifyPackage = new SystemNotifyPacket();
        notifyPackage.setFrom(receiver.getId());
        notifyPackage.setTo(userID);
        notifyPackage.setMsgType((byte) 0x05);
        notifyPackage.setServeType((byte) 0x00);
        notifyPackage.setMsgId(snowflake.next());
        notifyPackage.setPushContentLength(0);
        Map<String, Object> noDistrubData = new HashMap<String, Object>();
        noDistrubData.put("type", "no_disturb");

        Map<String, String> infoData = new HashMap<String, String>();
        infoData.put("from", receiver.getId());
        infoData.put("to", userID);
        infoData.put("room_id", roomId);
        noDistrubData.put("info", infoData);

        try {
            String content = objectMapper.writeValueAsString(noDistrubData);
            notifyPackage.setMessageBody(content);
        } catch (Exception e) {
            logger.error("Fails to parse exception.", e);
        }
        fromChannel.write(notifyPackage);
        if (logger.isDebugEnabled()) {
            logger.debug("receiver no disturb.from:{}.to:{},roomId:{}",
                    userID, receiver.getId(), roomId);
        }

    }


    /**
     * A 呼叫B，则fromUser是B
     *
     * @param fromUser
     * @param toUserId
     * @param packet
     */
    public void handleCallReceiveStatus(User fromUser, String toUserId, CallPacket packet) {
        //系统账号（小秘书）不需要处理
        if (userService.isSystemAccount(fromUser)) return;
        // A Call B  B Receive  B IDC 一定是  B 所在 定时任务的 RDC 
        callStatusService.setCallReceiverReceivedStatus(fromUser.getId(), toUserId, packet);
    }

    public String createCallRoomId() {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String padding = uuid.substring(0, 64 - CALL_ROOM_ID_PREFIX.length() - uuid.length());
        return CALL_ROOM_ID_PREFIX + padding + uuid;
    }


    private UDPServerPacket2 getUdpServerPacket(final ChannelInfo fromChannelInfo, final CallPacket packet, final ChannelInfo toChannelInfo,
                                                final User fromUser, final User toUser) {
        UDPServerPacket2 udpSeverPacket = null;
        String roomId = packet.getRoomId();
        if (null != roomId && !"".equals(roomId)) {
            try {
                udpSeverPacket = getUdpServerInfoFromCache(packet.getRoomId());
            } catch (Exception e) {
                logger.debug("fails to get udpServer info. roomId:{}", packet.getRoomId(), e);
            }
        }

        if (null == udpSeverPacket) {
            boolean isSystemAccount = userService.isSystemAccount(fromUser);
            try {
                udpSeverPacket = formUdpPacket(isSystemAccount, fromChannelInfo.getIp(), toChannelInfo.getIp(),
                        packet, UserUtils.getAppUserId(fromUser.getId(), valueOf(fromUser.getAppId())),
                        UserUtils.getAppUserId(toUser.getId(), valueOf(toUser.getAppId())));
                storeUdpServerInfo(roomId, udpSeverPacket);
            } catch (Throwable e) {
                logger.error("[CallHandler received],error:" + e.getMessage(), e);
            }
        } else {
            logger.debug("get udp server from cache. roomId:{},udpServerPacket:{}", roomId, udpSeverPacket);
        }

        return udpSeverPacket;

    }

    protected void storeUdpServerInfo(String roomId, UDPServerPacket2 udpServerPacket2) {
        if (null == roomId || null == udpServerPacket2) return;
        try {
            String json = objectMapper.writeValueAsString(udpServerPacket2);
            statusStore.set(getCallUdpInfoKey(roomId).getBytes(SystemConstant.CHARSET_UTF8), json.getBytes(SystemConstant.CHARSET_UTF8), 120);
            logger.debug("store udp info. roomId:{},udpServerPacket:{}", roomId, udpServerPacket2);
        } catch (Exception e) {
            logger.warn("fails to parse updServerPacket to json. udpServerPacket{}", udpServerPacket2, e);
        }
    }

    public UDPServerPacket2 getUdpServerInfoFromCache(String roomId) {
        if (null == roomId) return null;
        UDPServerPacket2 result = null;
        String udpJson = null;
        try {
            udpJson = statusStore.get(getCallUdpInfoKey(roomId));
        } catch (Exception e) {
            logger.error("getUdpServerInfoFromCache Error roomId:{} ", roomId, e);
        }
        if (StringUtils.isNotBlank(udpJson)) {
            try {
                result = objectMapper.readValue(udpJson, UDPServerPacket2.class);
                logger.debug("udpServerInfo from cache. roomId:{},udpServerPacket:{}", roomId, result);
            } catch (IOException e) {
                logger.warn("fails to parse udp json to udp package. udp:{}", udpJson, e);
            }
        }
        return result;
    }

    private String getCallUdpInfoKey(String roomId) {
        return format("%s_%s", CALL_UDP_INFO_PREFIX, roomId);
    }


    public void cleanCallRoomCache(String roomId) {
        if (null == roomId) return;
        callStatusService.cleanCallRoom(roomId);
    }


//    /**
//     * 解决IOS 已发布版本2.0
//     *
//     * @param packet
//     * @param toChannel
//     */
//    public void handleUserCallHangupUnaccept(CallPacket packet, Channel toChannel, User fromUser, User toUser) {
//        //给接受方（IOS版本 2.0.0补发一条 推送通知，解决IOS 本地推送自动消息，push栏不能保存未接通电话记录问题）
//        //发送方 主动挂断（未接通挂断）
//        if (null != packet.getStatus() && (CallHandupType.OPPOSITE_UN_ACCECPT_OVERTIME.getValue() == packet.getStatus() || CallHandupType.OPPOSITE_UN_ACCECPT_ACTIVE.getValue() == packet.getStatus())) {
//            //接受方 处于voip状态下才进行
//            if (null != toChannel && ChannelMode.SUSPEND.equals(toChannel.getChannelInfo().getChannelMode())) {
//                UserBehaviouAttr userBehaviouAttr = userService.getUserBehaviouAttr(toUser.getId(), toUser.getAppId());
//                if (null != userBehaviouAttr) {
//                    String clientVersion = userBehaviouAttr.getClientVersion();
//
//                    //接受方是IOS版本，并且是chatgame-2.0.0版本，才补发通知
//                    if (StringUtils.isNotBlank(clientVersion) && "chatgame-2.0.0".equalsIgnoreCase(clientVersion.trim())) {
//                        //IOS版本
//                        UserToken userToken = userService.getTokenInfo(toUser.getId(), toUser.getAppId());
//                        if (null != userToken && SystemConstant.USER_TOKEN_DEVICE_TYPE_IOS == userToken.getDeviceType()) {
//                            //补发push通知 不存储，不计入离线消息,走系统通知
//                            SystemNotifyPacket systemNotifyPacket = new SystemNotifyPacket();
//                            systemNotifyPacket.setServeType(SystemNotifyPacket.SERVICE_TYPE_NEED_PUSH);
//                            String nickName = userService.getFriendNickname(fromUser, toUser.getId(), toUser.getAppId());
//                            systemNotifyPacket.setFrom(fromUser.getId());
//                            systemNotifyPacket.setTo(toUser.getId());
//                            String template = SystemConstant.IOS_PUSH_VIDEO_TEMPLATE.trim();
//                            String pushContent = messageSource.getMessage(template, new String[]{nickName}, toUser.getLocale());
//                            systemNotifyPacket.setPushContentBody(pushContent);
//                            systemNotifyPacket.setMessageBody("");
//
//                            systemNotifyPacket.setCmsgId(UUID.randomUUID().toString());
//                            messageService.pushText(systemNotifyPacket, toUser, userToken, packet.getRoomId());
//                            logger.debug("[call plain push]. toUser:{},packet:{},systemNotify:{}", toUser, packet, systemNotifyPacket);
//                        }
//                    }
//                }
//            }
//        }
//    }

    public boolean isCleanCallRoomResource(CallStatus callStatus) {
        boolean result = false;
        switch (callStatus) {
            case REJECT:
            case BUSY:
            case HANGUP:
                result = true;
                break;
            default:
                break;
        }
        return result;
    }

    public boolean isSetCallRoomInfo(CallStatus callStatus) {
        boolean result = false;
        switch (callStatus) {
            case VIDEO_REQUEST:
            case AUDIO_REQUEST:
                result = true;
                break;
            default:
                break;
        }
        logger.debug("callStatus:{} result:{}", callStatus, result);
        return result;
    }

}