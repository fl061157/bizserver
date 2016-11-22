package com.handwin.server.controller;


import com.handwin.codec.PacketCodecs;
import com.handwin.entity.*;
import com.handwin.entity.wrong.SimpleWrongMessage;
import com.handwin.exception.ServerException;
import com.handwin.localentity.Message;
import com.handwin.localentity.MessageType;
import com.handwin.packet.*;
import com.handwin.protocal.v5.codec.V5GenericPacket;
import com.handwin.protocal.v5.codec.V5PacketHead;
import com.handwin.rabbitmq.MessageBuilder;
import com.handwin.server.Channel;
import com.handwin.server.ProxyMessageSender;
import com.handwin.server.proto.BaseResponseMessage;
import com.handwin.server.proto.ChannelAction;
import com.handwin.service.*;
import com.handwin.utils.ChannelUtils;
import com.handwin.utils.Snowflake;
import com.handwin.utils.SystemConstant;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;

/**
 * Created by Danny on 2014-12-06.
 */
@Service("defaultServiceController")
@Controller(disable = true)
public class DefaultServiceController implements ServiceController {

    private static final Logger logger = LoggerFactory.getLogger(DefaultServiceController.class);

    @Autowired
    private UserService userService;

    @Value("${localidc.country.code}")
    private String localCountryCode;

    @Value("${user.sent.ms.cmsgid.ttl}")
    public int cmgIdttl;

    @Autowired
    private ProxyMessageSender proxyMessageSender;

    @Autowired
    private Snowflake idGenerator;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private ConversationService conversationService;

    @Autowired
    private PacketCodecs packetCodecs;

    @Autowired
    private TaskExecutor executorInterBizServers;

    @Autowired
    private MessageBuilder messageBuilder;

    @Autowired
    private IResendMsgToBizServer resendMsgToBizServer;

    @Autowired
    private MessageService messageService;


    @Override
    public void handle(Channel channel, V5PacketHead packetHead, V5GenericPacket genericPacket) {

        if (logger.isDebugEnabled()) {
            logger.debug("Receive PacketHead:{} , GenericPacket:{}", packetHead, genericPacket);
        }
        boolean serverReceiveConfirm = packetHead.getServerReceivedConfirm();
        boolean isForward = !(StringUtils.isBlank(packetHead.getFromRegion()) ||
                userService.isLocalUser(packetHead.getFromRegion()));

        String toUserID = packetHead.getTo();
        final User toUser;
        if (StringUtils.isBlank(toUserID) || (toUser = userService.findById(toUserID,channel.getChannelInfo().getAppID())) == null) {
            logger.error("Not find user:{}", toUserID);
            if (!isForward && serverReceiveConfirm) {
                if (logger.isInfoEnabled()) {
                    logger.info("Write confirm back to: {} , packetType:{} ", packetHead.getFrom(), packetHead.getPacketType());
                }
                ack(channel, packetHead, MessageStatus.ERROR, null, null, false);
            }
            return;
        }
        if (StringUtils.isBlank(toUser.getCountrycode())) {
            logger.info("RegionCode Is Empty user.id :{} ", toUser.getId());
            if (!userService.isSystemAccount(toUser)) {
                return;
            }
            toUser.setCountrycode(localCountryCode);
        }

        if (StringUtils.isBlank(packetHead.getToRegion())) {
            packetHead.setToRegion(toUser.getCountrycode());
        }

        boolean isToUserIDC = userService.isLocalUser(toUser.getCountrycode());


        String clientMessageID = packetHead.getMessageID();

        if (logger.isDebugEnabled()) {
            logger.debug("isToUserIDC:{} , isForward:{} , toUserID: {} , clientMessageID:{} ",
                    isToUserIDC, isForward, toUserID, clientMessageID);
        }

        final Long mid = messageService.newMessageUID();


        if (serverReceiveConfirm && StringUtils.isNotBlank(clientMessageID)) {
            Long messageID = messageService.isServerReceived(clientMessageID,
                    packetHead.getResend() ? SystemConstant.MSGFLAG_RESENT : 0, packetHead.getFrom());
            if (messageID != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("serverReceiveConfirm clientMessageID :{} ", clientMessageID);
                }
                ack(channel, packetHead, MessageStatus.SERVER_RECEIVED, toUser, String.valueOf(mid), isForward);
                return;
            }
        }

        if (isToUserIDC) {
            handleToUserIDC(channel, packetHead, genericPacket, packetHead.getFrom(), toUser, mid, isForward, serverReceiveConfirm);
        } else if (!isForward) {
            handleProxy(channel, packetHead, genericPacket, packetHead.getFrom(), toUser, mid, serverReceiveConfirm);
        }

