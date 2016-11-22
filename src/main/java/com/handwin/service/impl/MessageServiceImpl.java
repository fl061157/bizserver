package com.handwin.service.impl;

import com.alibaba.fastjson.JSON;
import com.handwin.bean.RichMessageInfo;
import com.handwin.entity.*;
import com.handwin.entity.wrong.SimpleWrongMessage;
import com.handwin.exception.ServerException;
import com.handwin.localentity.Message;
import com.handwin.localentity.MessageType;
import com.handwin.localentity.UserLocalMsgCounter;
import com.handwin.message.bean.MessageStatus;
import com.handwin.packet.*;
import com.handwin.persist.MessagePersist;
import com.handwin.persist.StatusStore;
import com.handwin.protocal.v5.codec.V5GenericPacket;
import com.handwin.protocal.v5.codec.V5PacketHead;
import com.handwin.server.PushMessageSender;
import com.handwin.service.*;
import com.handwin.utils.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * @author fangliang
 */
@Service
public class MessageServiceImpl implements MessageService {

    private static final Logger logger = LoggerFactory.getLogger(MessageServiceImpl.class);

    public static final String USER_SENT_MSG_PREFIX = "user_send_ms_";

    @Autowired
    @Qualifier("mysqlMessagePersist")
    private MessagePersist messagePersist;

    @Autowired
    private Snowflake snowflake;

    @Autowired
    private PushMessageSender pushMessageSender;

    @Autowired
    @Qualifier("messageSource")
    private ReloadableResourceBundleMessageSource messageSource;

    @Autowired
    @Qualifier("messageSource1")
    private ReloadableResourceBundleMessageSource messageSource1;


    @Autowired
    @Qualifier(value = "statusClusterStoreImpl")
    private StatusStore statusStore;


    @Value("${offline.push.message.length}")
    private int pushMesageLength;

    @Autowired
    private UserService userService;

    @Autowired
    private DelayTaskService delayTaskService;

    @Autowired
    private UserCallSmSendService userCallSmSendService;

    @Autowired
    private MessageUtils messageUtils;

    @Autowired
    private GroupService groupService;

    @Autowired
    private ConversationService conversationService;

    private final static Charset UTF_8 = Charset.forName("UTF-8");


    @Override
    public Long newMessageUID() {
        return snowflake.next();
    }

    @Override
    public Message getMessage(String userId, long messageId) {
        if (logger.isDebugEnabled()) {
            logger.debug("[MessageService] getMessage userId:{} , messageId:{}", userId, messageId);
        }
        try {
            return messagePersist.getMessage(userId, messageId);
        } catch (Exception e) {
            logger.error(String.format("[MessageService] getMessage Error userId:%s , messageId:%s ",
                    userId, messageId), e);
            throw e;
        }
    }

    @Override
    public Message createMessage(String sender, String receiver, Message message, MessageStatus messageStatus,
                                 byte[] content) throws ServerException {
        if (message.getId() == null || 0l == message.getId()) {
            message.setId(snowflake.next());
        }

        if (logger.isInfoEnabled()) {
            logger.info("[MessageService] createMessage sender:{} , receiver:{} , messageId:{}",
                    sender, receiver, message.getId());
        }

        try {
            return messagePersist.createMessage(sender, receiver, message, messageStatus, content);
        } catch (Exception e) {
            logger.error(String.format("[MessageService] createMessage sender:%s , receiver:%s , messageId:%s ",
                    sender, receiver, message.getId()), e);
            //throw e;
        }
        return message;
    }


    @Override
    public void updateMessage(String userID, long messageID, MessageStatus messageStatus) throws ServerException {
        messagePersist.updateMessageStatus(userID, messageID, messageStatus);
    }


    @Override
    public Message createCallMessage(String fromUserID, CallPacket callPacket, MessageStatus messageStatus,
                                     CallStatus callStatus, Long createTime) {
        Message message = new Message();
        message.setContent("");
        if (callPacket instanceof GameCallPacket) {
            GameCallPacket gPacket = (GameCallPacket) callPacket;
            List<String> gameIDList = gPacket.getGameIds();
            if (CollectionUtils.isNotEmpty(gameIDList)) {
                Map<String, String> m = new HashedMap();
                m.put("game_id", gameIDList.get(0));
                String c = JSON.toJSONString(m);
                message.setContent(c);
            }
        }

        boolean isGame = false;

        if (callPacket.getUserData() != null && callPacket.getUserData().length > 0) {
            String userData = new String(callPacket.getUserData());
            logger.info("[CreateMessage] UserData:{}", userData);
            Map<String, Object> m = JSON.parseObject(userData, Map.class);
            if (MapUtils.isNotEmpty(m)) {
                if (m.get("game_id") != null) {
                    isGame = true;
                }
                message.setContent(userData);
            }
        }


        message.setConversationId(fromUserID);
        message.setCreateTime(null == createTime ? System.currentTimeMillis() : createTime);
        message.setReceiverType(SystemConstant.TO_SEND_MESSAGE_RECEIVE_TYPE_PERSON);
        message.setRoomId(callPacket.getRoomId());
        message.setId(snowflake.next());
        message.setReceiver(callPacket.getPeerName());
        PacketHead packetHead = callPacket.getPacketHead();
        if (packetHead != null) {
            message.setSecret((packetHead.getSecret() == SystemConstant.PACKET_SECRET) ? 1 : 0);
        } else {
            message.setSecret(0);
        }
        message.setSender(fromUserID);
        switch (callStatus) {
            case VIDEO_REQUEST: {
                if (callPacket instanceof GameCallPacket) {
                    message.setType(String.valueOf(MessageType.GAME_VIDEO_CALL));
                } else {
                    if (isGame) {
                        message.setType(String.valueOf(MessageType.GAME_VIDEO_CALL));
                    } else {
                        message.setType(String.valueOf(MessageType.VIDEO_CALL));
                    }
                }
                break;
            }
            case AUDIO_REQUEST: {
                if (callPacket instanceof GameCallPacket) {
                    message.setType(String.valueOf(MessageType.GAME_AUDIO_CALL));
                } else {
                    if (isGame) {
                        message.setType(String.valueOf(MessageType.GAME_AUDIO_CALL));
                    } else {
                        message.setType(String.valueOf(MessageType.AUDIO_CALL));
                    }
                }
                break;
            }
            default: {
                throw new IllegalArgumentException("callStatus:" + callStatus + " not support.");
            }
        }
        try {
            message.setIsCount(0);
            Message result = createMessage(fromUserID, callPacket.getPeerName(), message, messageStatus,
                    message.getContent().toString().getBytes(SystemConstant.CHARSET_UTF8));
            try {
                delayTaskService.submitDelayTask(2 * 60 * 1000, () -> {
                    try {
                        sendPushCallReissueTip(message, callPacket.getPacketHead().getAppId());
                    } catch (Throwable e) {
                        logger.error("fails to execute push call service.", e);
                    }
                });
            } catch (Exception e) {
                logger.error("fails to submit delay task", e);
            }
            return result;
        } finally {
            if (logger.isInfoEnabled()) {
                logger.info("save missed call msg.roomId:{}", callPacket.getRoomId());
            }
        }

    }


