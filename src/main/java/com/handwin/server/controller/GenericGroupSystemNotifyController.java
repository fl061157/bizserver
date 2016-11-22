package com.handwin.server.controller;

import com.handwin.bean.Platform;
import com.handwin.entity.ChannelInfo;
import com.handwin.entity.User;
import com.handwin.entity.wrong.SimpleWrongMessage;
import com.handwin.exception.ServerException;
import com.handwin.localentity.Message;
import com.handwin.packet.ChannelMode;
import com.handwin.packet.SystemNotifyPacket;
import com.handwin.protocal.v5.codec.V5GenericPacket;
import com.handwin.protocal.v5.codec.V5PacketHead;
import com.handwin.server.Channel;
import com.handwin.server.ProxyMessageSender;
import com.handwin.server.proto.ChannelAction;
import com.handwin.service.*;
import com.handwin.utils.ChannelUtils;
import com.handwin.utils.Snowflake;
import com.handwin.utils.SystemConstant;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

/**
 * Created by piguangtao on 15/11/26.
 * 新协议处理系统通知 以便能够处理服务器内部模块发送过来的请求
 * 接受方暂时不支持新协议，需要转化为老的协议发送
 * 该接口不根据群组id 向群组的所有的成员发送消息 而是根据指定的接受方用户发送系统通知
 */
@Service
@Controller(value = "/group/system/notify")
public class GenericGroupSystemNotifyController implements ServiceController {
    private static final Logger LOGGER = LoggerFactory.getLogger(GenericGroupSystemNotifyController.class);
    @Autowired
    private UserService userService;

    @Value("${localidc.country.code}")
    private String localCountryCode;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private GenericGroupService genericGroupService;

    @Autowired
    private MessageService messageService;


    @Autowired
    private Snowflake idGenerator;


    @Value("${idc.country.codes}")
    private String idcCountryCodes;

    @Value("${default.country.code}")
    private String defaultCountryCode;

    @Value("${localidc.country.code}")
    private String localIdcCountyCode;

    @Autowired
    private TaskExecutor executorInterBizServers;

    @Autowired
    private IResendMsgToBizServer resendMsgToBizServer;

    @Autowired
    protected ProxyMessageSender proxyMessageSender;

