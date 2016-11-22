package com.handwin.server.handler;

import com.alibaba.fastjson.JSON;
import com.google.common.annotations.VisibleForTesting;
import com.handwin.bean.Platform;
import com.handwin.codec.PacketCodecs;
import com.handwin.entity.ChannelInfo;
import com.handwin.entity.User;
import com.handwin.entity.UserBehaviouAttr;
import com.handwin.entity.UserToken;
import com.handwin.entity.wrong.SimpleWrongMessage;
import com.handwin.exception.ServerException;
import com.handwin.localentity.Message;
import com.handwin.localentity.MessageType;
import com.handwin.packet.*;
import com.handwin.rabbitmq.MessageBuilder;
import com.handwin.server.Channel;
import com.handwin.server.proto.ChannelAction;
import com.handwin.service.*;
import com.handwin.utils.ChannelUtils;
import com.handwin.utils.Snowflake;
import com.handwin.utils.SystemConstant;
import com.handwin.utils.VersionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SystemNotifyHandler extends AbstractHandler<SystemNotifyPacket> implements
        InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(SystemNotifyHandler.class);

    @Autowired
    private MessageService messageService;

    @Autowired
    private TcpSessionService onlineStatusService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private Snowflake idGenerator;

    @Autowired
    private ConversationService conversationService;

    @Autowired
    private PacketCodecs packetCodecs;

    @Autowired
    private MessageBuilder messageBuilder;

    @Autowired
    private TaskExecutor executorInterBizServers;


    @Autowired
    private GroupService groupService;


    @Value("${idc.country.codes}")
    private String idcCountryCodes;

    @Value("${default.country.code}")
    private String defaultCountryCode;

    @Value("${localidc.country.code}")
    private String localCountryCode;

    @VisibleForTesting
    @Value("${user.sent.ms.cmsgid.ttl}")
    public int cmgIdttl;

    @Autowired
    private GroupSystemNotifyFromCoreServerHandler groupSystemNotifyFromCoreServerHandler;

    public void afterPropertiesSet() throws Exception {
        register(SystemNotifyPacket.class);
    }

    @Override
    public void handle(Channel channel, SystemNotifyPacket p) {
        //需要对CoreServe发送的群组系统通知进行特殊处理 TODO 迁移新协议后处理
        Map<String, Object> extra = p.getExtra();
        if (null != extra && SystemConstant.SYSTEM_NOTIFY_EXTRAT_TYPE_GROUP.equals(extra.get(SystemConstant.SYSTEM_NOTIFY_EXTRA_KEY_TYPE))) {
            groupSystemNotifyFromCoreServerHandler.handle(channel, p);
            return;
        }


        final String from = org.apache.commons.lang3.StringUtils.trimToEmpty(channel.getChannelInfo().getUserId());
        boolean isDuduUID = userService.isDuduUID(from);
        String tempFrom = from;
        if (isDuduUID) {
            int lastIndexOf = from.lastIndexOf("@{");
            if (lastIndexOf > 0) {
                tempFrom = from.substring(0, lastIndexOf);
            }
        }
        final User fromUser = userService.findById(tempFrom, isDuduUID ? 0 : channel.getChannelInfo().getAppID());

        if (StringUtils.isBlank(from)) {
            logger.warn("findFromUser empty channel:{} ", channel);
            return;
        }

        if (fromUser == null) {
            logger.warn("findFromUser empty:{} ", from);
            return;
        }

        if (channel.getChannelInfo().getAppID() != 0 && p.getPacketHead() != null) {
            p.getPacketHead().setAppId(channel.getChannelInfo().getAppID());
        }

        if (StringUtils.isBlank(fromUser.getCountrycode())) {
            logger.info("RegionCode Is Empty from.user.id :{} ", fromUser.getId());
            if (!userService.isSystemAccount(fromUser)) {
                return;
            }
            fromUser.setCountrycode(localCountryCode); // TODO
        }

        boolean isForward = StringUtils.isNotBlank(fromUser.getCountrycode()) && !userService.isLocalUser(fromUser.getCountrycode());
        final Long mid = messageService.newMessageUID();


        if (StringUtils.isNotBlank(p.getCmsgId())) {
            Long cachedMessageId = messageService.isServerReceived(p.getCmsgId(),
                    SystemConstant.MSGFLAG_RESENT, fromUser.getId());
            if (cachedMessageId != null) {
                if (logger.isInfoEnabled()) {
                    logger.info("[SystemNotifyHandler MESSAGE]message {}-{} is already received",
                            cachedMessageId, p.getCmsgId());
                }
                MessageResponsePacket responsePacket = createMessageResponsePacket(MessageStatus.SERVER_RECEIVED, p);
                if (!isForward) {
                    ack(channel, responsePacket);
                }
                return;
            }
        }


        if (!p.isGroup()) {

            User toUser = userService.findById(p.getTo(), userService.isDuduUID(p.getTo()) ? 0 : p.getPacketHead().getAppId());
            if (toUser == null) {
                logger.error(String.format("not find user.id %s", p.getTo()));
                return;
            }
            if (StringUtils.isBlank(toUser.getCountrycode())) {
                logger.info("RegionCode Is Empty user.id :{} ", toUser.getId());
                if (!userService.isSystemAccount(toUser)) {
                    return;
                }
                toUser.setCountrycode(localCountryCode);
            }

            boolean isToUserIDC = userService.isLocalUser(toUser.getCountrycode());

            if (logger.isDebugEnabled()) {
                logger.debug("isForward:{} , isToUserIDC:{} , mid:{}", isForward, isToUserIDC, mid);
            }


            if (isToUserIDC) {
                handleToUserIDC(channel, p, fromUser, toUser, mid, isForward, p.getCmsgId(), false);
            } else if (!isForward) {
                handleProxy(channel, p, fromUser, toUser, mid, false);
            } else {
                logger.error("ToUser not belong this idc and is forward packet:{} ", p);
            }
        } else {

            if (!isForward) {
                ack(channel, p, mid);
            }

            List<User> toUserList;
            try {
                toUserList = groupService.findUserExcludeById(from, p.getTo(), p.getPacketHead().getAppId());
            } catch (ServerException e) {
                logger.error("findGroup error", e);
                return;
            }

            toUserList.stream()
                    .filter(user -> StringUtils.isBlank(user.getCountrycode())
                            || userService.isLocalUser(user.getCountrycode()))
                    .forEach(to -> handleToUserIDC(channel, p, fromUser, to, mid, isForward, p.getCmsgId(), true));

            if (!isForward) {

                toUserList.stream()
                        .map(User::getCountrycode)
                        .filter(countryCode -> StringUtils.isNotBlank(countryCode)
                                && !userService.isLocalUser(countryCode))
                        .map(userCountryCode -> idcCountryCodes.contains(userCountryCode) ? userCountryCode : defaultCountryCode)
                        .distinct()
                        .forEach(countryCode -> handleProxy(channel, p, fromUser, countryCode, mid, true));

            }


        }

    }


    private void handleProxy(Channel channel, SystemNotifyPacket p, User fromUser, User toUser, Long mid, boolean isGroup) {
        if (logger.isInfoEnabled()) {
            logger.info("handleProxy channel:{} , systemNotifyPacket:{} , fromuser.id:{} , toUser.id:{}",
                    channel, p, fromUser.getId(), toUser.getId());
        }
        writeProxy(toUser.getCountrycode(), p);
        if (StringUtils.isNotBlank(p.getCmsgId())) {
            messageService.addServerReceivedMessage(p.getCmsgId(), mid, cmgIdttl, fromUser.getId());
        }

        if (!isGroup) {
            MessageResponsePacket responsePacket = createMessageResponsePacket(MessageStatus.SERVER_RECEIVED, p);
            responsePacket.setMessageId(mid);
            ack(channel, responsePacket);
        }
    }

    private void handleProxy(Channel channel, SystemNotifyPacket p, User fromUser, String toUserCountryCode, Long mid, boolean isGroup) {
        if (logger.isDebugEnabled()) {
            logger.debug("handleProxy channel:{} , systemNotifyPacket:{} , fromuser.id:{} , toUser.countrycode:{}",
                    channel, p, fromUser.getId(), toUserCountryCode);
        }
        writeProxy(toUserCountryCode, p);
        if (StringUtils.isNotBlank(p.getCmsgId())) {
            messageService.addServerReceivedMessage(p.getCmsgId(), mid, cmgIdttl, fromUser.getId());
        }

        if (!isGroup) {
            MessageResponsePacket responsePacket = createMessageResponsePacket(MessageStatus.SERVER_RECEIVED, p);
            responsePacket.setMessageId(mid);
            ack(channel, responsePacket);
        }
    }


    private void handleToUserIDC(Channel channel, SystemNotifyPacket p, User fromUser, User toUser, Long mid, boolean isForward, String cmsgID, boolean isGroup) {
        if (logger.isDebugEnabled()) {
            logger.debug("handleToUserIDC channel:channel, {} , systemNotifyPacket:{} , fromUser.id:{} , toUser.id:{} ",
                    channel, p, fromUser.getId(), toUser.getId());
        }

        //系统通知 支持群组 如果是群组则走群组的校验规则
        if (!p.isGroup()) {
            if (conversationService.isInBlackSheet(toUser.getId(), fromUser.getId(), p.getPacketHead().getAppId())) {
                if (logger.isInfoEnabled()) {
                    logger.info("[SytemNoitfyMessage]user {} int the user {} black list",
                            fromUser.getId(), toUser.getId());
                }
                ackMsgResp(isForward, cmsgID, p, fromUser, channel, toUser, mid, isGroup);
                return;
            }
        } else {

            if (p.getMsgType() == 23) {
                String ct = p.getMessageBody();
                if (StringUtils.isNotBlank(ct)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("[Group Like] MessageBody:{}", ct);
                    }
                    try {
                        int appID = (p.getPacketHead() == null || p.getPacketHead().getAppId() == 0) ? fromUser.getAppId() : p.getPacketHead().getAppId();
                        Map<String, Object> mt = JSON.parseObject(ct, Map.class);
                        Object ot;
                        if (mt != null && (ot = mt.get("type")) != null && ot.equals("group_comment") && appID == 0) {
                            if (logger.isDebugEnabled()) {
                                logger.debug("[Group Like]: type:{}", ot);
                            }
                            UserBehaviouAttr uba = userService.getUserBehaviouAttr(toUser.getId(), toUser.getAppId());
                            boolean likeSupport = uba != null ? VersionUtils.isLikeSupportVersion(uba) : false;
                            if (!likeSupport) {
                                return;
                            } else {
                                if (logger.isDebugEnabled()) {
                                    logger.debug("[Group Like]: UserID:{} , Version:{}", uba.getUserAppIdkey(), uba.getClientVersion());
                                }
                            }
                        }
                    } catch (Exception e) {
                    }
                }
            }
        }

        Map<Platform, List<Channel>> channelMap = channelService.findChannelMap(toUser);

