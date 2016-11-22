package com.handwin.server.handler;

import com.google.common.annotations.VisibleForTesting;
import com.handwin.codec.PacketCodecs;
import com.handwin.entity.BizOutputMessage;
import com.handwin.entity.ChannelInfo;
import com.handwin.entity.User;
import com.handwin.entity.UserToken;
import com.handwin.entity.wrong.SimpleWrongMessage;
import com.handwin.exception.ServerException;
import com.handwin.localentity.Message;
import com.handwin.localentity.MessageType;
import com.handwin.packet.*;
import com.handwin.rabbitmq.MessageBuilder;
import com.handwin.server.Channel;
import com.handwin.server.ProxyMessageSender;
import com.handwin.server.proto.BaseResponseMessage;
import com.handwin.server.proto.ChannelAction;
import com.handwin.service.*;
import com.handwin.utils.ChannelUtils;
import com.handwin.utils.SystemConstant;
import com.handwin.utils.V5ProtoConstant;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Created by piguangtao on 15/7/16.
 */
@Service
public class CommonMessageHandler {
    private static Logger logger = LoggerFactory.getLogger(CommonMessageHandler.class);
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
    protected UserService userService;

    @Autowired
    protected ProxyMessageSender proxyMessageSender;

    @VisibleForTesting
    @Value("${user.sent.ms.cmsgid.ttl}")
    public int cmgIdttl;

    public void handleMsg(final Channel channel, CommonMsgPackage commonMsgPackage) {
        if (logger.isDebugEnabled()) {
            logger.debug("handle msg.packet:{},channel:{}", commonMsgPackage, channel);
        }

        // FromUser如果不是转发一定是本IDC的，这个必须确定
        // FromUser 如果不是本IDC的一定是转发过来的
        // 转发过来的不需要处理缓存的 CMSGID
        final String from = channel.getChannelInfo().getUserId();
        commonMsgPackage.setFrom(from);
        final User fromUser = userService.findById(from, channel.getChannelInfo().getAppID());
        if (fromUser == null) {
            logger.warn("findFromUser empty:{} ", from);
            return;
        }

        boolean isForward = !(StringUtils.isBlank(fromUser.getCountrycode()) ||
                userService.isLocalUser(fromUser.getCountrycode()));

        logger.debug("countryCode:{},isForward:{}", fromUser.getCountrycode(), isForward);

        if (handleResendMsg(channel, commonMsgPackage, fromUser, isForward)) {
            return;
        }

        switch (commonMsgPackage.getMessageServiceType()) {
            case SimpleMessagePacket.TO_USER:
                handleSingleToUserMsg(channel, commonMsgPackage, isForward, fromUser);
                break;
            case SimpleMessagePacket.TO_GROUP:
                handleGroupMsg(channel, commonMsgPackage, isForward, fromUser);
                break;
        }
    }


    private boolean handleResendMsg(final Channel channel, final CommonMsgPackage commonMsgPackage, final User fromUser, final boolean isForward) {
        boolean isMsgHandled = false;
        String from = fromUser.getId();
        //发送方所在IDC
        if (!isForward && handleResendMsgForLocal(channel, commonMsgPackage, from)) {
            //重发消息已经在服务端处理，则不需要再处理该消息
            isMsgHandled = true;
        } else if (isForward && handleResendMsgForOtherRegion(channel, commonMsgPackage, fromUser)) {
            isMsgHandled = true;
        }
        return isMsgHandled;
    }


