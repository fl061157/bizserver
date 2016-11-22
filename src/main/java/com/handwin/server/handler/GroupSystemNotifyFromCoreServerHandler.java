package com.handwin.server.handler;

import com.google.common.annotations.VisibleForTesting;
import com.handwin.bean.Platform;
import com.handwin.entity.ChannelInfo;
import com.handwin.entity.User;
import com.handwin.entity.UserToken;
import com.handwin.entity.wrong.SimpleWrongMessage;
import com.handwin.exception.ServerException;
import com.handwin.localentity.Message;
import com.handwin.localentity.MessageType;
import com.handwin.packet.*;
import com.handwin.server.Channel;
import com.handwin.server.ProxyMessageSender;
import com.handwin.server.proto.ChannelAction;
import com.handwin.service.ChannelService;
import com.handwin.service.ConversationService;
import com.handwin.service.MessageService;
import com.handwin.service.UserService;
import com.handwin.utils.ChannelUtils;
import com.handwin.utils.Snowflake;
import com.handwin.utils.SystemConstant;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by piguangtao on 15/11/30.
 */
@Service
public class GroupSystemNotifyFromCoreServerHandler implements Handler<SystemNotifyPacket> {
    private static final Logger LOGGER = LoggerFactory.getLogger(GroupSystemNotifyFromCoreServerHandler.class);


    @Autowired
    private UserService userService;

    @Value("${localidc.country.code}")
    private String localCountryCode;

    @Autowired
    private MessageService messageService;

    @Autowired
    private TaskExecutor executorInterBizServers;

    @Autowired
    protected ProxyMessageSender proxyMessageSender;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private Snowflake idGenerator;

    @VisibleForTesting
    @Value("${user.sent.ms.cmsgid.ttl}")
    public int cmgIdttl;

    @Autowired
    private ConversationService conversationService;

    @Override
    public void before(String traceId, SystemNotifyPacket packet) {

    }

    @Override
    public void after(String traceId, SystemNotifyPacket packet) {

    }

    @Override
    /**
     * 从外部部件发送过来的系统通知 channel可能为空 不能使用
     */
    public void handle(Channel channel, SystemNotifyPacket packet) {
        LOGGER.debug("[group system notify].channel:{}, packet:{}", channel, packet);
        if (null == packet) return;
        Map<String, Object> extraMap = packet.getExtra();
        if (null == extraMap) return;

        String from = org.apache.commons.lang3.StringUtils.trimToEmpty(packet.getFrom());


        ChannelInfo channelInfo = channel != null ? channel.getChannelInfo() : null;

        //TODO This Hold A Bug
        boolean isDuduUID = userService.isDuduUID(from);
        String tempFrom = from;
        if (isDuduUID) {
            int lastIndexOf = from.lastIndexOf("@{");
            if (lastIndexOf > 0) {
                tempFrom = from.substring(0, lastIndexOf);
            }
        }
        final User fromUser = userService.findById(tempFrom, isDuduUID ? 0 :
                channelInfo != null ? channelInfo.getAppID() : packet.getPacketHead().getAppId());
        if (fromUser == null) {
            LOGGER.warn("findFromUser empty:{} ", from);
            return;
        }

        if (StringUtils.isBlank(fromUser.getCountrycode())) {
            LOGGER.info("RegionCode Is Empty from.user.id :{} ", fromUser.getId());
            if (!userService.isSystemAccount(fromUser)) {
                return;
            }
            fromUser.setCountrycode(localCountryCode);
        }
        final Long mid = messageService.newMessageUID();
        User toUser = userService.findById(packet.getTo(), userService.isDuduUID(packet.getTo()) ? 0 : packet.getPacketHead().getAppId());
        if (toUser == null) {
            LOGGER.error(String.format("not find user.id %s", packet.getTo()));
            return;
        }
        if (StringUtils.isBlank(toUser.getCountrycode())) {
            LOGGER.info("RegionCode Is Empty user.id :{} ", toUser.getId());
            if (!userService.isSystemAccount(toUser)) {
                return;
            }
            toUser.setCountrycode(localCountryCode);
        }

        //发送方不在本区时，判断为转发处理
        boolean isForward = StringUtils.isNotBlank(fromUser.getCountrycode()) && !userService.isLocalUser(fromUser.getCountrycode());

        if (StringUtils.isNotBlank(packet.getCmsgId())) {
            Long cachedMessageId = messageService.isServerReceived(packet.getCmsgId(),
                    SystemConstant.MSGFLAG_RESENT, fromUser.getId());
            if (cachedMessageId != null) {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("[SystemNotifyHandler MESSAGE]message {}-{} is already received",
                            cachedMessageId, packet.getCmsgId());
                }
//                if (isForward) {
//                    //跨区转发时 需要回复ack
//                    ackMsgRespForForward(packet, fromUser, cachedMessageId);
//                }
                return;
            }
        }