        if (!isForward && packetHead.getBidirection()) {
            channel.write(responseGenericPacket(packetHead, genericPacket));
        }

    }


    private void handleToUserIDC(Channel channel, V5PacketHead packetHead, V5GenericPacket genericPacket, String fromUserID,
                                 User toUser, Long mid, boolean isForward, boolean serverReceiveConfirm) {
        if (logger.isDebugEnabled()) {
            logger.debug("handleToUserIDC channel:{} , genericPacket:{} , fromUserID:{} , toUserID:{} ",
                    channel, genericPacket, fromUserID, toUser.getId());
        }

        Channel toChannel = null;
        try {
            toChannel = channelService.findChannel(toUser);
        } catch (ServerException e) {
            logger.error(String.format("write systemMessage to user.id:%s error", toUser.getId()), e);
        }

        try {
            createMessage(packetHead, genericPacket, mid, ChannelUtils.isOffline(toChannel) ?
                    com.handwin.message.bean.MessageStatus.UNDEAL : com.handwin.message.bean.MessageStatus.ONLINE);
        } catch (ServerException e) {
            logger.error("CreateMessage error from:{} , to:{} ", packetHead.getFrom(), packetHead.getTo(), e);
            return;
        }


        writeTo(channel, toChannel, packetHead, genericPacket, toUser, mid);

        if (serverReceiveConfirm) {
            if (StringUtils.isNotBlank(packetHead.getMessageID())) {
                messageService.addServerReceivedMessage(packetHead.getMessageID(), mid, cmgIdttl, fromUserID);
            }
            if (!isForward) {

                ack(channel, packetHead, MessageStatus.SERVER_RECEIVED, toUser, String.valueOf(mid), isForward);
            } else {
                executorInterBizServers.execute(() -> proxyMessageSender.write(packetHead.getFromRegion(),
                        encodeBiz2BizStatusMsg(channel.getChannelInfo(), confirmGenericPacket(packetHead, MessageStatus.SERVER_RECEIVED, null),
                                channel.getTraceId(), toUser.getCountrycode())));
            }
        }
    }


    private void handleProxy(Channel channel, V5PacketHead packetHead, V5GenericPacket genericPacket, String fromUserID, User toUser, Long mid, boolean serverReceiveConfirm) {
        if (logger.isDebugEnabled()) {
            logger.debug("forward systemMessage fromUser:{} , toUser:{} , packetHead:{} , genericPacket:{}",
                    packetHead.getFrom(), toUser.getId(), packetHead, genericPacket);
        }
        if (serverReceiveConfirm) {
            if (StringUtils.isNotBlank(packetHead.getMessageID())) {
                resendMsgToBizServer.saveSingleMsgForResend(fromUserID, packetHead.getMessageID(), toUser.getCountrycode(), toUser.getId(),
                        genericPacket.getSrcMsgBytes());
            }
            GenericPacket packet = new GenericPacket();
            packet.setV5GenericPacket(genericPacket);
            proxyMessageSender.write(toUser.getCountrycode(), packet);
            if (StringUtils.isNotBlank(packetHead.getMessageID())) {
                messageService.addServerReceivedMessage(packetHead.getMessageID(), mid, cmgIdttl, fromUserID);
            }
            ack(channel, packetHead, MessageStatus.SERVER_RECEIVED, toUser, String.valueOf(mid), false);

        } else {
            GenericPacket packet = new GenericPacket();
            packet.setV5GenericPacket(genericPacket);
            proxyMessageSender.write(toUser.getCountrycode(), packet);
        }

    }


    private Message createMessage(V5PacketHead packetHead, V5GenericPacket genericPacket, Long mid, com.handwin.message.bean.MessageStatus messageStatus) throws ServerException {
        Message message = null;
        if (packetHead.getClientReceivedConfirm() || packetHead.getPush()) {
            message = buildMessage(packetHead, genericPacket);
            message.setId(mid);
            message.setIsCount(0);
            GenericPacket packet = new GenericPacket();
            packet.setV5GenericPacket(genericPacket);
            messageService.createMessage(packetHead.getFrom(), packetHead.getTo(), message, messageStatus,
                    packetCodecs.encode(packetHead.getVersion(), packet));
            addMessageId2PacketHead(packetHead, message.getId());
        }
        return message;
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


    private void writeTo(Channel channel, Channel toChannel, V5PacketHead packetHead, V5GenericPacket genericPacket, User toUser, long messageID) {
        ChannelInfo toChannelInfo;
        if (toChannel != null && (toChannelInfo = toChannel.getChannelInfo()) != null &&
                toChannelInfo.getChannelMode() != ChannelMode.SUSPEND) {
            try {
                if (logger.isDebugEnabled()) {
                    logger.debug("Write System Message To: {}", toChannel.getChannelInfo());
                }
                byte[] trackBytes = SimpleWrongMessage.encode(toChannelInfo.getAppID(),
                        channel.getChannelInfo().getUserId(), toChannelInfo.getUserId(), messageID, channel.getTraceId());
                GenericPacket packet = new GenericPacket();
                packet.setV5GenericPacket(genericPacket);
                toChannel.write(packet, trackBytes, ChannelAction.SEND);
            } catch (Throwable e) {
                logger.error(e.getMessage(), e);
                handleOfflineMessage(packetHead, genericPacket, toUser, channel);
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("user {} offline, process offline system notify message.", toUser.getId());
            }
            try {
                handleOfflineMessage(packetHead, genericPacket, toUser, channel);
            } catch (Exception e) {
                logger.error("handleOfflineMessage error from:{} , to:{} ",
                        channel.getChannelInfo().getUserId(), toUser.getId(), e);
            }
        }

    }


    private void ack(Channel channel, V5PacketHead packetHead, MessageStatus messageStatus, User toUser, String messageID, boolean isForward) {
        if (isForward) {
            if (logger.isDebugEnabled()) {
                logger.debug("Is forward ack packetHead:{} , toUser:{}", packetHead, toUser);
            }
            executorInterBizServers.execute(() -> proxyMessageSender.write(packetHead.getFromRegion(),
                    encodeBiz2BizStatusMsg(channel.getChannelInfo(), confirmGenericPacket(packetHead, MessageStatus.SERVER_RECEIVED, messageID),
                            channel.getTraceId(), toUser.getCountrycode())));
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Is not forward ack packetHead:{} , toUser:{} ", packetHead, toUser);
            }
            GenericPacket genericPacket = confirmGenericPacket(packetHead, messageStatus, messageID);
            channel.write(genericPacket);
        }
    }


    private void handleOfflineMessage(V5PacketHead packetHead, V5GenericPacket genericPacket, User toUser, Channel fromChannel) {
        if (!packetHead.getPush()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Offline noNeedPush , fromUserID:{} , toUserID:{} ",
                        fromChannel.getChannelInfo().getUserId(), toUser.getId());
            }
            return;
        }
        String fromUserId = fromChannel.getChannelInfo().getUserId();
        if (conversationService.isInBlackOrInGreySheet(toUser.getId(), fromUserId,toUser.getAppId())) {
            if (logger.isDebugEnabled()) {
                logger.debug("Offline hasRight , fromUserID:{} is in toUserID:{} GreyOrBlackList",
                        fromUserId, toUser.getId());
            }
            return;
        }
        UserToken userToken = userService.getTokenInfo(toUser.getId(), toUser.getAppId());
        if (userToken == null || userToken.getDeviceType() > 2) {
            if (logger.isInfoEnabled()) {
                logger.info("Offline user {} have no push token or un supported device type", toUser.getId());
            }
            return;
        }
        messageService.pushText(packetHead, genericPacket, toUser, userToken, fromChannel.getTraceId());
    }


    private Message buildMessage(V5PacketHead packetHead, V5GenericPacket genericPacket) {
        Message message = new Message();
        message.setId(idGenerator.next());
        message.setContent(genericPacket.getBodySrcBytes());
        message.setSecret(0); //TODO 非加密信息
        message.setCreateTime(System.currentTimeMillis());
        message.setConversationId(packetHead.getFrom());
        message.setReceiverType(SystemConstant.TO_SEND_MESSAGE_RECEIVE_TYPE_PERSON);  //TODO
        message.setType(MessageType.DEFAULT_TYPE.name());
        return message;
    }


    private GenericPacket confirmGenericPacket(V5PacketHead packetHead, MessageStatus messageStatus, String messageID) {
        V5PacketHead confirmPacketHead = new V5PacketHead();
        confirmPacketHead.setHead(packetHead.getHead());
        confirmPacketHead.setPacketType(packetHead.getPacketType());
        confirmPacketHead.setVersion(packetHead.getVersion());
        confirmPacketHead.setService(ServiceType.Confirm.getType());
        confirmPacketHead.setMessageID(packetHead.getMessageID());
        confirmPacketHead.setTempId(packetHead.getTempId());

        V5GenericPacket confirmPacket = new V5GenericPacket();
        confirmPacket.setPacketHead(confirmPacketHead);

        confirmPacketHead.addHead(MessageStatus.STATUS, messageStatus.id());
        if (StringUtils.isNotBlank(messageID)) {
            confirmPacketHead.setServerMessageID(messageID);
        }

        GenericPacket genericPacket = new GenericPacket();
        genericPacket.setV5GenericPacket(confirmPacket);
        return genericPacket;
    }

    private GenericPacket responseGenericPacket(V5PacketHead packetHead, V5GenericPacket genericPacket) {
        V5PacketHead copyHead = packetHead.copy();
        String to = packetHead.getTo();
        String toRegion = packetHead.getToRegion();
        copyHead.setTo(packetHead.getFrom());
        copyHead.setToRegion(packetHead.getFromRegion());
        copyHead.setFrom(to);
        copyHead.setFromRegion(toRegion);

        V5GenericPacket v5GenericPacket = genericPacket.copy();
        v5GenericPacket.setPacketHead(copyHead);

        GenericPacket result = new GenericPacket();
        return result;
    }


    private void addMessageId2PacketHead(V5PacketHead packetHead, long messageID) {
        if (messageID > 0) {
            packetHead.setServerMessageID(String.valueOf(messageID));
        }
    }

}