    /**
     * 处理群组系统通知
     * 消息体为map
     * key: push 表示push的内容
     * key: msg  表示消息内容
     *
     * @param channel
     * @param packetHead
     * @param genericPacket
     */
    @Override
    public void handle(Channel channel, V5PacketHead packetHead, V5GenericPacket genericPacket) {

        //通过via 判断消息是否转发过来的
        boolean isForward = false;

        //发送方区和接受方区不相等时，则表示转发
        if (StringUtils.isNotBlank(packetHead.getFrom())) {
            isForward = !packetHead.getFrom().equalsIgnoreCase(packetHead.getToRegion());
        }

        User fromUser = null;

        String toUserIds = packetHead.getTo();
        if (StringUtils.isBlank(toUserIds)) {
            LOGGER.error(String.format("toUserId should not be null"));
            return;
        }

        //消息重发逻辑处理
        if (genericPacket.getPacketHead().getResend()) {
            Long cachedMessageId = messageService.isServerReceived(genericPacket.getPacketHead().getMessageID(),
                    SystemConstant.MSGFLAG_RESENT, fromUser.getId());
            if (null != cachedMessageId) {
                genericGroupService.ack(isForward, channel, genericPacket, cachedMessageId, fromUser);
                return;
            }
        }

        List<User> noLocalUsers = new ArrayList<>();
        final boolean isForwardFinal = isForward;
        Stream.of(toUserIds.split(",")).forEach(toUserId -> {
            User toUser = userService.findById(toUserId, channel.getChannelInfo().getAppID());
            if (null == toUser) {
                LOGGER.error(String.format("toUser should not be null.userId:%s", toUserId));
                return;
            }

            if (StringUtils.isBlank(toUser.getCountrycode())) {
                LOGGER.info("RegionCode Is Empty user.id :{} ", toUser.getId());
                if (!userService.isSystemAccount(toUser)) {
                    return;
                }
                toUser.setCountrycode(localCountryCode);
            }

            boolean isToUserIDC = userService.isLocalUser(toUser.getCountrycode());
            Long serverMessageId = idGenerator.next();
            //接受方在本数据中心
            if (isToUserIDC) {
                //给发送方发送数据
//                Channel toChannel = null;
//                try {
//                    toChannel = channelService.findChannel(toUser);
//                } catch (ServerException e) {
//                    LOGGER.error(String.format("write systemMessage to user.id:%s error", toUser.getId()), e);
//                }

                Map<Platform, List<Channel>> toChannelMap = channelService.findChannelMap(toUser);


                //创建消息
                //需要存储 或者需要客户端确认
                if (packetHead.getStore() || packetHead.getClientReceivedConfirm()) {
                    //需要构建群组消息
                    Message message = genericGroupService.buildGroupSystemNotifyMessage(genericPacket, serverMessageId);
                    messageService.createMessage(packetHead.getFrom(), packetHead.getTo(), message,
                            ChannelUtils.isOffline(toChannelMap) ? com.handwin.message.bean.MessageStatus.UNDEAL : com.handwin.message.bean.MessageStatus.ONLINE,
                            (byte[]) genericPacket.getBodyMap().get(GenericGroupService.GENERIC_GROUP_SYSTEM_NOTIFY_BODY_MSG));
                }
                //发送系统通知

                SystemNotifyPacket notifyPacket = genericGroupService.transToSystemNotify(genericPacket, serverMessageId);
                AtomicBoolean offlinePush = new AtomicBoolean(false);
                if (MapUtils.isNotEmpty(toChannelMap)) {
                    toChannelMap.values().stream().forEach(channels -> {
                        channels.stream().forEach(toChannel -> {
                            ChannelInfo toChannelInfo;
                            if (toChannel != null && (toChannelInfo = toChannel.getChannelInfo()) != null &&
                                    toChannelInfo.getChannelMode() != ChannelMode.SUSPEND) {
                                //发送在线消息
                                try {
                                    if (LOGGER.isDebugEnabled()) {
                                        LOGGER.debug("[generic message]Write System Message To: {}", toChannel.getChannelInfo());
                                    }
                                    byte[] trackBytes = SimpleWrongMessage.encode(toChannelInfo.getAppID(),
                                            channel.getChannelInfo().getUserId(), toChannelInfo.getUserId(), serverMessageId, channel.getTraceId());
                                    toChannel.write(notifyPacket, trackBytes, ChannelAction.SEND);
                                } catch (Throwable e) {
                                    LOGGER.error(e.getMessage(), e);
                                    if (toChannel.getChannelInfo().findPlatform() == Platform.Mobile) {
                                        offlinePush.set(true);
                                    }
                                }
                            }
                        });

                    });

                }

                if (ChannelUtils.isOffline(toChannelMap) || offlinePush.get()) {
                    genericGroupService.handleOfflineMessage(genericPacket, notifyPacket);
                }

                genericGroupService.ack(isForwardFinal, channel, genericPacket, serverMessageId, toUser);
            }
        });


        //处理非本区的用户
        if (!isForward) {
            if (null != noLocalUsers && noLocalUsers.size() > 0) {
                String traceId = MDC.get("TraceID");
                String prefix = MDC.get("PREFIX");
                //按照不同的国家进行转发
                noLocalUsers.stream()
                        .map(User::getCountrycode)
                        .filter(countryCode -> StringUtils.isNotBlank(countryCode)
                                && !userService.isLocalUser(countryCode))
                        .map(userCountryCode -> idcCountryCodes.contains(userCountryCode) ? userCountryCode : defaultCountryCode)
                        .distinct()
                        .forEach(countryCode -> executorInterBizServers.execute(() -> {
                            MDC.put("TraceID", traceId);
                            MDC.put("PREFIX", prefix);
                            try {
                                //把消息写入临时消息表中
                                resendMsgToBizServer.saveGroupMsgForResend(packetHead.getFrom(), UUID.randomUUID().toString(), countryCode, (String) packetHead.getHead("group_id"), genericPacket.getSrcMsgBytes());
                                sendMsgToOtherBizServer(proxyMessageSender, countryCode, genericPacket);
                            } finally {
                                MDC.remove("TraceID");
                                MDC.remove("PREFIX");
                            }
                        }));

            }
        }
    }

    private void sendMsgToOtherBizServer(ProxyMessageSender sender, String countryCode, V5GenericPacket packet) {
        executorInterBizServers.execute(() -> sender.writeV5Protocol(countryCode, packet.getSrcMsgBytes()));
    }
}