        boolean isToUserIDC = userService.isLocalUser(toUser.getCountrycode());

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("isForward:{} , isToUserIDC:{} , mid:{}", isForward, isToUserIDC, mid);
        }

        if (isToUserIDC) {
            handleToUserIDC(packet, fromUser, toUser, mid, isForward);
        } else if (!isForward) {
            handleProxy(packet, fromUser, toUser.getCountrycode(), mid);
        } else {
            LOGGER.error("ToUser not belong this idc and is forward packet:{} ", packet);
        }
    }


    private void handleToUserIDC(SystemNotifyPacket p, User fromUser, User toUser, Long mid, boolean isForward) {

        Map<Platform, List<Channel>> toChannelMap = null;
        //Channel toChannel = null;
        try {
            //toChannel = channelService.findChannel(toUser);
            toChannelMap = channelService.findChannelMap(toUser);
        } catch (ServerException e) {
            LOGGER.error(String.format("write systemMessage to user.id:%s error", toUser.getId()), e);
        }
        createMessage(p, mid, ChannelUtils.isOffline(toChannelMap) ? com.handwin.message.bean.MessageStatus.UNDEAL : com.handwin.message.bean.MessageStatus.ONLINE);

        writeTo(toChannelMap, p, toUser, mid);

        //只有跨区转发的才需要回复
//        if (isForward) {
//            ackMsgRespForForward(p, fromUser, mid);
//        }
    }


    public MessageResponsePacket createMessageResponsePacket(MessageStatus messageStatus, SystemNotifyPacket packet, Long serverMessageId) {
        MessageResponsePacket messageResponsePacket = new MessageResponsePacket();
        messageResponsePacket.setMessageType(SimpleMessagePacket.STATUS_RESPONSE_MESSAGE_TYPE);
        messageResponsePacket.setMessageStatus(messageStatus);
        messageResponsePacket.setMessageId(serverMessageId);
        messageResponsePacket.setTempId(packet.getPacketHead().getTempId());
        messageResponsePacket.setCmsgid(packet.getCmsgId());
        return messageResponsePacket;
    }

    private void createMessage(SystemNotifyPacket p, Long mid, com.handwin.message.bean.MessageStatus messageStatus) throws ServerException {
        if (p.isNeedSave() || p.isNeedAck()) {
            Message message = buildMessage(p);
            message.setId(mid);
            messageService.createMessage(p.getFrom(), p.getTo(), message, messageStatus, p.getMessageBody().getBytes(Charset.forName("UTF-8")));
        }
    }

    private Message buildMessage(SystemNotifyPacket p) {
        Message message = new Message();
        message.setId(idGenerator.next());
        message.setContent(p.getMessageBody().getBytes(Charset.forName("UTF-8")));
        message.setSecret(0);
        message.setCreateTime(System.currentTimeMillis());
        message.setConversationId((String) p.getExtra().get(SystemConstant.SYSTEM_NOTIFY_EXTRA_KEY_GROUP_ID));
        message.setReceiverType(SystemConstant.TO_SEND_MESSAGE_RECEIVE_TYPE_GROUP);
        message.setType(MessageType.SYSTEM_NOTIFY.name());
        message.setIsCount(p.isUnreadInc() ? 0 : 1);
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

    private void writeTo(Map<Platform, List<Channel>> toChannelMap, SystemNotifyPacket p, User toUser, long messageID) {
        boolean offlinePush = false;

        if (MapUtils.isNotEmpty(toChannelMap)) {
            for (List<Channel> channels : toChannelMap.values()) {
                for (Channel toChannel : channels) {
                    if (toChannel != null && toChannel.getChannelInfo() != null &&
                            (toChannel.getChannelInfo().findPlatform() != Platform.Mobile ||
                                    (toChannel.getChannelInfo().findPlatform() == Platform.Mobile
                                            && toChannel.getChannelInfo().getChannelMode() != ChannelMode.SUSPEND))) {
                        try {
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("Write System Message To: {}", toChannel.getChannelInfo());
                            }
                            byte[] trackBytes = SimpleWrongMessage.encode(toChannel.getChannelInfo().getAppID(),
                                    p.getFrom(), toChannel.getChannelInfo().getUserId(), messageID, p.getTraceId());
                            p.setMsgId(messageID);
                            toChannel.write(p, trackBytes, ChannelAction.SEND);
                        } catch (Throwable e) {
                            LOGGER.error(e.getMessage(), e);
                            if (toChannel.getChannelInfo().findPlatform() == Platform.Mobile) {
                                offlinePush = true;
                            }
                        }
                    }
                }

            }
        }

        if (ChannelUtils.isOffline(toChannelMap) || offlinePush) {
            handleOfflineMessage(p, toUser);
        }


    }

    private void handleOfflineMessage(SystemNotifyPacket notifyPacket, User toUser) {
        if (notifyPacket != null && !notifyPacket.isNeedPush()) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Offline noNeedPush , fromUserID:{} , toUserID:{} ",
                        notifyPacket.getFrom(), toUser.getId());
            }
            return;
        }
        String groupId = getGroupId(notifyPacket);
        if (StringUtils.isBlank(groupId)) {
            LOGGER.debug("Offline noNeedPush , fromUserID:{} , toUserID:{},no group. ",
                    notifyPacket.getFrom(), toUser.getId());
            return;
        }

        if (conversationService.isNoDisturbForGroup(toUser.getId(), groupId, notifyPacket.getPacketHead().getAppId())) {
            LOGGER.debug("Offline noNeedPush , fromUserID:{} , toUserID:{}, groupId:{} no disturb. ",
                    notifyPacket.getFrom(), toUser.getId(), groupId);
            return;
        }

        UserToken userToken = userService.getTokenInfo(toUser.getId(), toUser.getAppId());
        if (userToken == null || userToken.getDeviceType() > 2) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Offline user {} have no push token or un supported device type", toUser.getId());
            }
            return;
        }
        messageService.pushText(notifyPacket, toUser, userToken, notifyPacket.getTraceId());
    }

    private void handleProxy(SystemNotifyPacket p, User fromUser, String toUserCountryCode, Long mid) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("handleProxy systemNotifyPacket:{} , fromuser.id:{} , toUser.countrycode:{}",
                    p, fromUser.getId(), toUserCountryCode);
        }
        writeProxy(toUserCountryCode, p);
        if (StringUtils.isNotBlank(p.getCmsgId())) {
            messageService.addServerReceivedMessage(p.getCmsgId(), mid, cmgIdttl, fromUser.getId());
        }
    }

    private void writeProxy(String toUserRegion, SystemNotifyPacket p) {
        executorInterBizServers.execute(() -> proxyMessageSender.write(toUserRegion, p));
    }

//    private void ackMsgRespForForward(SystemNotifyPacket packet, User fromUser, Long serverMessageId) {
//        //跨区转发时 需要回复ack
//        MessageResponsePacket responsePacket = createMessageResponsePacket(MessageStatus.SERVER_RECEIVED, packet, serverMessageId);
//        executorInterBizServers.execute(() -> proxyMessageSender.write(fromUser.getCountrycode(), responsePacket));
//    }


    public String getGroupId(SystemNotifyPacket packet) {
        String groupId = null;
        if (null != packet && null != packet.getExtra()) {
            if (null != packet.getExtra().get(SystemConstant.SYSTEM_NOTIFY_EXTRA_KEY_GROUP_ID)) {
                groupId = (String) packet.getExtra().get(SystemConstant.SYSTEM_NOTIFY_EXTRA_KEY_GROUP_ID);
            }
        }
        return groupId;
    }
}