    private void handleGroupMsg(final Channel channel, final CommonMsgPackage commonMsgPackage, final boolean isForward, final User fromUser) {
        final Long mid = messageService.newMessageUID();
        String from = fromUser.getId();
        String toGroup = commonMsgPackage.getToGroup();
        List<User> toUserList;
        try {
            toUserList = groupService.findUserExcludeById(from, toGroup, fromUser.getAppId());
        } catch (ServerException e) {
            logger.error("findGroup error", e);
            return;
        }
        if (toUserList == null || toUserList.size() == 0) {
            logger.warn("group has no or only one user:{} ", toGroup);
            handleAfterToMsgPersist(isForward, channel, commonMsgPackage, mid, fromUser, localIdcCountyCode);
            return;
        }
        if (!isForward) {
            //把消息写到临时消息表中
            toUserList.stream()
                    .map(User::getCountrycode)
                    .filter(countryCode -> StringUtils.isNotBlank(countryCode)
                            && !userService.isLocalUser(countryCode))
                    .map(userCountryCode -> idcCountryCodes.contains(userCountryCode) ? userCountryCode : defaultCountryCode)
                    .distinct()
                    .forEach(countryCode -> {
                        //把消息写入临时消息表中
                        resendMsgToBizServer.saveGroupMsgForResend(from, commonMsgPackage.getCmsgid(), countryCode, toGroup, commonMsgPackage.getSrcMsgBytes());
                        sendMsgToOtherBizServer(proxyMessageSender, countryCode, commonMsgPackage.getSrcMsgBytes());
                    });
        }
        toUserList.stream()
                .filter(user -> StringUtils.isBlank(user.getCountrycode())
                        || userService.isLocalUser(user.getCountrycode()))
                .forEach(to -> {
                            try {
                                logger.debug("group msg to send. receiver:{}", to.getId());
                                commonMsgPackage.setToUser(to.getId());
                                handleToUserIDCMsg(isForward, channel, commonMsgPackage, fromUser, to, mid, false);
                            } catch (Throwable e) {
                                logger.error(String.format("send message to:%s error", to), e);
                            }
                        }
                );
        handleAfterToMsgPersist(isForward, channel, commonMsgPackage, mid, fromUser, localIdcCountyCode);
    }


    /**
     * 处理单发消息
     *
     * @param channel
     * @param commonMsgPackage
     * @param isForward
     * @param fromUser
     */
    private void handleSingleToUserMsg(final Channel channel, final CommonMsgPackage commonMsgPackage, final boolean isForward, User fromUser) {
        String toUserID = commonMsgPackage.getToUser();
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
            handleAfterToMsgPersist(isForward, channel, commonMsgPackage, mid, fromUser, localIdcCountyCode);
            return;
        }