    @Override
    public boolean removeMessage(String userId, Long messageId) {
        if (logger.isDebugEnabled()) {
            logger.debug("[MessageService] removeMessage userId:{} , messageId:{} ", userId, messageId);
        }
        try {
            return messagePersist.removeMessage(userId, messageId);
        } catch (Exception e) {
            logger.error(String.format("[MessageService] removeMessage userId:%s , messageId:%s", userId, messageId), e);
            throw e;
        }
    }

    @Override
    public UserLocalMsgCounter updateUnreadLocalCount(String userId,
                                                      boolean isAddOne) {
        if (logger.isDebugEnabled()) {
            logger.debug("[MessageService] updateUnreadLocalCount userId:{} , delta:{} , operate:{} ", userId, isAddOne);
        }
        try {
            return messagePersist.updateUnreadLocalCount(userId, isAddOne);
        } catch (Exception e) {
            logger.error("[MessageService] updateUnreadLocalCount error ", e);
            throw e;
        }
    }


    @Override
    public void pushText(final SystemNotifyPacket notifyPackage, User toUser,
                         UserToken device, final String traceId) {
        OfflinePushPackageBean packet = new OfflinePushPackageBean();
        if (null != notifyPackage.getPushContentTemplate()) {
            String pushContentForTemplate = messageUtils.getRichMessageForPush(notifyPackage.getPushContentTemplate(), toUser.getId(), toUser.getAppId());
            if (StringUtils.isNotBlank(pushContentForTemplate)) {
                packet.setContent(pushContentForTemplate);
                logger.info("[rich message]richPush:{} pushContent:{}.toUser:{}", notifyPackage.getPushContentTemplate(), pushContentForTemplate, toUser);
            } else {
                packet.setContent(notifyPackage.getPushContentBody());
            }
        } else {
            packet.setContent(notifyPackage.getPushContentBody());
        }
        packet.setFromUserId(notifyPackage.getFrom());
        packet.setSendType(OfflinePushPackageBean.SENDTYPE_PERSON);

        Map<String, Object> extra = notifyPackage.getExtra();
        if (null != extra && SystemConstant.SYSTEM_NOTIFY_EXTRAT_TYPE_GROUP.equals(extra.get(SystemConstant.SYSTEM_NOTIFY_EXTRA_KEY_TYPE))) {
            String groupId = (String) extra.get(SystemConstant.SYSTEM_NOTIFY_EXTRA_KEY_GROUP_ID);
            packet.setFromUserId(groupId);
            packet.setSendType(OfflinePushPackageBean.SENDTYPE_GROUP);
            Object enterConversation = extra.get(SystemConstant.SYSTEM_NOTIFY_EXTRA_PUSH_ENTER_CONVERSATION);
            if (null != enterConversation && (!(Boolean) enterConversation)) {
                packet.setTipType("1");
            }
        }
        packet.setToUserId(notifyPackage.getTo());

        if (notifyPackage.getPacketHead() == null || (notifyPackage.getPacketHead().getAppId() == 0)) {
            packet.setAppId(toUser.getAppId());
        } else {
            packet.setAppId(notifyPackage.getPacketHead().getAppId());
        }

        packet.setUnreadInc(notifyPackage.isUnreadInc());
        packet.addAttr("msgType", String.valueOf(notifyPackage.getMsgType()));
        Map<String, Object> extraData = notifyPackage.getExtra();
        if (null != extra && extraData.size() > 0) {
            Object notifyType = extraData.get("notifyType");
            if (null != notifyType) {
                packet.addAttr("notifyType", String.valueOf(notifyType));
            }
        }
        pushText(device, toUser, traceId, packet);
    }


