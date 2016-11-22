package com.handwin.server.handler;

import com.google.common.annotations.VisibleForTesting;
import com.handwin.bean.Platform;
import com.handwin.bean.RichMessageInfo;
import com.handwin.codec.PacketCodecs;
import com.handwin.entity.*;
import com.handwin.entity.wrong.SimpleWrongMessage;
import com.handwin.exception.ServerException;
import com.handwin.localentity.Message;
import com.handwin.localentity.MessageType;
import com.handwin.packet.*;
import com.handwin.packet.v5.V5SimpleMessagepacket;
import com.handwin.rabbitmq.MessageBuilder;
import com.handwin.server.Channel;
import com.handwin.server.ProxyMessageSender;
import com.handwin.server.proto.BaseResponseMessage;
import com.handwin.server.proto.ChannelAction;
import com.handwin.service.*;
import com.handwin.utils.ChannelUtils;
import com.handwin.utils.MessageUtils;
import com.handwin.utils.SystemConstant;
import com.handwin.utils.VersionUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class SimpleMessageHandler<T extends SimpleMessagePacket> extends AbstractHandler<T> {

    private static final Logger logger = LoggerFactory.getLogger(SimpleMessageHandler.class);
    @VisibleForTesting
    @Value("${user.sent.ms.cmsgid.ttl}")
    public int cmgIdttl;
    private int messagePacketType = messagePacketType();
    @Autowired
    private ConversationService conversationService;
    @Autowired
    private MessageService messageService;
    @Autowired
    private ChannelService channelService;
    @Autowired
    private GroupService groupService;

    @Value("${idc.country.codes}")
    private String idcCountryCodes;

    @Value("${default.country.code}")
    private String defaultCountryCode;

    @Value("${localidc.country.code}")
    private String localIdcCountyCode;


    @Autowired
    private PacketCodecs packetCodecs;

    @Autowired
    private IResendMsgToBizServer resendMsgToBizServer;

    @Autowired
    private MessageBuilder messageBuilder;

    @Autowired
    private TaskExecutor executorInterBizServers;

    @Autowired
    private MessageUtils messageUtils;


    public SimpleMessageHandler() {
        this.messagePacketType = messagePacketType();
    }

    /**
     * 用户只在其RegionCode所在的TcpServer上进行登录，
     * 处理的消息 TcpServer-->BizSever & BizServer-->BizServer
     * TcpServer-->BizServer表示非转发消息，一定是发送用户所在的IDC bizserver发送消息
     * BizServer-->BizServer表示是从发送用户IDC转到接受方用户所在IDC的bizServer
     *
     * @param channel
     * @param simpleMessagePacket
     */
    @Override
    public void handle(final Channel channel, final SimpleMessagePacket simpleMessagePacket) {

        if (logger.isDebugEnabled()) {
            logger.debug("handle msg.packet:{},channel:{}", simpleMessagePacket, channel);
        }

        // FromUser如果不是转发一定是本IDC的，这个必须确定
        // FromUser 如果不是本IDC的一定是转发过来的
        // 转发过来的不需要处理缓存的 CMSGID
        final String from = channel.getChannelInfo().getUserId();

        User fromUser = null;
        if (StringUtils.isNotBlank(from)) {
            simpleMessagePacket.setFrom(from);
            fromUser = userService.findById(from, channel.getChannelInfo().getAppID());
        }

        if (fromUser == null) {
            logger.warn("findFromUser empty:{} ", from);
            return;
        }

        ChannelUtils.wrapAppID(simpleMessagePacket.getPacketHead(), channel);

//        boolean isForward = !(StringUtils.isBlank(fromUser.getCountrycode()) ||
//                userService.isLocalUser(fromUser.getCountrycode()));
        boolean isForward = messageUtils.isForward(fromUser, simpleMessagePacket);
        if (handleResendMsg(channel, simpleMessagePacket, fromUser, isForward)) {
            return;
        }

        if (SystemConstant.MESSAGE_TYPE_RICH_MEDIA == (SystemConstant.MESSAGE_TYPE_RICH_MEDIA & simpleMessagePacket.getEntityType())) {
            simpleMessagePacket.setRichMessageInfo(messageUtils.generateRichMessage(simpleMessagePacket.getContent()));
        }

        switch (simpleMessagePacket.getMessageServiceType()) {
            case SimpleMessagePacket.TO_USER:
                handleSingleToUserMsg(channel, simpleMessagePacket, isForward, fromUser, simpleMessagePacket.getToUser());
                break;
            case SimpleMessagePacket.TO_GROUP:
                handleGroupMsg(channel, simpleMessagePacket, isForward, fromUser);
                break;
        }
    }


    private boolean handleResendMsg(final Channel channel, final SimpleMessagePacket simpleMessagePacket, final User fromUser, final boolean isForward) {
        boolean isMsgHandled = false;
        String from = fromUser.getId();
        //发送方所在IDC
        if (!isForward && handleResendMsgForLocal(channel, simpleMessagePacket, from)) {
            //重发消息已经在服务端处理，则不需要再处理该消息
            isMsgHandled = true;
        } else if (isForward && handleResendMsgForOtherRegion(channel, simpleMessagePacket, fromUser)) {
            isMsgHandled = true;
        }
        return isMsgHandled;
    }


    private void handleGroupMsg(final Channel channel, final SimpleMessagePacket simpleMessagePacket, final boolean isForward, final User fromUser) {
        Long mid = messageService.newMessageUID();
        String from = fromUser.getId();
        String fromGroup = simpleMessagePacket.getFromGroup();
//        List<User> toUserList;
//        try {
//            toUserList = groupService.findUserExcludeById(from, fromGroup);
//        } catch (ServerException e) {
//            logger.error("findGroup error", e);
//            return;
//        }

        List<User> toUserList = messageUtils.getReceiversForGroupMessage(simpleMessagePacket, from);
        if (toUserList == null || toUserList.size() == 0) {
            logger.warn("group has no or only one user:{} ", fromGroup);
            handleAfterToMsgPersist(isForward, channel, simpleMessagePacket, mid, fromUser, localIdcCountyCode);
            return;
        }
        Group group = groupService.findGroupInfo(fromGroup);
        String traceId = MDC.get("TraceID");
        String prefix = MDC.get("PREFIX");
        if (!isForward) {
            //把消息写到临时消息表中
            toUserList.stream()
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
                            resendMsgToBizServer.saveGroupMsgForResend(from, simpleMessagePacket.getCmsgid(), countryCode, fromGroup, simpleMessagePacket.getSrcMsgBytes());
                            sendMsgToOtherBizServer(proxyMessageSender, countryCode, simpleMessagePacket);
                        } finally {
                            MDC.remove("TraceID");
                            MDC.remove("PREFIX");
                        }
                    }));
        }
        toUserList.stream()
                .filter(user -> StringUtils.isBlank(user.getCountrycode())
                        || userService.isLocalUser(user.getCountrycode()))
                .forEach(to -> executorInterBizServers.execute(() -> {
                            MDC.put("TraceID", traceId);
                            MDC.put("PREFIX", prefix);
                            try {
                                Long messageId = messageService.newMessageUID();
                                logger.debug("group msg to send.fromId:{}, receiver:{}. groupId:{}", from, to.getId(), simpleMessagePacket.getToGroup());
                                //并发程序不能设置to 否则多个线程使用相同的userId
                                handleToUserIDCMsg(isForward, channel, simpleMessagePacket, fromUser, to, messageId, group);
                            } catch (Throwable e) {
                                logger.error(String.format("send message to:%s error", to), e);
                            } finally {
                                MDC.remove("TraceID");
                                MDC.remove("PREFIX");
                            }
                        })
                );

        handleAfterToMsgPersist(isForward, channel, simpleMessagePacket, mid, fromUser, localIdcCountyCode);
    }


    /**
     * 处理单发消息
     *
     * @param channel
     * @param simpleMessagePacket
     * @param isForward
     * @param fromUser
     */
    private void handleSingleToUserMsg(final Channel channel, final SimpleMessagePacket simpleMessagePacket, final boolean isForward, User fromUser, String toUserID) {
        String from = fromUser.getId();
        final Long mid = messageService.newMessageUID();
        User toUser;
        try {
            toUser = userService.findById(toUserID, channel.getChannelInfo().getAppID());
        } catch (Exception e) {
            logger.error("findToUser error", e.getCause());
            return;
        }
        if (toUser == null) {
            logger.warn("findToUser empty: {} ", toUserID);
            handleAfterToMsgPersist(isForward, channel, simpleMessagePacket, mid, fromUser, localIdcCountyCode);
            return;
        }

        if (toUserMsgCanHandleThisIDC(toUser)) {
            if (conversationService.isInBlackSheet(toUserID, simpleMessagePacket.getFrom(), channel.getChannelInfo().getAppID())) {
                if (logger.isInfoEnabled()) {
                    logger.info("[SimpleMessageHandler MESSAGE]user {} int the user {} black list",
                            simpleMessagePacket.getFrom(), toUserID);
                }
                handleAfterToMsgPersist(isForward, channel, simpleMessagePacket, mid, fromUser, toUser.getCountrycode());
            } else {
                handleToUserIDCMsg(isForward, channel, simpleMessagePacket, fromUser, toUser, mid, null);
            }
        } else {
            if (logger.isInfoEnabled()) {
                logger.info("[SimpleMessageHandler MESSAGE]transfer message to {} MQ ",
                        toUser.getCountrycode());
            }
            try {
                //把消息写入临时消息表中，写入成功，则返回状态消息，然后发送消息
//                boolean saveResult = resendMsgToBizServer.saveSingleMsgForResend(from, simpleMessagePacket.getCmsgid(), toUser.getCountrycode(), toUserID,
//                        simpleMessagePacket.getSrcMsgBytes());
                boolean saveResult = true;
                if (saveResult) {
                    //后台发送消息
                    sendMsgToOtherBizServer(proxyMessageSender, toUser.getCountrycode(), simpleMessagePacket);
                    handleAfterToMsgPersist(isForward, channel, simpleMessagePacket, mid, fromUser, toUser.getCountrycode());
                } else {
                    logger.warn("Fails to save resend msg.from:{},to:{},cmsgId:{}", from, toUser.getId(), simpleMessagePacket.getCmsgid());
                }
            } catch (Exception e) {
                logger.error("forward message error", e);
            }
        }
    }


    private void handleAfterToMsgPersist(boolean isForward, final Channel channel, final SimpleMessagePacket simpleMessagePacket, Long mid, User fromUser, final String toUserCountryCode) {
        messageService.addServerReceivedMessage(simpleMessagePacket.getCmsgid(), mid, cmgIdttl, fromUser.getId());
        MessageResponsePacket responsePacket = createMessageResponsePacket(SimpleMessagePacket.STATUS_RESPONSE_MESSAGE_TYPE,
                mid, simpleMessagePacket.getTempId(), MessageStatus.SERVER_RECEIVED, simpleMessagePacket.getCmsgid());
        if (isForward) {
            //发送方非本区，需要给bizServer回复 状态消息（消息已接受） MessageResponsePacket bizServer的channel 采用proxyMessageSender
            if (!StringUtils.isBlank(simpleMessagePacket.getCmsgid())) {
                sendMsgToOtherBizServer(proxyMessageSender, fromUser.getCountrycode(), encodeBiz2BizStatusMsg(channel.getChannelInfo(), responsePacket, channel.getTraceId(), toUserCountryCode));

            }
        } else {
            channel.write(responsePacket);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("[SimpleMessageHandler MESSAGE] write msg status-server received.messageId:{}", mid);
        }
    }


    private void sendMsgToOtherBizServer(ProxyMessageSender sender, String countryCode, BasePacket packet) {
        executorInterBizServers.execute(() -> {
            if (packet instanceof V5SimpleMessagepacket) {
                sender.writeV5Protocol(countryCode, packet.getSrcMsgBytes());
            } else {
                sender.write(countryCode, packet);
            }
        });
    }

    private void sendMsgToOtherBizServer(ProxyMessageSender sender, String countryCode, byte[] packet) {
        executorInterBizServers.execute(() -> sender.write(countryCode, packet));
    }


    private byte[] encodeBiz2BizStatusMsg(final ChannelInfo channelInfo, final BasePacket packet, final String traceId, final String toUserCountryCode) {
        PacketHead packetHead;
        if ((packetHead = packet.getPacketHead()) == null) {
            packetHead = new PacketHead();
            packet.setPacketHead(packetHead);
        }
        packetHead.setAppId((short) channelInfo.getAppID());
        packetHead.setVersion((byte) channelInfo.getClientVersion());
        byte[] packetBody = packetCodecs.encode(channelInfo.getClientVersion(), packet);

        BaseResponseMessage baseResponseMessage = BaseResponseMessage.formResponseMessage(channelInfo, traceId);
        BizOutputMessage bizOutputMessage = messageBuilder.buildTcp2bizReqMessage(channelInfo.getNodeId(),
                baseResponseMessage, packetBody, toUserCountryCode.getBytes(Charset.forName("UTF-8")));
        return bizOutputMessage.getMessageBody();
    }


    private boolean toUserMsgCanHandleThisIDC(User toUser) {
        return userService.isSystemAccount(toUser) || userService.isLocalUser(toUser.getCountrycode());
    }

    /**
     * 重发处理TcpServer向BizServer发送的消息
     *
     * @param channel
     * @param simpleMessagePacket
     * @param formUserId
     * @return true:消息在服务端已经处理 false:消息在服务端没有处理
     */
    private boolean handleResendMsgForLocal(final Channel channel, final SimpleMessagePacket simpleMessagePacket, final String formUserId) {
        boolean result = false;
        Long cachedMessageId = messageService.isServerReceived(simpleMessagePacket.getCmsgid(),
                simpleMessagePacket.getMsgFlag(), formUserId);
        if (cachedMessageId != null) {

            if (logger.isInfoEnabled()) {
                logger.info("[SimpleMessageHandler MESSAGE]message {}-{} is already received",
                        cachedMessageId, simpleMessagePacket.getCmsgid());
            }
            channel.write(createMessageResponsePacket(SimpleMessagePacket.STATUS_RESPONSE_MESSAGE_TYPE,
                    cachedMessageId, simpleMessagePacket.getTempId(),
                    MessageStatus.SERVER_RECEIVED, simpleMessagePacket.getCmsgid()));
            result = true;
        }
        return result;
    }


    /**
     * 重发处理BizServer向BizServer发送的消息
     *
     * @param channel
     * @param simpleMessagePacket
     * @param fromUser
     * @return true:消息在服务端已经处理 false:消息在服务端没有处理
     */
    private boolean handleResendMsgForOtherRegion(final Channel channel, final SimpleMessagePacket simpleMessagePacket, final User fromUser) {
        boolean result = false;

        //跨区发送消息时，重发模块过来的消息和发送者所在的bizServer过来的消息，无法保证消息次序，都需要进行重发判断
        Long cachedMessageId = messageService.isServerReceived(simpleMessagePacket.getCmsgid(),
                SystemConstant.MSGFLAG_RESENT, fromUser.getId());
        if (cachedMessageId != null) {
            if (logger.isInfoEnabled()) {
                logger.info("[SimpleMessageHandler MESSAGE]message {}-{} is already received",
                        cachedMessageId, simpleMessagePacket.getCmsgid());
            }
            //获取接受方用户的国家码
            String toUserCountryCode = idcCountryCodes;
            switch (simpleMessagePacket.getMessageServiceType()) {
                case SimpleMessagePacket.TO_USER:
                    String toUserID = simpleMessagePacket.getToUser();
                    try {
                        User toUser = userService.findById(toUserID, channel.getChannelInfo().getAppID());
                        toUserCountryCode = toUser.getCountrycode();
                    } catch (Exception e) {
                        logger.error("findToUser error", e.getCause());
                    }
                    break;
            }

            //回复发送方IDC，发送方消息已经处理
            MessageResponsePacket responsePacket = createMessageResponsePacket(SimpleMessagePacket.STATUS_RESPONSE_MESSAGE_TYPE,
                    cachedMessageId, simpleMessagePacket.getTempId(),
                    MessageStatus.SERVER_RECEIVED, simpleMessagePacket.getCmsgid());

            sendMsgToOtherBizServer(proxyMessageSender, fromUser.getCountrycode(), encodeBiz2BizStatusMsg(channel.getChannelInfo(), responsePacket, channel.getTraceId(), toUserCountryCode));

            result = true;
        }
        return result;
    }


    protected void handleToUserIDCMsg(boolean isForward, Channel channel, SimpleMessagePacket simpleMessagePacketPar,
                                      User fromUser, User toUser, Long mid, Group group) throws ServerException {
        String toUserID = toUser.getId();
        String traceId = channel.getTraceId();

        //防止并发修改
        SimpleMessagePacket simpleMessagePacket = simpleMessagePacketPar;


        int appID = (simpleMessagePacket.getPacketHead() == null || simpleMessagePacket.getPacketHead().getAppId() == 0) ? fromUser.getAppId() : simpleMessagePacket.getPacketHead().getAppId();

        if (simpleMessagePacketPar.getPacketType() == VideoMessagePacket.VIDEO_MESSAGE_PACKET_TYPE && appID == 0) {

            UserBehaviouAttr uba = userService.getUserBehaviouAttr(toUser.getId(), toUser.getAppId());

            boolean newVedioSupport = uba != null ? VersionUtils.isSupportVersion(uba) : false;

            if (!newVedioSupport) {
                simpleMessagePacket = new TextMessagePacket();
                BeanUtils.copyProperties(simpleMessagePacketPar, simpleMessagePacket);
                //更改属性值
                simpleMessagePacket.setPacketHead(simpleMessagePacketPar.getPacketHead());
                simpleMessagePacket.setPacketType(TextMessagePacket.TEXT_MESSAGE_PACKET_TYPE);
                simpleMessagePacket.setMessageType((byte) MessageType.TEXT.getValue());
                String c = messageUtils.getVideoMessageTipForUnsupportedVersion((VideoMessagePacket) simpleMessagePacketPar, fromUser, toUser, simpleMessagePacket.getPacketHead().getAppId());
                simpleMessagePacket.setContent(c.getBytes(Charset.forName("UTF-8")));
            }
        }

        //Channel toChannel = channelService.findChannel(toUser);

        Map<Platform, List<Channel>> toChannelMap = channelService.findChannelMap(toUser);


        Message message = createMessage(simpleMessagePacket, toUserID);
        message.setId(mid);
        message.setIsCount(0);
        Map<String, Object> metaMap = new HashMap<>();
        metaMap.put("cmsgid", simpleMessagePacket.getCmsgid());
        metaMap.put("entity_format", messageService.getMessageEntityType(simpleMessagePacket));
        message.setMeta(metaMap);

        message = messageService.createMessage(simpleMessagePacket.getFrom(),
                toUserID, message, ChannelUtils.isOffline(toChannelMap) ? com.handwin.message.bean.MessageStatus.UNDEAL : com.handwin.message.bean.MessageStatus.ONLINE, simpleMessagePacket.getContent());

        //非群组消息 发送确认消息
        if (null == simpleMessagePacket.getFromGroup()) {
            handleAfterToMsgPersist(isForward, channel, simpleMessagePacket, mid, fromUser, toUser.getCountrycode());
        }

        if (MapUtils.isNotEmpty(toChannelMap)) {

            for (Map.Entry<Platform, List<Channel>> entry : toChannelMap.entrySet()) {
                Platform platform = entry.getKey();
                List<Channel> channelList = entry.getValue();
                if (CollectionUtils.isNotEmpty(channelList)) {

                    for (Channel toChannel : channelList) {

                        ChannelInfo toChannelInfo = toChannel == null ? null : toChannel.getChannelInfo();

                        if (toChannelInfo != null && (platform != Platform.Mobile || (platform == Platform.Mobile && toChannelInfo.getChannelMode() != ChannelMode.SUSPEND))) {
                            SimpleMessagePacket messagePacket = createSimpleMessagePacket(simpleMessagePacket, message.getId(),
                                    fromUser.getId(), toUserID);

                            try {
                                byte[] trackBytes = SimpleWrongMessage.encode(toChannelInfo.getAppID(),
                                        channel.getChannelInfo().getUserId(), toChannelInfo.getUserId(), message.getId(), traceId);
                                if (logger.isDebugEnabled()) {
                                    logger.debug("userID:{} , backTrack.size:{} ", toUser.getId(), trackBytes != null ? trackBytes.length : 0);
                                }
                                messagePacket.setTraceId(traceId);

                                //send.incrementAndGet();

                                toChannel.write(messagePacket, trackBytes, ChannelAction.SEND);
                            } catch (IOException e) {
                                logger.error("encode track info error", e);
                                toChannel.write(messagePacket, ChannelAction.SEND);
                            }

                            if (logger.isInfoEnabled()) {
                                logger.info("[SimpleMessageHandler MESSAGE] send msg to receiver:{}",
                                        simpleMessagePacket.getToUser());
                            }
                        }


                    }
                }
            }
        }


        if (ChannelUtils.isOffline(toChannelMap)) {
            //群组消息
            if (null != simpleMessagePacket.getFromGroup()) {
                //免打扰的判断
                boolean canSendPush = false;
                //处于免打扰
                if (conversationService.isNoDisturbForGroup(toUser.getId(), simpleMessagePacket.getFromGroup(), simpleMessagePacket.getPacketHead().getAppId())) {
                    //如果是@消息则可以发送push消息
                    //内容为富媒体格式
                    if (SystemConstant.MESSAGE_TYPE_RICH_MEDIA == (SystemConstant.MESSAGE_TYPE_RICH_MEDIA & simpleMessagePacket.getEntityType())) {
                        RichMessageInfo richMessageInfo = simpleMessagePacket.getRichMessageInfo();
                        if (null != richMessageInfo) {
                            String atUsers = richMessageInfo.getAtUsers();
                            //@该人 免打扰不起作用
                            if (StringUtils.isNotBlank(atUsers) && atUsers.contains(toUser.getId())) {
                                canSendPush = true;
                            }
                        }
                        logger.debug("[group message] push. groupId:{} ,toUserId:{}, canSendPush:{} rich media.", simpleMessagePacket.getFromGroup(), toUser.getId(), canSendPush);
                    }
                } else {
                    canSendPush = true;
                }

                if (canSendPush) {
                    handleOfflineMessage(fromUser, simpleMessagePacket, toUser, traceId, group);
                } else {
                    logger.debug("[group message] push. groupId:{} ,toUserId:{} no disturb. no push.", simpleMessagePacket.getFromGroup(), toUser.getId());
                }
            }//单发消息
            else if (!conversationService.isInGreySheet(toUserID, simpleMessagePacket.getFrom(), channel.getChannelInfo().getAppID())) {
                handleOfflineMessage(fromUser, simpleMessagePacket, toUser, traceId, null);
            } else {
                if (logger.isInfoEnabled()) {
                    logger.info("[SimpleMessageHandler MESSAGE] from:{} is in grey list.receiver:{}",
                            fromUser.getId(), simpleMessagePacket.getToUser());
                }
            }
        }
    }


    private void handleOfflineMessage(User fromUser, SimpleMessagePacket packet, User toUser, String traceId, Group group) {

        UserToken userToken = userService.getTokenInfo(toUser.getId(), toUser.getAppId());
        if (userToken == null || userToken.getDeviceType() > 2) {
            if (logger.isInfoEnabled()) {
                logger.info("user {} have no push token or un supported device type", toUser.getId());

            }
            return;
        }
        //获得用户昵称，优先获取手机通讯录的名字
        String nickName = "";
        try {
            nickName = userService.getFriendNickname(fromUser, toUser.getId(), toUser.getAppId());
        } catch (ServerException e) {
            logger.error(String.format("handleOfflineMessage fromUser:%s, toUser:%s",
                    fromUser, toUser), e);
        }

        if (null != group) {
            nickName = String.format("%s(%s)", nickName, group.getName() != null ? group.getName() : "");
        }

        messageService.pushText(fromUser, toUser, userToken, nickName, packet, traceId);
    }


    public MessageResponsePacket createMessageResponsePacket(byte messageType, long messageId, int tempId,
                                                             MessageStatus messageStatus, String cmsgId) {
        MessageResponsePacket messageResponsePacket = new MessageResponsePacket();
        messageResponsePacket.setMessageType(messageType);
        messageResponsePacket.setMessageStatus(messageStatus);
        messageResponsePacket.setMessageId(messageId);
        messageResponsePacket.setTempId(tempId);
        messageResponsePacket.setCmsgid(cmsgId);
        return messageResponsePacket;
    }


    private SimpleMessagePacket createSimpleMessagePacket(SimpleMessagePacket packet, long messageId, String from, String toUserId) {
        SimpleMessagePacket simpleMessagePacket = new SimpleMessagePacket();
        simpleMessagePacket.setPacketType(packet.getPacketType());
        simpleMessagePacket.setMessageType(packet.getMessageType());
        simpleMessagePacket.setMessageServiceType(packet.getMessageServiceType());
        simpleMessagePacket.setEntityType(packet.getEntityType());
        simpleMessagePacket.setFrom(from);
        simpleMessagePacket.setToUser(toUserId);
        simpleMessagePacket.setFromGroup(packet.getFromGroup());
        simpleMessagePacket.setToGroup(packet.getToGroup());
        simpleMessagePacket.setTempId(packet.getTempId());
        simpleMessagePacket.setMessageId(messageId);
        simpleMessagePacket.setContent(packet.getContent());

        simpleMessagePacket.setCmsgid(packet.getCmsgid());
        PacketHead head = new PacketHead();
        head.setTempId(packet.getTempId());
        head.setSecret(packet.getPacketHead().getSecret());
        simpleMessagePacket.setPacketHead(head);
        return simpleMessagePacket;
    }

    @VisibleForTesting
    public Message createMessage(SimpleMessagePacket simpleMessagePacket, String toUserId) {
        Message message = new Message();
        message.setSender(simpleMessagePacket.getFrom());
        message.setReceiver(toUserId);
        message.setCreateTime(System.currentTimeMillis());
        message.setConversationId(simpleMessagePacket.getFrom());
        if (StringUtils.isNotBlank(simpleMessagePacket.getFromGroup())) {
            message.setReceiverType(SystemConstant.TO_SEND_MESSAGE_RECEIVE_TYPE_GROUP);
            message.setConversationId(simpleMessagePacket.getFromGroup());
        } else {
            message.setReceiverType(SystemConstant.TO_SEND_MESSAGE_RECEIVE_TYPE_PERSON);
        }
        message.setType(MessageType.findByValue(simpleMessagePacket.getMessageType()).name());
        message.setSecret((simpleMessagePacket.getPacketHead().getSecret() == SystemConstant.PACKET_SECRET) ? 1 : 0);
        return message;
    }


    protected abstract int messagePacketType();


}