        if (toUserMsgCanHandleThisIDC(toUser)) {
            handleToUserIDCMsg(isForward, channel, commonMsgPackage, fromUser, toUser, mid, true);
        } else {
            if (logger.isInfoEnabled()) {
                logger.info("[SimpleMessageHandler MESSAGE]transfer message to {} MQ ",
                        toUser.getCountrycode());
            }
            try {
                //把消息写入临时消息表中，写入成功，则返回状态消息，然后发送消息
                boolean saveResult = resendMsgToBizServer.saveSingleMsgForResend(from, commonMsgPackage.getCmsgid(), toUser.getCountrycode(), toUserID,
                        commonMsgPackage.getSrcMsgBytes());
                if (saveResult) {
                    //后台发送消息
                    sendMsgToOtherBizServer(proxyMessageSender, toUser.getCountrycode(), commonMsgPackage.getSrcMsgBytes());
                    handleAfterToMsgPersist(isForward, channel, commonMsgPackage, mid, fromUser, toUser.getCountrycode());
                } else {
                    logger.warn("Fails to save resend msg.from:{},to:{},cmsgId:{}", from, toUser.getId(), commonMsgPackage.getCmsgid());
                }
            } catch (Exception e) {
                logger.error("forward message error", e);
            }
        }
    }


    private void handleAfterToMsgPersist(boolean isForward, final Channel channel, final CommonMsgPackage commonMsgPackage, Long mid, User fromUser, final String toUserCountryCode) {
        messageService.addServerReceivedMessage(commonMsgPackage.getCmsgid(), mid, cmgIdttl, fromUser.getId());
        if (isForward) {
            //发送方非本区，需要给bizServer回复 状态消息（消息已接受） MessageResponsePacket bizServer的channel 采用proxyMessageSender
            if (!StringUtils.isBlank(commonMsgPackage.getCmsgid())) {
                MessageResponsePacket responsePacket = createMessageResponsePacket(SimpleMessagePacket.STATUS_RESPONSE_MESSAGE_TYPE,
                        mid, commonMsgPackage.getTempId(), MessageStatus.SERVER_RECEIVED, commonMsgPackage.getCmsgid());
                sendMsgToOtherBizServer(proxyMessageSender, fromUser.getCountrycode(), encodeBiz2BizStatusMsg(channel.getChannelInfo(), responsePacket, channel.getTraceId(), toUserCountryCode));

            }
        } else {
            if (commonMsgPackage.replyServerReceived()) {
                BasePacket responsePacket = createMessageResponsePacket(commonMsgPackage, mid);
                channel.write(responsePacket);
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("[SimpleMessageHandler MESSAGE] write msg status-server received.messageId:{}", mid);
        }
    }


//    private void sendMsgToOtherBizServer(ProxyMessageSender sender, String countryCode, BasePacket packet) {
//        executorInterBizServers.execute(() -> sender.write(countryCode, packet));
//    }

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
     * @param commonMsgPackage
     * @param formUserId
     * @return true:消息在服务端已经处理 false:消息在服务端没有处理
     */
    private boolean handleResendMsgForLocal(final Channel channel, final CommonMsgPackage commonMsgPackage, final String formUserId) {
        boolean result = false;
        Long cachedMessageId = messageService.isServerReceived(commonMsgPackage.getCmsgid(),
                SystemConstant.MSGFLAG_RESENT, formUserId);
        if (cachedMessageId != null) {

            if (logger.isInfoEnabled()) {
                logger.info("[SimpleMessageHandler MESSAGE]message {}-{} is already received",
                        cachedMessageId, commonMsgPackage.getCmsgid());
            }
//            channel.write(createMessageResponsePacket(SimpleMessagePacket.STATUS_RESPONSE_MESSAGE_TYPE,
//                    cachedMessageId, commonMsgPackage.getTempId(),
//                    MessageStatus.SERVER_RECEIVED, commonMsgPackage.getCmsgid()));
            if (commonMsgPackage.replyServerReceived()) {
                channel.write(createMessageResponsePacket(commonMsgPackage, cachedMessageId));
            }
            result = true;
        }
        return result;
    }


    /**
     * 重发处理BizServer向BizServer发送的消息
     *
     * @param channel
     * @param commonMsgPackage
     * @param fromUser
     * @return true:消息在服务端已经处理 false:消息在服务端没有处理
     */
    private boolean handleResendMsgForOtherRegion(final Channel channel, final CommonMsgPackage commonMsgPackage, final User fromUser) {
        boolean result = false;

        //跨区发送消息时，重发模块过来的消息和发送者所在的bizServer过来的消息，无法保证消息次序，都需要进行重发判断
        Long cachedMessageId = messageService.isServerReceived(commonMsgPackage.getCmsgid(),
                SystemConstant.MSGFLAG_RESENT, fromUser.getId());
        if (cachedMessageId != null) {
            if (logger.isInfoEnabled()) {
                logger.info("[SimpleMessageHandler MESSAGE]message {}-{} is already received",
                        cachedMessageId, commonMsgPackage.getCmsgid());
            }
            //获取接受方用户的国家码
            String toUserCountryCode = idcCountryCodes;
            switch (commonMsgPackage.getMessageServiceType()) {
                case SimpleMessagePacket.TO_USER:
                    String toUserID = commonMsgPackage.getToUser();
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
                    cachedMessageId, commonMsgPackage.getTempId(),
                    MessageStatus.SERVER_RECEIVED, commonMsgPackage.getCmsgid());

            sendMsgToOtherBizServer(proxyMessageSender, fromUser.getCountrycode(), encodeBiz2BizStatusMsg(channel.getChannelInfo(), responsePacket, channel.getTraceId(), toUserCountryCode));

            result = true;
        }
        return result;
    }


    protected void handleToUserIDCMsg(boolean isForward, Channel channel, CommonMsgPackage commonMsgPackage,
                                      User fromUser, User toUser, Long mid, boolean isResponseStatusMsg) throws ServerException {
        String toUserID = commonMsgPackage.getToUser();
        String traceId = channel.getTraceId();

        if (conversationService.isInBlackSheet(toUserID, commonMsgPackage.getFrom(), fromUser.getAppId())) {
            if (logger.isInfoEnabled()) {
                logger.info("[SimpleMessageHandler MESSAGE]user {} int the user {} black list",
                        commonMsgPackage.getFrom(), toUserID);
            }
            if (isResponseStatusMsg) {
                handleAfterToMsgPersist(isForward, channel, commonMsgPackage, mid, fromUser, toUser.getCountrycode());
            }
            return;
        }
        Channel toChannel = channelService.findChannel(toUser);

        Message message = createMessage(commonMsgPackage);
        message.setId(mid);
        message.setIsCount(0);
        if (commonMsgPackage.needStore()) {
            message = messageService.createMessage(commonMsgPackage.getFrom(),
                    commonMsgPackage.getToUser(), message, ChannelUtils.isOffline(toChannel) ? com.handwin.message.bean.MessageStatus.UNDEAL : com.handwin.message.bean.MessageStatus.ONLINE, commonMsgPackage.getContent());
        }

        if (isResponseStatusMsg) {
            handleAfterToMsgPersist(isForward, channel, commonMsgPackage, mid, fromUser, toUser.getCountrycode());
        }


        ChannelInfo toChannelInfo = toChannel == null ? null : toChannel.getChannelInfo();

        if (toChannelInfo != null && toChannelInfo.getChannelMode() != ChannelMode.SUSPEND) {
            BasePacket messagePacket = createSimpleMessagePacket(commonMsgPackage, message.getId(),
                    fromUser.getId());

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
                        commonMsgPackage.getToUser());
            }
        } else {
            if (!commonMsgPackage.isPush()) {
                logger.debug("no need to push");
                return;
            }
            if (!conversationService.isInGreySheet(toUserID, commonMsgPackage.getFrom(), fromUser.getAppId())) {
                handleOfflineMessage(fromUser, commonMsgPackage, toUser, traceId);
                if (logger.isInfoEnabled()) {
                    logger.info("[SimpleMessageHandler MESSAGE] receiver:{}. send push msg. toChannelInfo:{}",
                            commonMsgPackage.getToUser(), toChannelInfo);
                }
            } else {
                if (logger.isInfoEnabled()) {
                    logger.info("[SimpleMessageHandler MESSAGE] from:{} is in grey list.receiver:{}",
                            fromUser.getId(), commonMsgPackage.getToUser());
                }
            }
        }
    }


    private void handleOfflineMessage(User fromUser, CommonMsgPackage packet, User toUser, String traceId) {

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
        messageService.pushText(fromUser, toUser, userToken, nickName, packet, traceId);
    }


    public BasePacket createMessageResponsePacket(CommonMsgPackage commonMsgPackage, long mid) {
        BasePacket basePacket = null;
        switch (commonMsgPackage.getType()) {
            case 1:
                basePacket = createMessageResponsePacket(SimpleMessagePacket.STATUS_RESPONSE_MESSAGE_TYPE,
                        mid, commonMsgPackage.getTempId(), MessageStatus.SERVER_RECEIVED, commonMsgPackage.getCmsgid());
                break;
            case 2:
                PacketHead packetHead = commonMsgPackage.getGenericPacket().getPacketHead().copy();
                packetHead.setContentType(V5ProtoConstant.CONTENT_TYPE_BYTES);
                packetHead.setService(V5ProtoConstant.SERVCIE_MSG_STATUS);
                packetHead.setServerMessageID(String.valueOf(mid));
                GenericPacket genericPacket = new GenericPacket();
                genericPacket.setPacketHead(packetHead);
                genericPacket.setBodyType(GenericPacket.BODY_TYPE_BYTES);
                //服务端已经接受
                genericPacket.setBodySrcBytes(new byte[]{(byte) MessageStatus.SERVER_RECEIVED.id()});
                basePacket = genericPacket;
                break;
            default:
                break;
        }
        return basePacket;
    }

    protected MessageResponsePacket createMessageResponsePacket(byte messageType, long messageId, int tempId,
                                                                MessageStatus messageStatus, String cmsgId) {
        MessageResponsePacket messageResponsePacket = new MessageResponsePacket();
        messageResponsePacket.setMessageType(messageType);
        messageResponsePacket.setMessageStatus(messageStatus);
        messageResponsePacket.setMessageId(messageId);
        messageResponsePacket.setTempId(tempId);
        messageResponsePacket.setCmsgid(cmsgId);
        return messageResponsePacket;
    }


    private BasePacket createSimpleMessagePacket(CommonMsgPackage commonMsgPackage, long messageId, String from) {
        BasePacket result = null;
        switch (commonMsgPackage.getType()) {
            case 1:
                SimpleMessagePacket simpleMessagePacket = new SimpleMessagePacket();
                simpleMessagePacket.setPacketType(commonMsgPackage.messagePacketType());
                simpleMessagePacket.setMessageType(commonMsgPackage.getMessageType());
                simpleMessagePacket.setMessageServiceType(commonMsgPackage.getMessageServiceType());
                simpleMessagePacket.setFrom(from);
                simpleMessagePacket.setToUser(commonMsgPackage.getToUser());
                simpleMessagePacket.setFromGroup(commonMsgPackage.getFromGroup());
                simpleMessagePacket.setToGroup(commonMsgPackage.getToGroup());
                simpleMessagePacket.setTempId(commonMsgPackage.getTempId());
                simpleMessagePacket.setMessageId(messageId);
                simpleMessagePacket.setContent(commonMsgPackage.getContent());

                simpleMessagePacket.setCmsgid(commonMsgPackage.getCmsgid());
                PacketHead head = new PacketHead();
                head.setTempId(commonMsgPackage.getTempId());
                head.setSecret(commonMsgPackage.getSecret());
                simpleMessagePacket.setPacketHead(head);
                result = simpleMessagePacket;
                break;
            case 2:
                commonMsgPackage.getGenericPacket().getPacketHead().setFrom(from);
                commonMsgPackage.getGenericPacket().getPacketHead().setServerMessageID(String.valueOf(messageId));
                result = commonMsgPackage.copyGenericPacket();

                break;
        }
        return result;
    }

    @VisibleForTesting
    public Message createMessage(CommonMsgPackage commonMsgPackage) {
        Message message = new Message();
        message.setSender(commonMsgPackage.getFrom());
        message.setReceiver(commonMsgPackage.getToUser());
        message.setCreateTime(System.currentTimeMillis());
        message.setConversationId(commonMsgPackage.getFrom());
        if (StringUtils.isNotBlank(commonMsgPackage.getFromGroup())) {
            message.setReceiverType(SystemConstant.TO_SEND_MESSAGE_RECEIVE_TYPE_GROUP);
            message.setConversationId(commonMsgPackage.getFromGroup());
        } else {
            message.setReceiverType(SystemConstant.TO_SEND_MESSAGE_RECEIVE_TYPE_PERSON);
        }
        message.setType(MessageType.findByValue(commonMsgPackage.getMessageType()).name());
        message.setSecret((commonMsgPackage.getSecret() == SystemConstant.PACKET_SECRET) ? 1 : 0);
        return message;
    }
}