    @Override
    public void pushText(SystemNotifyPacket notifyPackage, User toUser, UserToken device, String traceId, boolean noDisturb) {
        OfflinePushPackageBean packet = new OfflinePushPackageBean();
        packet.setContent(notifyPackage.getPushContentBody());
        packet.setFromUserId(notifyPackage.getFrom());
        packet.setSendType(OfflinePushPackageBean.SENDTYPE_PERSON);

        Map<String, Object> extra = notifyPackage.getExtra();
        if (null != extra && SystemConstant.SYSTEM_NOTIFY_EXTRAT_TYPE_GROUP.equals(extra.get(SystemConstant.SYSTEM_NOTIFY_EXTRA_KEY_TYPE))) {
            String groupId = (String) extra.get(SystemConstant.SYSTEM_NOTIFY_EXTRA_KEY_GROUP_ID);
            packet.setFromUserId(groupId);
            packet.setSendType(OfflinePushPackageBean.SENDTYPE_GROUP);
            Object enterConversation = extra.get(SystemConstant.SYSTEM_NOTIFY_EXTRA_PUSH_ENTER_CONVERSATION);
            if (null != enterConversation && (!(Boolean) enterConversation)) {
                packet.setTipType("1");
            }
        }
        packet.setToUserId(notifyPackage.getTo());
        packet.setAppId(notifyPackage.getPacketHead() != null ? notifyPackage.getPacketHead().getAppId() : 0);
        packet.setUnreadInc(notifyPackage.isUnreadInc());
        packet.addAttr("msgType", String.valueOf(notifyPackage.getMsgType()));

//        if( noDisturb ) {
//            packet.setNoticeType(PushMsgMqBean.NoticeType.NO_ALERT );
//        }

        pushText(device, toUser, traceId, packet);
    }

    @Override
    public void pushText(V5PacketHead packetHead, V5GenericPacket genericPacket, User toUser, UserToken device, String traceId) {
        OfflinePushPackageBean packet = new OfflinePushPackageBean();
        packet.setContent(packetHead.getPushContent());
        packet.setFromUserId(packetHead.getFrom());
        packet.setToUserId(packetHead.getTo());
        packet.setAppId(packetHead.getAppId());
        packet.setSendType(OfflinePushPackageBean.SENDTYPE_PERSON);
        packet.setUnreadInc(packetHead.getPushCount() > 0);
        pushText(device, toUser, traceId, packet);
    }

    @Override
    public void pushText(User fromUser, User toUser, UserToken toUserToken,
                         String nickName, SimpleMessagePacket packet,
                         String traceID) {

        byte[] msgContentForPush = packet.getContent();
        if (null != packet.getRichMessageInfo()) {
            String richPushContent = messageUtils.getRichMessageForPush(packet.getRichMessageInfo(), toUser.getId(), toUser.getAppId());
            if (StringUtils.isNotBlank(richPushContent)) {
                msgContentForPush = richPushContent.getBytes(StandardCharsets.UTF_8);
            }
        } else {

            if (UserUtils.isThiradApp(packet.getPacketHead().getAppId())) {
                if (ArrayUtils.isNotEmpty(msgContentForPush)) {
                    String jc = new String(msgContentForPush, UTF_8);
                    try {
                        Map m = JSON.parseObject(jc, Map.class);
                        if (m != null && m.containsKey("text")) {
                            jc = (String) m.get("text");
                            msgContentForPush = jc.getBytes(UTF_8);
                        }
                    } catch (Exception e) {
                    }
                }
            }


        }
        String sendContent = getOfflinePushMessageContent(toUserToken, nickName,
                toUser.getLocale(), packet.getMessageType(), msgContentForPush,
                packet.getPacketHead().getSecret() != 0x00, packet.getPacketHead().getAppId());
        OfflinePushPackageBean packageBean = new OfflinePushPackageBean();
        packageBean.setTraceId(traceID);
        packageBean.setContent(sendContent);
        packageBean.setToUserId(toUser.getId());
        packageBean.setFromUserId(null != packet.getFromGroup() ? packet.getFromGroup() : fromUser.getId());
        packageBean.setAppId(fromUser.getAppId());
        packageBean.setSendType(null != packet.getFromGroup() ? OfflinePushPackageBean.SENDTYPE_GROUP : OfflinePushPackageBean.SENDTYPE_PERSON);
        packageBean.setFromNickName(fromUser.getNickname());
        packageBean.setFromMobile(fromUser.getMobile());
        packageBean.setFromAvatarUrl(fromUser.getAvatar_url());
        packageBean.setFromCountryCode(fromUser.getCountrycode());
        packageBean.setTime(String.valueOf(System.currentTimeMillis()));
        packageBean.setReplyType(true);
        pushText(toUserToken, toUser, traceID, packageBean);
        if (logger.isDebugEnabled()) {
            logger.debug("receiveNameMD5:{}. pushContent:{} offline push message.", toUser.getId(), packageBean);
        }
    }

    @Override
    public void pushText(User fromUser, User toUser, UserToken toUserToken, String text,
                         String traceID, PushMsgMqBean.NoticeType noticeType, String tipType, Boolean isMsgIncrementByOne, Long timeToLiveInMillsec) {

        OfflinePushPackageBean packageBean = new OfflinePushPackageBean();
        packageBean.setFromUserId(fromUser.getId());
        packageBean.setTraceId(traceID);
        packageBean.setContent(text);
        packageBean.setToUserId(toUser.getId());
        packageBean.setAppId(fromUser.getAppId());
        packageBean.setSendType(OfflinePushPackageBean.SENDTYPE_PERSON);
        packageBean.setFromNickName(fromUser.getNickname());
        packageBean.setFromMobile(fromUser.getMobile());
        packageBean.setFromAvatarUrl(fromUser.getAvatar_url());
        packageBean.setFromCountryCode(fromUser.getCountrycode());
        packageBean.setTime(String.valueOf(System.currentTimeMillis()));
        packageBean.setReplyType(false);
        packageBean.setNoticeType(noticeType);
        packageBean.setTipType(tipType);
        packageBean.setTimeToLiveInMillSec(timeToLiveInMillsec);
        pushTextAndMsgIncrementByOne(toUserToken, toUser, traceID, packageBean);
        if (logger.isDebugEnabled()) {
            logger.debug("receiveNameMD5:{}.offline push message. content:{}", toUser.getId(), text);
        }
    }


