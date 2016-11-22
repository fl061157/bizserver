package com.handwin.service;

import com.handwin.entity.User;
import com.handwin.entity.UserToken;
import com.handwin.localentity.Message;
import com.handwin.localentity.MessageType;
import com.handwin.packet.*;
import com.handwin.protocal.v5.codec.V5GenericPacket;
import com.handwin.protocal.v5.codec.V5PacketHead;
import com.handwin.server.Channel;
import com.handwin.server.ProxyMessageSender;
import com.handwin.utils.SystemConstant;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by piguangtao on 15/11/27.
 */
@Service
public class GenericGroupService {
    private static final Logger LOGGER = LoggerFactory.getLogger(GenericGroupService.class);

    /**
     * 消息内 内容为字节数组
     */
    public static final String GENERIC_GROUP_SYSTEM_NOTIFY_BODY_MSG = "msg";

    /**
     * 群组id
     */
    private static final String HEAD_GROUP_ID = "group-id";

    @Autowired
    private UserService userService;

    @Autowired
    private MessageService messageService;
    @Autowired
    private TaskExecutor executorInterBizServers;

    @Autowired
    protected ProxyMessageSender proxyMessageSender;


    public Message buildGroupSystemNotifyMessage(V5GenericPacket genericPacket, Long serverMessageId) {
        V5PacketHead packetHead = genericPacket.getPacketHead();
        Message message = new Message();
        message.setId(serverMessageId);
        message.setContent(genericPacket.getBodyMap().get(GENERIC_GROUP_SYSTEM_NOTIFY_BODY_MSG));
        message.setSecret(0);
        message.setCreateTime(System.currentTimeMillis());

        //群组id
        message.setConversationId((String) packetHead.getHead(HEAD_GROUP_ID));
        message.setReceiverType(SystemConstant.TO_SEND_MESSAGE_RECEIVE_TYPE_GROUP);
        message.setType(MessageType.SYSTEM_NOTIFY.name());
        message.setIsCount(packetHead.getPushIncr() ? 0 : 1);

        Map<String, Object> metaMap = new HashMap<>();
        metaMap.put("cmsgid", packetHead.getMessageID());
        metaMap.put("entity_format", SystemConstant.MESSAGE_ENTITY_SYSTEM_NOTIRY);
        message.setMeta(metaMap);
        message.setSender(packetHead.getFrom());
        return message;
    }

    public SystemNotifyPacket transToSystemNotify(V5GenericPacket genericPacket, Long serverMessageId) {
        SystemNotifyPacket notifyPacket = new SystemNotifyPacket();
        PacketHead packetHead = new PacketHead();
        packetHead.setHead((byte) 0xb7);
        packetHead.setVersion((byte) 0x04);
        packetHead.setPacketType((byte) 0x08);
        packetHead.setSecret((byte) 0x00);
        packetHead.setTimestamp(System.currentTimeMillis());
        packetHead.setTempId(genericPacket.getPacketHead().getTempId());
        packetHead.setAppId((byte) 0x00);

        //群组通知的msgtype
        notifyPacket.setMsgType((byte) 0x01);
        notifyPacket.setServeType(getServerType(genericPacket.getPacketHead()));
        //from设置为群组Id
        notifyPacket.setFrom((String) genericPacket.getPacketHead().getHead(HEAD_GROUP_ID));
        notifyPacket.setTo(genericPacket.getPacketHead().getTo());
        //不需要设置push（客户端不需要push内容）
        notifyPacket.setMessageBody(new String((byte[]) genericPacket.getBodyMap().get(GenericGroupService.GENERIC_GROUP_SYSTEM_NOTIFY_BODY_MSG), StandardCharsets.UTF_8));
        notifyPacket.setMsgId(serverMessageId);
        notifyPacket.setCmsgId(genericPacket.getPacketHead().getMessageID());
        return notifyPacket;
    }

    protected byte getServerType(V5PacketHead packetHead) {
        byte result = (byte) ((packetHead.getStore() ? (byte) 0x01 : (byte) 0x00) |
                (packetHead.getClientReceivedConfirm() ? (byte) 0x02 : (byte) 0x00) |
                (packetHead.getPush() ? (byte) 0x04 : (byte) 0x00) |
                (packetHead.getPushIncr() ? (byte) 0x08 : (byte) 0x00) |
                (packetHead.getEnsureArrive() ? (byte) 0x10 : (byte) 0x00)
        );

        return result;
    }

    /**
     * 发送离线消息
     *
     * @param genericPacket
     * @param notifyPacket
     */
    public void handleOfflineMessage(V5GenericPacket genericPacket, SystemNotifyPacket notifyPacket) {
        if (null == genericPacket) return;

        //不需要推送
        if (!genericPacket.getPacketHead().getPush()) {
            return;
        }
        String toUserId = genericPacket.getPacketHead().getTo();
        User toUser = userService.findById(toUserId, genericPacket.getPacketHead().getAppId() );
        if (null == toUser) return;

        UserToken userToken = userService.getTokenInfo(toUser.getId(), toUser.getAppId());
        if (userToken == null || userToken.getDeviceType() > 2) {
            LOGGER.info("Offline user {} have no push token or un supported device type", toUser.getId());
            return;
        }
        messageService.pushText(notifyPacket, toUser, userToken, genericPacket.getPacketHead().getTraceId());
    }

    public void writeProx(String toUserCountryCode, V5GenericPacket genericPackePar) {
        if (null == toUserCountryCode || null == genericPackePar) return;

        GenericPacket genericPacket = new GenericPacket();
        genericPacket.setV5GenericPacket(genericPackePar);

        executorInterBizServers.execute(() -> proxyMessageSender.write(toUserCountryCode, genericPacket));
    }

    public User getFromUser(Channel channel) {
        User result = null;
        if (null == channel) return result;
        final String from = channel.getChannelInfo().getUserId();
        if (StringUtils.isBlank(from)) return result;
        result = userService.findById(from , channel.getChannelInfo().getAppID() );
        return result;
    }

    public MessageResponsePacket createMessageResponsePacket(MessageStatus messageStatus, V5GenericPacket packet, Long serverMessageId) {
        MessageResponsePacket messageResponsePacket = new MessageResponsePacket();
        messageResponsePacket.setMessageType(SimpleMessagePacket.STATUS_RESPONSE_MESSAGE_TYPE);
        messageResponsePacket.setMessageStatus(messageStatus);
        messageResponsePacket.setMessageId(serverMessageId);
        messageResponsePacket.setTempId(packet.getPacketHead().getTempId());
        messageResponsePacket.setCmsgid(packet.getPacketHead().getMessageID());
        return messageResponsePacket;
    }

    public void ack(boolean isForward, Channel channel, V5GenericPacket genericPacket, Long serverMessageId, User fromUser) {
        if (isForward) {
            //跨区转发时 需要回复ack
            MessageResponsePacket responsePacket = createMessageResponsePacket(MessageStatus.SERVER_RECEIVED, genericPacket, serverMessageId);
            executorInterBizServers.execute(() -> proxyMessageSender.write(fromUser.getCountrycode(), responsePacket));
        } else {
            //本区需要确认时
            if (genericPacket.getPacketHead().getServerReceivedConfirm()) {
                MessageResponsePacket responsePacket = createMessageResponsePacket(MessageStatus.SERVER_RECEIVED, genericPacket, serverMessageId);
                channel.write(responsePacket);
            }
        }
    }
}
