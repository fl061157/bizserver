package com.handwin.service;

import com.handwin.entity.ChannelInfo;
import com.handwin.entity.User;
import com.handwin.entity.UserToken;
import com.handwin.entity.wrong.CallWrongMessage;
import com.handwin.entity.wrong.SimpleWrongMessage;
import com.handwin.entity.wrong.WrongMessage;
import com.handwin.localentity.Message;
import com.handwin.message.bean.MessageStatus;
import com.handwin.packet.CallPacket;
import com.handwin.packet.CallStatus;
import com.handwin.packet.PacketHead;
import com.handwin.server.AbstractChannelImpl;
import com.handwin.server.Channel;
import com.handwin.server.proto.ChannelAction;
import com.handwin.utils.ChannelUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Created by fangliang on 18/5/15.
 */
@Service
public class FailingService {

    private static final Logger logger = LoggerFactory.getLogger(FailingService.class);

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @Autowired
    private TcpSessionService onlineStatusService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private CallService callService;


    public void handle(byte[] packetBody, byte[] trackBody, String traceID) {
        if (trackBody == null || trackBody.length == 0) {
            return;
        }
        ByteBuf buf = Unpooled.copiedBuffer(trackBody);
        if (logger.isDebugEnabled()) {
            logger.debug("WrongServerMessageHandler handle traceId:{}", traceID);
        }
        try {
            WrongMessage wrongMessage = null;
            try {
                wrongMessage = decodeWrongMessage(buf);
            } catch (IOException e) {
                logger.error("[FailingService]  format error content:{} ", wrongMessage.getContent());
                return;
            }
            switch (wrongMessage.getWrongMessageType()) {
                case WrongMessage.SIMPLE_WRONG_MESSAGE_TYPE:
                    SimpleWrongMessage simpleWrongMessage = (SimpleWrongMessage) wrongMessage;
                    simpleWrongMessage.setTraceId(traceID);
                    handle(packetBody, simpleWrongMessage);
                    break;
                case WrongMessage.CALL_WRONG_MESSAGE_TYPE:
                    CallWrongMessage callWrongMessage = (CallWrongMessage) wrongMessage;
                    callWrongMessage.setTraceId(traceID);
                    handle(callWrongMessage);
                    break;
            }
        } catch (Exception e) {
            logger.error("[WrongServerMessageHandler] parse error: " );
        } finally {
            buf.release();
        }

    }


    private void handle(CallWrongMessage callWrongMessage) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("[WrongServerMessageHandler] handle callWrongMessage , roomId:{} , from:{} , to:{} ",
                    callWrongMessage.getRoomID(), callWrongMessage.getFrom(), callWrongMessage.getTo());
        }
        String from = callWrongMessage.getFrom();
        String to = callWrongMessage.getTo();
        User toUser = userService.findById(to, callWrongMessage.getAppID());
        User fromUser = userService.findById(from, callWrongMessage.getAppID());
        ChannelInfo channelInfo = onlineStatusService.getChannelInfo(to,
                (short) callWrongMessage.getAppID(), userService.isSystemAccount(toUser));
        if (channelInfo == null) {
            CallPacket callPacket = new CallPacket(); //Trick
            callPacket.setRoomId(callWrongMessage.getRoomID());
            callPacket.setCallStatus(CallStatus.getInstance(callWrongMessage.getCallStatus()));
            callPacket.setPeerName(to);
            PacketHead packetHead = new PacketHead();
            packetHead.setAppId((short) callWrongMessage.getAppID());
            packetHead.setSecret((byte) callWrongMessage.getScrect());
            callPacket.setPacketHead(packetHead);
            callService.userOfflineHandle(fromUser, toUser, callPacket,
                    (short) callWrongMessage.getAppID(), callWrongMessage.getTraceId());
        }
    }

    private void handle(byte[] packetBody, SimpleWrongMessage simpleWrongMessage) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("[WrongServerMessageHandler] handle simpleWrongMessage, messageId:{}, fromUserId:{}, toUserId:{}",
                    simpleWrongMessage.getMessageId(), simpleWrongMessage.getFromUserId(), simpleWrongMessage.getToUserId());
        }
        String toUserId = simpleWrongMessage.getToUserId();
        long messageId = simpleWrongMessage.getMessageId();
        Message message = messageService.getMessage(toUserId, messageId);
        if (message != null) {
            User toUser = userService.findById(toUserId, simpleWrongMessage.getAppId());
            Channel toChannel = channelService.findChannel(toUser);
            int wrongCount = simpleWrongMessage.getWrongCount();
            if (ChannelUtils.isOffline(toChannel) || wrongCount > 3) {
                messageService.updateMessage(toUserId, messageId, MessageStatus.UNDEAL); //更新为 UNDEAL
                handleOfflineMessage(simpleWrongMessage, message, toUser); //发送离线消息
            } else {
                if (wrongCount <= 3) { // ++
                    simpleWrongMessage.setWrongCount(wrongCount + 1);
                    byte[] trackBackInfoContent = simpleWrongMessage.encode();
                    if (toChannel instanceof AbstractChannelImpl) {
                        ((AbstractChannelImpl) toChannel).setTraceId(simpleWrongMessage.getTraceId());
                    }
                    toChannel.write(packetBody, trackBackInfoContent, ChannelAction.SEND);
                } else {
                    logger.warn("write too many times.");
                }
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("[WrongServerMessageHandler] handle simpleWrongMessage, messageId:{} content is empty !",
                        simpleWrongMessage.getMessageId());
            }
        }
    }

    private void handleOfflineMessage(SimpleWrongMessage simpleWrongMessage, Message message, User toUser) {
        int appId = simpleWrongMessage.getAppId();
        String to = message.getReceiver();
        UserToken userToken = userService.getTokenInfo(to, appId);
        if (userToken == null || userToken.getDeviceType() > 2) {
            if (logger.isInfoEnabled()) {
                logger.info("user {} have no push token or un supported device type", to);
            }
            return;
        }
        String from = message.getSender();
        User fromUser = userService.findById(from, appId);
        //获得用户昵称，优先获取手机通讯录的名字
        String nickName = userService.getFriendNickname(fromUser, toUser.getId(), fromUser.getAppId());
        messageService.pushMessage(simpleWrongMessage, message, fromUser, toUser, userToken, nickName);
    }


    private WrongMessage decodeWrongMessage(ByteBuf buf) throws IOException {
        int type = buf.readInt();
        int count = buf.readInt();
        int contentLength = buf.readInt();

        WrongMessage wrongMessage = null;
        switch (type) {
            case WrongMessage.SIMPLE_WRONG_MESSAGE_TYPE:
                wrongMessage = new SimpleWrongMessage();
                break;
            case WrongMessage.CALL_WRONG_MESSAGE_TYPE:
                wrongMessage = new CallWrongMessage(wrongMessage);
                break;
        }

        wrongMessage.setContentLength(contentLength);
        wrongMessage.setWrongMessageType(type);
        wrongMessage.setWrongCount(count);

        wrongMessage.decode(buf);

        return wrongMessage;
    }

}