    @Override
    public void pushMessage(SimpleWrongMessage simpleWrongMessage,
                            Message message, User fromUser, User toUser, UserToken userToken, String nickName) {
        int appId = simpleWrongMessage.getAppId();
        String from = simpleWrongMessage.getFromUserId();
        String to = simpleWrongMessage.getToUserId();
        if (userToken == null || userToken.getDeviceType() > 2) {
            if (logger.isDebugEnabled()) {
                logger.debug("user {} have no push token or un supported device type", to);
            }
            return;
        }

        OfflinePushPackageBean packageBean = new OfflinePushPackageBean();
        packageBean.setTraceId(simpleWrongMessage.getTraceId());

        packageBean.setToUserId(to);
        packageBean.setAppId(appId);

        //需要对群组消息 免打扰进行处理
        String pushTextContent = null;
        RichMessageInfo richMessageInfo = null;
        Map<String, Object> messageMeta = message.getMeta();
        if (null != messageMeta) {
            Object format = messageMeta.get("entity_format");
            if (null != format) {
                //富媒体消息
                if (SystemConstant.MESSAGE_ENTITY_RICH_MEDIA.intValue() == (Integer) format) {
                    richMessageInfo = messageUtils.generateRichMessage((byte[]) message.getContent());
                    if (null != richMessageInfo) {
                        pushTextContent = messageUtils.getRichMessageForPush(richMessageInfo, toUser.getId(), toUser.getAppId());
                    }
                } else if (SystemConstant.MESSAGE_ENTITY_SYSTEM_NOTIRY.intValue() == (Integer) format) {
                    if (null != messageMeta.get("push_content")) {
                        pushTextContent = (String) messageMeta.get("push_content");
                    } else {
                        logger.info("system notify. no push. message:{}", message);
                        return;
                    }
                }
            }
            Object enterConversation = messageMeta.get(SystemConstant.SYSTEM_NOTIFY_EXTRA_PUSH_ENTER_CONVERSATION);
            //ios push点击会话栏是否进入会话界面
            if (null != enterConversation && (!(Boolean) enterConversation)) {
                packageBean.setTipType("1");
            }
        }

        if (null == pushTextContent) {
            pushTextContent = new String((byte[]) message.getContent(), StandardCharsets.UTF_8);
        }

        //群组
        if (SystemConstant.TO_SEND_MESSAGE_RECEIVE_TYPE_GROUP == message.getReceiverType()) {
            packageBean.setSendType(OfflinePushPackageBean.SENDTYPE_GROUP);
            //群组Id
            String groupId = message.getConversationId();
            Group group = groupService.findGroupInfo(groupId);
            if (null != groupId) {
                nickName = String.format("%s(%s)", fromUser.getNickname(), group.getName() != null ? group.getName() : "");
            }
            packageBean.setFromUserId(groupId);

            //群组免打扰判断
            boolean canSendPush = false;
            //处于免打扰
            if (conversationService.isNoDisturbForGroup(toUser.getId(), groupId, appId)) {
                //如果是@消息则可以发送push消息
                //内容为富媒体格式
                if (null != richMessageInfo) {
                    String atUsers = richMessageInfo.getAtUsers();
                    //@该人 免打扰不起作用
                    if (StringUtils.isNotBlank(atUsers) && atUsers.contains(toUser.getId())) {
                        canSendPush = true;
                    }
                }
                logger.debug("[group message] push. groupId:{} ,toUserId:{}, canSendPush:{} rich media.", groupId, toUser.getId(), canSendPush);
            } else {
                canSendPush = true;
            }
            if (!canSendPush) {
                logger.debug("[group message] push. groupId:{} ,toUserId:{}, canSendPush:{} rich media. can not send push.", groupId, toUser.getId(), canSendPush);
                return;
            }
        } else {
            packageBean.setSendType(OfflinePushPackageBean.SENDTYPE_PERSON);
            packageBean.setFromUserId(from);
        }


        String sendContent = null;
        try {
            sendContent = getOfflinePushMessageContent(userToken, nickName,
                    toUser.getLocale(), (byte) MessageType.getMessageType(message.getType()).getValue(),
                    pushTextContent.getBytes(StandardCharsets.UTF_8), message.getSecret() != 0, appId);
        } catch (Exception e) {
            logger.error("[WrongServerMessageHandler] handleOfflineMessage ", e);
        }
        packageBean.setContent(sendContent);
        packageBean.setFromNickName(fromUser.getNickname());
        packageBean.setFromMobile(fromUser.getMobile());
        packageBean.setFromAvatarUrl(fromUser.getAvatar_url());
        packageBean.setFromCountryCode(fromUser.getCountrycode());
        packageBean.setTime(String.valueOf(System.currentTimeMillis()));
        pushText(userToken, toUser, simpleWrongMessage.getTraceId(), packageBean);
        if (logger.isDebugEnabled()) {
            logger.debug("receiveNameMD5:{}.offline push message.", to);
        }
    }


    private void pushText(UserToken device, User toUser, String traceId, OfflinePushPackageBean packet) {
        PushTextBean pushTextBean = formPushTextBean(device, traceId, packet);

        if ((packet.getAppId() == 0) && packet.getAppId() != toUser.getAppId()) {
            packet.setAppId(toUser.getAppId());
        }
        pushText(device, toUser, pushTextBean, false);
    }

    private void pushTextAndMsgIncrementByOne(UserToken device, User toUser, String traceId, OfflinePushPackageBean packet) {
        PushTextBean pushTextBean = formPushTextBean(device, traceId, packet);

        if ((pushTextBean.getAppId() == 0) && pushTextBean.getAppId() != toUser.getAppId()) {
            pushTextBean.setAppId(toUser.getAppId());
        }
        pushText(device, toUser, pushTextBean, true);
    }