//        Channel toChannel = null;
//        try {
//            toChannel = channelService.findChannel(toUser);
//        } catch (ServerException e) {
//            logger.error(String.format("write systemMessage to user.id:%s error", toUser.getId()), e);
//        }
        createMessage(p, mid, ChannelUtils.isOffline(channelMap) ? com.handwin.message.bean.MessageStatus.UNDEAL : com.handwin.message.bean.MessageStatus.ONLINE, toUser);

        if (MapUtils.isNotEmpty(channelMap)) {
            for (Map.Entry<Platform, List<Channel>> entry : channelMap.entrySet()) {
                for (Channel toChannel : entry.getValue()) {
                    writeTo(channel, toChannel, p, toUser, mid);
                }
            }
        }

        if (ChannelUtils.isOffline(channelMap)) {

            if (logger.isDebugEnabled()) {
                logger.debug("user {} offline, process offline system notify message.", toUser.getId());
            }
            handleOfflineMessage(p, toUser, channel);
        }

        ackMsgResp(isForward, cmsgID, p, fromUser, channel, toUser, mid, isGroup);

    }

    private void ackMsgResp(boolean isForward, String cmsgID, SystemNotifyPacket p, User fromUser, Channel channel, User toUser, Long mid, boolean isGroup) {
        if (StringUtils.isNotBlank(cmsgID)) {
            messageService.addServerReceivedMessage(p.getCmsgId(), mid, cmgIdttl, fromUser.getId());
        }
        if (!isForward && !isGroup) {
            MessageResponsePacket responsePacket = createMessageResponsePacket(MessageStatus.SERVER_RECEIVED, p);
            responsePacket.setMessageId(mid);
            ack(channel, responsePacket);
        }
    }


    private void writeProxy(String toUserRegion, SystemNotifyPacket p) {
        executorInterBizServers.execute(() -> proxyMessageSender.write(toUserRegion, p));
    }


    public void writeTo(Channel channel, Channel toChannel, SystemNotifyPacket p, User toUser, long messageID) {

        ChannelInfo toChannelInfo;
        try {
            if (toChannel != null && (toChannelInfo = toChannel.getChannelInfo()) != null &&
                    ((toChannelInfo.getChannelMode() != ChannelMode.SUSPEND && toChannelInfo.findPlatform() == Platform.Mobile) || toChannelInfo.findPlatform() != Platform.Mobile)) {
                try {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Write System Message To: {}", toChannel.getChannelInfo());
                    }
                    byte[] trackBytes = SimpleWrongMessage.encode(toChannelInfo.getAppID(),
                            channel.getChannelInfo().getUserId(), toChannelInfo.getUserId(), messageID, channel.getTraceId());
                    p.setMsgId(messageID);
                    toChannel.write(p, trackBytes, ChannelAction.SEND);
                } catch (Throwable e) {
                    logger.error(e.getMessage(), e);
                    handleOfflineMessage(p, toUser, channel);
                }
            }
        } catch (Throwable e) {
            logger.error(String.format("write message toUser:%s error", toUser.getId()), e);
        }
    }

    private void ack(Channel channel, SystemNotifyPacket p, Long mid) {
        MessageResponsePacket responsePacket = createMessageResponsePacket(MessageStatus.SERVER_RECEIVED, p);
        responsePacket.setMessageId(mid);
        ack(channel, responsePacket);
    }


    private void ack(Channel channel, MessageResponsePacket responsePacket) {
        channel.write(responsePacket);
    }


    private void createMessage(SystemNotifyPacket p, Long mid, com.handwin.message.bean.MessageStatus messageStatus, User toUser) throws ServerException {
        if (p.isNeedSave() || p.isNeedAck()) {
            Message message = buildMessage(p);
            message.setId(mid);
            if (p.isGroup()) {
                messageService.createMessage(p.getFrom(), toUser.getId(), message, messageStatus, p.getMessageBody().getBytes(Charset.forName("UTF-8")));
            } else {
                messageService.createMessage(p.getFrom(), p.getTo(), message, messageStatus, p.getMessageBody().getBytes(Charset.forName("UTF-8")));
            }
        }
    }

    private Message buildMessage(SystemNotifyPacket p) {
        Message message = new Message();
        message.setId(idGenerator.next());
        message.setContent(p.getMessageBody().getBytes(Charset.forName("UTF-8")));
        message.setSecret(0);
        message.setCreateTime(System.currentTimeMillis());
        message.setConversationId(p.getFrom());
        message.setReceiverType(SystemConstant.TO_SEND_MESSAGE_RECEIVE_TYPE_PERSON);
        message.setType(MessageType.SYSTEM_NOTIFY.name());
        message.setIsCount(p.isUnreadInc() ? 0 : 1);

        //群组系统通知
        if (null != p.getGroupId()) {
            message.setConversationId(p.getGroupId());
            message.setReceiverType(SystemConstant.TO_SEND_MESSAGE_RECEIVE_TYPE_GROUP);
        }
        Map<String, Object> metaMap = new HashMap<>();
        metaMap.put("cmsgid", p.getCmsgId());
        metaMap.put("entity_format", SystemConstant.MESSAGE_ENTITY_SYSTEM_NOTIRY);

        if (p.isNeedPush() && StringUtils.isNotBlank(p.getPushContentBody())) {
            metaMap.put("push_content", p.getPushContentBody());
        }
        if (null != p.getExtra()) {
            if (null != p.getExtra().get(SystemConstant.SYSTEM_NOTIFY_EXTRA_PUSH_ENTER_CONVERSATION)) {
                metaMap.put(SystemConstant.SYSTEM_NOTIFY_EXTRA_PUSH_ENTER_CONVERSATION, p.getExtra().get(SystemConstant.SYSTEM_NOTIFY_EXTRA_PUSH_ENTER_CONVERSATION));
            }
        }
        message.setMeta(metaMap);
        return message;
    }


    private void handleOfflineMessage(SystemNotifyPacket notifyPacket, User toUser, Channel fromChannel) {
        if (notifyPacket != null && !notifyPacket.isNeedPush()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Offline noNeedPush , fromUserID:{} , toUserID:{} ",
                        fromChannel.getChannelInfo().getUserId(), toUser.getId());
            }
            return;
        }
        String fromUserId = fromChannel.getChannelInfo().getUserId();
        if (conversationService.isInBlackOrInGreySheet(toUser.getId(), fromUserId, toUser.getAppId())) {
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


        boolean noDisturb = false;
        if (notifyPacket.isGroup()) {
            try {
                noDisturb = conversationService.isNoDisturbForGroup(toUser.getId(), notifyPacket.getTo(), notifyPacket.getPacketHead().getAppId());
            } catch (ServerException e) {
            }
        }

        if (logger.isDebugEnabled()) {
            if (noDisturb) {
                logger.debug("User Is NoDisturb User.id:{} ", toUser.getId());
            }
        }

        if (!noDisturb) {

            messageService.pushText(notifyPacket, toUser, userToken, fromChannel.getTraceId());
        }

    }


    private MessageResponsePacket createMessageResponsePacket(MessageStatus messageStatus, SystemNotifyPacket packet) {
        MessageResponsePacket messageResponsePacket = new MessageResponsePacket();
        messageResponsePacket.setMessageType(SimpleMessagePacket.STATUS_RESPONSE_MESSAGE_TYPE);
        messageResponsePacket.setMessageStatus(messageStatus);
        messageResponsePacket.setMessageId(packet.getMsgId());
        messageResponsePacket.setTempId(packet.getPacketHead().getTempId());
        messageResponsePacket.setCmsgid(packet.getCmsgId());
        return messageResponsePacket;
    }


}