    private void pushText(UserToken device, User toUser, PushTextBean pushTextBean, Boolean isMsgIncrementByOne) {

        pushTextBean.setFrom(UserUtils.outThirdUserID(pushTextBean.getFrom(), pushTextBean.getAppId()));
        pushTextBean.setTo(UserUtils.outThirdUserID(pushTextBean.getTo(), pushTextBean.getAppId()));


        if (device.getDeviceType() == 1) { //IOS
            Integer unReadCount = 0;
            try {
                unReadCount = updateUnreadLocalCount(toUser.getId(), pushTextBean.isIncreament()).getCounter().get().intValue();
                if (null != isMsgIncrementByOne && isMsgIncrementByOne) {
                    unReadCount++;
                }
            } catch (Exception e) {
                logger.error("[CallService sendCallPush]: getUserUnReadCount Error userId:{} ", toUser.getId(), e);

            }
            pushTextBean.setMsgNumber(Optional.ofNullable(unReadCount).orElse(0));
            pushMessageSender.write(toUser.getCountrycode(), pushTextBean);
        } else if (device.getDeviceType() == 2) {// android
            pushMessageSender.write(toUser.getCountrycode(), pushTextBean);
        }
    }


    private PushCallBean formatPushCallBean(User fromUser, User toUser, UserToken toUserToken,
                                            CallPacket callPacket, Integer unReadCount, final String traceId, String nickName) {
        PushCallBean pushCallBean;
        if (callPacket instanceof GameCallPacket) {
            pushCallBean = new PushGameCallBean();
            pushCallBean.setExtraData(((GameCallPacket) callPacket).getExtraData());
            ((PushGameCallBean) pushCallBean).setMediaType(((GameCallPacket) callPacket).getMediaType());
            ((PushGameCallBean) pushCallBean).setGameIds(((GameCallPacket) callPacket).getGameIds());
        } else {
            pushCallBean = new PushCallBean();
            pushCallBean.setExtraData(callPacket.getUserData());
        }
        pushCallBean.setFrom(fromUser.getId());
        pushCallBean.setTo(callPacket.getPeerName());
        byte msgCmd = MsgCmd.MSG_GAME_PUSH;
        switch (callPacket.getCallStatus()) {
            case VIDEO_REQUEST:
                pushCallBean.setCallType(CallType.VIDEO);
                if (!(pushCallBean instanceof PushGameCallBean)) {
                    msgCmd = MsgCmd.MSG_VIDEO_PUSH;
                } else {
                    msgCmd = MsgCmd.MSG_GAME_VIDEO_PUSH;
                }
                break;
            case AUDIO_REQUEST:
                pushCallBean.setCallType(CallType.AUDIO);
                if (!(pushCallBean instanceof PushGameCallBean)) {
                    msgCmd = MsgCmd.MSG_AUDIO_PUSH;
                } else {
                    msgCmd = MsgCmd.MSG_GAME_AUDIO_PUSH;
                }
                break;
            default:
                break;
        }
        pushCallBean.setContent(getOfflinePushMessageContent(toUserToken,
                nickName, toUser.getLocale(), msgCmd,
                callPacket.getContent().getBytes(SystemConstant.CHARSET_UTF8), false, callPacket.getPacketHead().getAppId()));
        pushCallBean.setTraceId(traceId);
        pushCallBean.setAppId(callPacket.getPacketHead().getAppId());
        pushCallBean.setProvider(toUserToken.getProvider());
        pushCallBean.setDeviceToken(toUserToken.getToken());
        pushCallBean.setFromAvatarUrl(fromUser.getAvatar_url());
        pushCallBean.setFromNickName(fromUser.getNickname());
        pushCallBean.setFromMobile(fromUser.getMobile());
        pushCallBean.setFromCountryCode(fromUser.getCountrycode());
        pushCallBean.setTime(String.valueOf(SystemUtils.getGreenwichTimestamp()));
        if (toUserToken.getDeviceType() == SystemConstant.DEVICE_TYPE_IOS) {
            pushCallBean.setDeviceType(DeviceType.IOS);
            pushCallBean.setMsgNumber(unReadCount != null ? unReadCount : 0);
        } else {
            pushCallBean.setDeviceType(DeviceType.ANDRIOD);
        }

        pushCallBean.setRoomId(callPacket.getRoomId());
        return pushCallBean;
    }


    @Override
    public void pushCall(User fromUser, User toUser, UserToken toUserToken,
                         CallPacket callPacket, Integer unReadCount, String traceId, String nickName) {
        PushCallBean pushCallBean = formatPushCallBean(fromUser, toUser, toUserToken,
                callPacket, unReadCount, traceId, nickName);

        if (pushCallBean.getAppId() == 0 && (pushCallBean.getAppId() != toUser.getAppId())) {
            pushCallBean.setAppId(toUser.getAppId());
        }

        pushCallBean.setFrom(UserUtils.outThirdUserID(pushCallBean.getFrom(), pushCallBean.getAppId()));
        pushCallBean.setTo(UserUtils.outThirdUserID(pushCallBean.getTo(), pushCallBean.getAppId()));

        pushMessageSender.write(toUser.getCountrycode(), pushCallBean);
    }


    private String getOfflinePushMessageContent(UserToken userToken, String userName, Locale locale,
                                                byte msgCmd, byte[] content, boolean isSecret, Integer appID) {
        //给用户发送推送消息
        String sendContent;
        if (userName == null) {
            userName = "";
        }

        if (SystemConstant.DEVICE_TYPE_ANDRIOD == userToken.getDeviceType()) {
            sendContent = getAndroidPushContent(userName, locale, msgCmd, content, isSecret, appID);
        } else if (SystemConstant.DEVICE_TYPE_IOS == userToken.getDeviceType()) {
            sendContent = getIOSPushContent(userName, locale, msgCmd, content, isSecret, appID);
        } else {
            sendContent = "";
            logger.warn("DeviceType:{} not support.", userToken.getDeviceType());
        }

        //如果是小米的推送，还需要添加昵称
        if (SystemConstant.DEVICE_TOKEN_PROVIDE_XIAOMI.equals(userToken.getProvider())) {
            sendContent = String.format("%s %s", userName, sendContent);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("userName:{},msgCmd:{},content:{},result:{}.locale:{}",
                    userName, msgCmd, content, sendContent, locale);
        }
        return sendContent;
    }


    @Override
    public Long isServerReceived(String cmsgId, byte msgflag, String userId) {
        if (SystemConstant.MSGFLAG_RESENT != msgflag || StringUtils.isBlank(cmsgId)) {
            return null;
        }
        final String key = String.format("%s_%s_%s", USER_SENT_MSG_PREFIX, userId, cmsgId);
        try {
            String r = statusStore.get(key);
            if (r != null) {
                try {
                    return Long.parseLong(r);
                } catch (Exception e) {
                    logger.error("Parses error r :{}", r, e);
                }
            }
        } catch (Exception e) {
            logger.error(String.format("isServerReceived cmsgID:%s", cmsgId), e);
        }
        return null;
    }


    @Override
    public void addServerReceivedMessage(String cmsgId, Long messageId, final int ttl, String userId) {
        if (StringUtils.isBlank(cmsgId)) {
            return;
        }
        final String key = String.format("%s_%s_%s", USER_SENT_MSG_PREFIX, userId, cmsgId);
        try {
            statusStore.setEx(key, ttl, String.valueOf(messageId));
        } catch (Exception e) {
            logger.error(String.format("addServerReceiverdMessage cmsgID:%s error", cmsgId), e);
        }

    }

    /**
     * 呼叫push 补送 消息通知
     * 使用场景:
     * A 呼叫B时, B无网络,B过了一段时间后，连接上网络，没有push call的提示
     * 给B补发一条missed push call 的普通消息后，B连接上网络则能够接受到推送中心的missed call 提示
     *
     * @param message
     */
    @Override
    public void sendPushCallReissueTip(Message message, Integer appId) {
        logger.debug("[send call push tip] begin to send push . message:{}", message);
        if (null == message) return;

        String to = message.getReceiver();
        String from = message.getSender();
        Long messageId = message.getId();
        Message storedMessage = getMessage(to, messageId);

        //如果数据库中没有该消息，表示用户已经在线，拉取到了该missed call,不需要再补发提示
        if (null == storedMessage) {
            logger.debug("message:{} pushed by user. no push.", message);
            return;
        }

        User toUser = userService.findById(to, appId);
        User fromUser = userService.findById(from, appId);
        if (null == toUser || null == fromUser) return;

        //放开限制 所有的用户均发送
//        UserToken userToken = userService.getTokenInfo(to, toUser.getAppId());
//        if (null == userToken || StringUtils.isBlank(userToken.getToken())) {
//            logger.debug("[call push] userId:{} no token. no push. message:{}", to, message);
//            return;
//        }
//
//        //IOS的情况下发送短信
//        if (DeviceType.IOS.getValue() != userToken.getDeviceType()) {
//            logger.debug("[call push]. userId:{} token no ios. no push.", to);
//            return;
//        }

        //用户离线，再进行push的推送，客户端在有网络的情况下，可能在后台已经连接上了网络，所以不能以用户通道作为该补发消息推送的依据
//        Channel toChannel = channelService.findChannel(toUser);
        storedMessage = getMessage(to, messageId);

        //如果数据库中没有该消息，表示用户已经在线，拉取到了该missed call,不需要再补发提示
        if (null != storedMessage) {
            userCallSmSendService.sendCallSm(toUser, fromUser, "missed");
//            pushMessageSender.write(toUser.getCountrycode(), pushTextBean);
        } else {
            logger.debug("message:{} . has been pulled . no push.", message);
        }

        logger.debug("[send call push tip] success to send push . message:{}", message);
    }

    @Override
    public Integer getMessageEntityType(SimpleMessagePacket simpleMessagePacket) {
        if (SystemConstant.MESSAGE_TYPE_RICH_MEDIA == (SystemConstant.MESSAGE_TYPE_RICH_MEDIA & simpleMessagePacket.getEntityType())) {
            return SystemConstant.MESSAGE_ENTITY_RICH_MEDIA;
        } else {
            return SystemConstant.MESSAGE_ENTITY_PLAIN;
        }
    }


    @Override
    public void pushText(User fromUser, User toUser, UserToken toUserToken,
                         String nickName, CommonMsgPackage packet,
                         String traceID) {
        //根据不同的app（第三方还是CG，进行不同的推送）
        String sendContent;
        if (fromUser.getAppId() < SystemConstant.CG_MAX_APP_ID) {
            sendContent = getOfflinePushMessageContent(toUserToken, nickName,
                    toUser.getLocale(), packet.getMessageType(), packet.getContent(),
                    packet.getSecret() != 0x00, toUser.getAppId());
        } else {
            sendContent = getPushTipFromThirdApp(fromUser.getAppId(), fromUser, packet.getGenericPacket());
        }

        OfflinePushPackageBean packageBean = new OfflinePushPackageBean();
        packageBean.setTraceId(traceID);
        packageBean.setContent(sendContent);
        packageBean.setToUserId(packet.getToUser());
        packageBean.setFromUserId(null != packet.getFromGroup() ? packet.getFromGroup() : fromUser.getId());
        packageBean.setAppId(fromUser.getAppId());
        packageBean.setSendType(null != packet.getFromGroup() ? OfflinePushPackageBean.SENDTYPE_GROUP : OfflinePushPackageBean.SENDTYPE_PERSON);
        packageBean.setFromNickName(fromUser.getNickname());
        packageBean.setFromMobile(fromUser.getMobile());
        packageBean.setFromAvatarUrl(fromUser.getAvatar_url());
        packageBean.setFromCountryCode(fromUser.getCountrycode());
        packageBean.setTime(String.valueOf(System.currentTimeMillis()));
        pushText(toUserToken, toUser, traceID, packageBean);
        if (logger.isDebugEnabled()) {
            logger.debug("receiveNameMD5:{}.offline push message.", toUser.getId());
        }
    }


    private PushTextBean formPushTextBean(UserToken device, String traceId, OfflinePushPackageBean packet) {
        final PushTextBean bean = new PushTextBean();
        //给用户发送推送消息
        bean.setFrom(packet.getFromUserId());
        bean.setTo(packet.getToUserId());
        bean.setContent(packet.getContent());
        bean.setSendType(packet.getSendType());
        bean.setTraceId(traceId);
        bean.setAppId(packet.getAppId());
        bean.setProvider(device.getProvider());

        if (device.getDeviceType() == 1) {
            bean.setDeviceType(DeviceType.IOS);
            bean.setReplyType(packet.isReplyType());
        } else if (device.getDeviceType() == 2) {// android
            bean.setDeviceType(DeviceType.ANDRIOD);
        } else {
            //unknown device type
            logger.warn("Unknown device type {}.packet:{}", device.getDeviceType(), packet);
        }
        bean.setDeviceToken(device.getToken());
        bean.setFromCountryCode(packet.getFromCountryCode());
        bean.setFromAvatarUrl(packet.getFromAvatarUrl());
        bean.setFromMobile(packet.getFromMobile());
        bean.setFromNickName(packet.getFromNickName());
        bean.setTime(packet.getTime());
        bean.setIncreament(packet.isUnreadInc());
        bean.getAttrs().putAll(packet.getAttrs());

        if (null != packet.getNoticeType()) {
            bean.setNoticeType(packet.getNoticeType());
        }

        if (StringUtils.isNotBlank(packet.getTipType())) {
            bean.setTipType(packet.getTipType());
        }

        if (null != packet.getMetaMap() && packet.getMetaMap().size() > 0) {
            bean.getMetaMap().putAll(packet.getMetaMap());
        }
        return bean;
    }


    private String getIOSPushContent(String userName, Locale locale,
                                     byte msgCmd, byte[] content, boolean isSecret, Integer appID) {


        ReloadableResourceBundleMessageSource mSource = getMessageSource(appID);
        String template;
        String sendContent = "";
        switch (msgCmd) {
            case MsgCmd.MSG_TEXT: {
                if (isSecret) {
                    template = SystemConstant.IOS_PUSH_TEXT_SECRET_TEMPLATE.trim();
                    sendContent = mSource.getMessage(template, new String[]{userName}, locale);
                } else {
                    template = SystemConstant.IOS_PUSH_TEXT_TEMPLATE.trim();
                    String strContent = new String(content, Charset.forName("utf-8"));
                    sendContent = mSource.getMessage(template, new String[]{userName, strContent}, locale);
                    //超过一定的字符就要截断
                    if (sendContent.length() > pushMesageLength) {
                        sendContent = sendContent.substring(0, pushMesageLength) + "...";
                    }
                }
                break;
            }

            case MsgCmd.MSG_IMGURL: {
                template = SystemConstant.IOS_PUSH_PIC_TEMPLATE.trim();
                sendContent = mSource.getMessage(template, new String[]{userName}, locale);
                break;
            }

            case MsgCmd.MSG_NAMECARD: {
                template = SystemConstant.IOS_PUSH_NAMECARD_TEMPLATE.trim();
                sendContent = mSource.getMessage(template, new String[]{userName}, locale);
                break;
            }

            case MsgCmd.MSG_VOICE: {
                template = SystemConstant.IOS_PUSH_VOICE_TEMPLATE.trim();
                sendContent = mSource.getMessage(template, new String[]{userName}, locale);
                break;
            }
            case MsgCmd.MSG_VIDEO: {
                template = SystemConstant.IOS_PUSH_VIDEO_TEMPLATE.trim();
                sendContent = mSource.getMessage(template, new String[]{userName}, locale);
                break;
            }

            case MsgCmd.MSG_VIDEO_PUSH: {
                template = SystemConstant.IOS_PUSH_VIDEO_CALL.trim();
                sendContent = mSource.getMessage(template, new String[]{userName}, locale);
                break;
            }

            case MsgCmd.MSG_AUDIO_PUSH: {
                template = SystemConstant.IOS_PUSH_AUDIO_CALL.trim();
                sendContent = mSource.getMessage(template, new String[]{userName}, locale);
                break;
            }

            case MsgCmd.MSG_GAME_PUSH: {
                template = SystemConstant.IOS_PUSH_GAME_CALL.trim();
                sendContent = mSource.getMessage(template, new String[]{userName}, locale);
                break;
            }

            case MsgCmd.MSG_GAME_VIDEO_PUSH: {
                template = SystemConstant.IOS_PUSH_GAME_VIDEO_CALL.trim();
                sendContent = mSource.getMessage(template, new String[]{userName}, locale);
                break;
            }


            default: {
                logger.error("[push-content].userName:{},msgCmd:{}. msgCmd not support.", userName, msgCmd);
                break;
            }
        }
        return sendContent;
    }


    private String getAndroidPushContent(String userName, Locale locale,
                                         byte msgCmd, byte[] content, boolean isSecret, Integer appID) {


        ReloadableResourceBundleMessageSource mSource = getMessageSource(appID);
        String template;
        String sendContent = "".intern();
        switch (msgCmd) {
            case MsgCmd.MSG_TEXT: {
                if (isSecret) {
                    template = SystemConstant.ANDROID_PUSH_TEXT_SECRET_TEMPLATE.trim();
                    sendContent = mSource.getMessage(template, new String[]{userName}, locale);
                } else {
                    template = SystemConstant.ANDROID_PUSH_TEXT_TEMPLATE.trim();
                    String strContent = new String(content, Charset.forName("utf-8"));
                    sendContent = mSource.getMessage(template, new String[]{userName, strContent}, locale);
                }
                break;
            }
            case MsgCmd.MSG_IMGURL: {
                template = SystemConstant.ANDROID_PUSH_PIC_TEMPLATE.trim();
                sendContent = mSource.getMessage(template, new String[]{userName}, locale);
                break;
            }

            case MsgCmd.MSG_NAMECARD: {
                template = SystemConstant.ANDROID_PUSH_NAMECARD_TEMPLATE.trim();
                sendContent = mSource.getMessage(template, new String[]{userName}, locale);
                break;
            }

            case MsgCmd.MSG_VOICE: {
                template = SystemConstant.ANDROID_PUSH_VOICE_TEMPLATE.trim();
                sendContent = mSource.getMessage(template, new String[]{userName}, locale);
                break;
            }
            case MsgCmd.MSG_VIDEO: {
                template = SystemConstant.ANDROID_PUSH_VIDEO_TEMPLATE.trim();
                sendContent = mSource.getMessage(template, new String[]{userName}, locale);
                break;
            }

            case MsgCmd.MSG_VIDEO_PUSH: {
                template = SystemConstant.ANDROID_PUSH_VIDEO_CALL.trim();
                sendContent = mSource.getMessage(template, new String[]{userName}, locale);
                break;
            }

            case MsgCmd.MSG_AUDIO_PUSH: {
                template = SystemConstant.ANDROID_PUSH_AUDIO_CALL.trim();
                sendContent = mSource.getMessage(template, new String[]{userName}, locale);
                break;
            }

            case MsgCmd.MSG_GAME_AUDIO_PUSH: {
                template = SystemConstant.ANDROID_PUSH_AUDIO_CALL.trim();
                sendContent = mSource.getMessage(template, new String[]{userName}, locale);
                break;
            }

            case MsgCmd.MSG_GAME_VIDEO_PUSH: {
                template = SystemConstant.ANDROID_PUSH_GAME_VIDEO_CALL.trim();
                sendContent = mSource.getMessage(template, new String[]{userName}, locale);
                break;
            }

            default: {
                logger.error("[push-content].userName:{},msgCmd:{}. msgCmd not support.",
                        userName, msgCmd);
                break;
            }
        }
        return sendContent;
    }


    public interface MsgCmd {
        byte MSG_TEXT = 1;
        byte MSG_NAMECARD = 4;
        byte MSG_IMGURL = 3;
        byte MSG_VOICE = 5;
        byte MSG_VIDEO = 6;
        byte MSG_VIDEO_PUSH = -1;
        byte MSG_AUDIO_PUSH = -2;

        byte MSG_GAME_VIDEO_PUSH = -4;
        byte MSG_GAME_AUDIO_PUSH = -5;


        byte MSG_GAME_PUSH = -3;
        byte MSG_SRV_SINGLE = 1;
        byte MSG_TEXT_GROUP = 2;
        byte MSG_IMG = 2;
        byte MSG_IMG_SINGLE = 1;
        byte MSG_IMG_BOTH_TRANSFER = 2;
        byte MSG_IMGURL_SINGLE = 1;
    }

    /**
     * 待完善，需要根据第三方app的配置获取不同的推送提示语
     *
     * @param appId
     * @param fromUser
     * @param genericPacket
     * @return
     */
    public String getPushTipFromThirdApp(Integer appId, User fromUser, GenericPacket genericPacket) {
        String pushContent = "";

        String headPushContent = genericPacket.getPacketHead().getPushContent();
        if (StringUtils.isNotBlank(headPushContent)) {
            pushContent = headPushContent;
        } else {
            String service = genericPacket.getPacketHead().getService();
            switch (service) {
                case V5ProtoConstant.SERVICE_SEND_SINGE_TEXT:
                case V5ProtoConstant.SERVICE_SEND_GROUP_TEXT:
                case V5ProtoConstant.SERVICE_SEND_CHATROOM_TEXT:
                    //获取消息的内容
                    Map bodyMap = genericPacket.getBodyMap();
                    if (null != bodyMap) {
                        try {
                            String content = (String) genericPacket.getBodyMap().get("content");
                            if (StringUtils.isNotBlank(content)) {
                                pushContent = String.format("%s:%s", fromUser.getNickname(), content);
                            }
                        } catch (Exception e) {
                            logger.error("fails to get v5 protoc msg content.", e);
                        }
                    }
                    break;
                case V5ProtoConstant.SERVICE_SEND_SINGE_IMG:
                case V5ProtoConstant.SERVICE_SEND_GROUP_IMG:
                case V5ProtoConstant.SERVICE_SEND_CHATROOM_IMG:
                    pushContent = String.format("%s %s", fromUser.getNickname(), "发送了一张图片");
                    break;
                case V5ProtoConstant.SERVICE_SEND_SINGE_AUDIO:
                case V5ProtoConstant.SERVICE_SEND_GROUP_AUDIO:
                case V5ProtoConstant.SERVICE_SEND_CHATROOM_AUDIO:
                    pushContent = String.format("%s %s", fromUser.getNickname(), "发送了一条语音消息");
                    break;
                case V5ProtoConstant.SERVICE_SEND_SINGE_VIDEO:
                case V5ProtoConstant.SERVICE_SEND_GROUP_VIDEO:
                case V5ProtoConstant.SERVICE_SEND_CHATROOM_VIDEO:
                    pushContent = String.format("%s %s", fromUser.getNickname(), "发送了一条视频消息");
                    break;
                case V5ProtoConstant.SERVICE_SEND_SINGE_CMD:
                case V5ProtoConstant.SERVICE_SEND_GROUP_CMD:
                case V5ProtoConstant.SERVICE_SEND_CHATROOM_CMD:
                    //命令消息没有推送提示语
                    break;
                default:
                    break;
            }
        }
        return pushContent;
    }


    private ReloadableResourceBundleMessageSource getMessageSource(Integer appID) {
        if (appID != null) {
            switch (appID) {
                case 0:
                    return messageSource;
                case 1:
                    return messageSource1;
            }
        }
        return messageSource;
    }


}
