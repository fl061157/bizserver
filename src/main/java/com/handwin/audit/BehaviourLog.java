package com.handwin.audit;

import com.alibaba.fastjson.JSON;
import com.handwin.entity.ChannelInfo;
import com.handwin.packet.*;
import com.handwin.server.Channel;
import com.handwin.server.proto.BaseRequestMessage;
import com.handwin.server.proto.FullProtoRequestMessage;
import com.handwin.service.TcpSessionService;
import com.handwin.utils.ChannelUtils;
import com.handwin.utils.UserUtils;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by piguangtao on 15/3/25.
 */
@Service
public class BehaviourLog {
    private static final Logger LOGGER = LoggerFactory.getLogger(BehaviourLog.class);

    private static final Logger LOGGER_AUDIT = LoggerFactory.getLogger("com.handwin.audit.BehaviourLog_Audit");

    private static final Logger LOGGER_THIRDAPP_COMMIT = LoggerFactory.getLogger("com.handwin.third.ThirdApp_Commit");


    private static final String LOG_SPLIT_CHAR = "|";

    private ExecutorService executorService = Executors.newFixedThreadPool(2, new DefaultThreadFactory("[write-log-thread]"));

    @Autowired
    private TcpSessionService onlineStatusService;


    public void audit(SystemNotifyPacket notifyPacket) {
        executorService.submit(() -> {
            try {
                String userID = notifyPacket.getFrom();
                BehaviourInfo behaviourInfo = new BehaviourInfo();
                behaviourInfo.buildIp("")
                        .buildUserId(userID)
                        .buildSessionId("")
                        .buildTime(System.currentTimeMillis()).buildAppId(notifyPacket.getPacketHead().getAppId());

                behaviourInfo.buildAction(Action.SYSTEM_NOTIFY)
                        .buildReceiver(notifyPacket.getTo())
                        .addAttribe(String.valueOf(notifyPacket.getMsgType()))
                        .addAttribe(String.valueOf(notifyPacket.getServeType()))
                        .addAttribe(notifyPacket.getCmsgId())
                        .addAttribe(notifyPacket.getTraceId())
                        .addAttribe("");

                LOGGER_AUDIT.info(behaviourInfo.generateLog());

            } catch (Throwable e) {
                LOGGER.error("fails to audit user behaviour.", e);
            }

        });
    }


    public static class ThirdLogger {

        private Integer appID;
        private String from;
        private String to;
        private String action;
        private String content;
        private long createTime;
        private boolean isGroup;
        private String roomID;

        public Integer getAppID() {
            return appID;
        }

        public void setAppID(Integer appID) {
            this.appID = appID;
        }

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getTo() {
            return to;
        }

        public void setTo(String to) {
            this.to = to;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public long getCreateTime() {
            return createTime;
        }

        public void setCreateTime(long createTime) {
            this.createTime = createTime;
        }

        public boolean isGroup() {
            return isGroup;
        }

        public void setGroup(boolean group) {
            isGroup = group;
        }

        public String getRoomID() {
            return roomID;
        }

        public void setRoomID(String roomID) {
            this.roomID = roomID;
        }
    }


    private static final String ACTION_TEXT_MSG = "text_msg";
    private static final String ACTION_IMG_MSG = "img_msg";
    private static final String ACTION_VIDEO_MSG = "video_msg";
    private static final String ACTION_VOICE_MSG = "audio_msg";
    private static final String ACTION_VIDEO_CALL = "video_call";
    private static final String ACTION_AUDIO_CALL = "audio_call";
    private static final String ACTION_RECEIVE_CALL = "receive_call";
    private static final String ACTION_SYS_MSG = "custom_msg";
    private static final byte MSG_TYPE_GIF = 25;
    private static final byte MSG_TYPE_CUSTOMIZE = 24;
    private static final Base64 BASE_64 = new Base64();


    private String action(SimpleMessagePacket simpleMessagePacket) {
        switch (simpleMessagePacket.getPacketType()) {
            case TextMessagePacket.TEXT_MESSAGE_PACKET_TYPE:
                return ACTION_TEXT_MSG;
            case ImageMessagePacket.IMAGE_MESSAGE_PACKET_TYPE:
                return ACTION_IMG_MSG;
            case VideoMessagePacket.VIDEO_MESSAGE_PACKET_TYPE:
                return ACTION_VIDEO_MSG;
            case VoiceMessagePacket.VOICE_MESSAGE_PACKET_TYPE:
                return ACTION_VOICE_MSG;
        }
        return null;
    }

    private String action(CallPacket callPacket) {
        switch (callPacket.getCallStatus()) {
            case VIDEO_REQUEST:
                return ACTION_VIDEO_CALL;
            case AUDIO_REQUEST:
                return ACTION_AUDIO_CALL;
            case RECEIVED:
                return ACTION_RECEIVE_CALL;
        }
        return null;
    }

    private String action(SystemNotifyPacket systemNotifyPacket) {
        byte msgType = systemNotifyPacket.getMsgType();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[ThirdPart Logger] msgType:{} , from:{} , to:{}...", msgType, systemNotifyPacket.getFrom(), systemNotifyPacket.getTo());
        }
        if (msgType == MSG_TYPE_GIF || msgType == MSG_TYPE_CUSTOMIZE) {
            return ACTION_SYS_MSG;
        }
        return null;
    }


    private ThirdLogger build(Channel channel, SystemNotifyPacket systemNotifyPacket) {
        String action = action(systemNotifyPacket);
        if (StringUtils.isBlank(action)) return null;
        String from = channel.getChannelInfo().getUserId();
        String to = systemNotifyPacket.getTo();
        Integer appID = systemNotifyPacket.getPacketHead().getAppId();
        boolean isGroup = systemNotifyPacket.isGroup();
        String content = null;
        byte[] bytes = systemNotifyPacket.getMessageBody().getBytes();
        if (ArrayUtils.isNotEmpty(bytes)) {
            BASE_64.encode(bytes);
            byte[] dest = BASE_64.encodeBase64Chunked(bytes);
            content = new String(dest);
            if (StringUtils.isNotBlank(content)) {
                content.replaceAll("\r", "").replaceAll("\n", "");
            }
        }
        long createTime = System.currentTimeMillis();
        ThirdLogger thirdLogger = new ThirdLogger();
        thirdLogger.setAction(action);
        thirdLogger.setFrom(UserUtils.cutThirdUserID(from));
        thirdLogger.setTo(UserUtils.cutThirdUserID(to));
        thirdLogger.setAppID(appID);
        thirdLogger.setContent(content);
        thirdLogger.setGroup(isGroup);
        thirdLogger.setCreateTime(createTime);
        return thirdLogger;
    }


    private ThirdLogger build(Channel channel, SimpleMessagePacket simpleMessagePacket) {
        String action = action(simpleMessagePacket);
        if (StringUtils.isBlank(action)) return null;
        String from = channel.getChannelInfo().getUserId();
        String to = simpleMessagePacket.getToUser();
        Integer appID = simpleMessagePacket.getPacketHead().getAppId();
        String content = null;
        byte[] bytes = simpleMessagePacket.getContent();
        if (ArrayUtils.isNotEmpty(bytes)) {
            BASE_64.encode(bytes);
            byte[] dest = BASE_64.encodeBase64Chunked(bytes);
            content = new String(dest);
        }
        long createTime = System.currentTimeMillis();
        boolean isGroup = simpleMessagePacket.getMessageServiceType() == SimpleMessagePacket.TO_USER ? false : true;
        ThirdLogger thirdLogger = new ThirdLogger();
        thirdLogger.setAction(action);
        thirdLogger.setFrom(UserUtils.cutThirdUserID(from));
        thirdLogger.setTo(UserUtils.cutThirdUserID(to));
        thirdLogger.setAppID(appID);
        thirdLogger.setContent(content);
        thirdLogger.setGroup(isGroup);
        thirdLogger.setCreateTime(createTime);
        return thirdLogger;
    }


    private ThirdLogger build(Channel channel, CallPacket callPacket) {
        String action = action(callPacket);
        if (StringUtils.isBlank(action)) return null;
        String from = channel.getChannelInfo().getUserId();
        String to = callPacket.getPeerName();
        Integer appID = callPacket.getPacketHead().getAppId();
        long createTime = System.currentTimeMillis();
        boolean isGroup = false;
        String roomID = callPacket.getRoomId();
        ThirdLogger thirdLogger = new ThirdLogger();
        thirdLogger.setAction(action);
        thirdLogger.setFrom(UserUtils.cutThirdUserID(from));
        thirdLogger.setTo(UserUtils.cutThirdUserID(to));
        thirdLogger.setAppID(appID);
        thirdLogger.setRoomID(roomID);
        thirdLogger.setGroup(isGroup);
        thirdLogger.setCreateTime(createTime);
        return thirdLogger;
    }


    private String generateLog(ThirdLogger thirdLogger) {
        String log = String.format("|%d|%d|%s", System.currentTimeMillis(), thirdLogger.getAppID(), JSON.toJSONString(thirdLogger));
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[ThirdPart Logger] log:{} ", log);
        }
        return log;
    }


    public void thirdLoggerCommit(final Channel channel, final FullProtoRequestMessage message) {
        BasePacket basePacket = message.getPacket();
        if (channel == null || message == null || basePacket == null) {
            return;
        }
        if (!UserUtils.isThiradApp(basePacket.getPacketHead().getAppId())) {
            return;
        }

        ThirdLogger thirdLogger = null;

        if (basePacket instanceof SimpleMessagePacket) {
            thirdLogger = build(channel, (SimpleMessagePacket) basePacket);
        } else if (basePacket instanceof CallPacket) {
            thirdLogger = build(channel, (CallPacket) basePacket);
        } else if (basePacket instanceof SystemNotifyPacket) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[ThirdPart Logger] SystemNotifyPacket ===> ");
            }
            thirdLogger = build(channel, (SystemNotifyPacket) basePacket);
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[ThirdPart Logger] PacketType:{}", basePacket.getPacketType());
            }
        }

        if (thirdLogger != null) {
            final ThirdLogger tLogger = thirdLogger;
            executorService.submit(() -> {
                LOGGER_THIRDAPP_COMMIT.info(generateLog(tLogger
                ));
                return;
            });
        }

    }


    public void audit(final Channel channel, final FullProtoRequestMessage message) {
        executorService.submit(() -> {
            if (null == channel) return;
            try {
                BaseRequestMessage baseRequestMessage = message.getBaseRequestMessage();
                BasePacket packet = message.getPacket();
                String userID = StringUtils.isNotBlank(baseRequestMessage.getUserId()) ? baseRequestMessage.getUserId() : channel.getChannelInfo().getUserId();
                BehaviourInfo behaviourInfo = new BehaviourInfo();
                behaviourInfo.buildIp(null != channel ? channel.getIp() : "")
                        .buildUserId(userID)
                        .buildSessionId(baseRequestMessage.getSessionId())
                        .buildTime(System.currentTimeMillis()).buildAppId(packet.getPacketHead().getAppId());
                if (null != channel.getChannelInfo()) {
                    behaviourInfo.buildChannelUuid(channel.getChannelInfo().getUuid())
                            .buildChannelMode(String.valueOf(channel.getChannelInfo().getChannelMode().getValue()));
                }

                //在后面追加networkType
                String networkType = "";
                if (null != channel.getChannelInfo()) {
                    networkType = channel.getChannelInfo().getNetworkType();
                    if (StringUtils.isBlank(networkType)) {
                        String userId = baseRequestMessage.getUserId();
                        int appId = baseRequestMessage.getAppId();
                        networkType = getChannelNetworkType(userId, appId, channel.getChannelInfo().getUuid());
                    }
                }

                if (packet instanceof LoginPacket) {
                    LoginPacket packet1 = (LoginPacket) packet;
                    behaviourInfo.buildAction(Action.LOGIN_ACTION)
                            .buildSessionId(packet1.getSessionId())
                            .addAttribe(packet1.getRegionCode())
                            .addAttribe(null != packet1.getChannelMode() ? String.valueOf(packet1.getChannelMode().getValue()) : "")
                            .addAttribe(networkType);

                } else if (packet instanceof SimpleMessagePacket) {
                    SimpleMessagePacket packet1 = (SimpleMessagePacket) packet;
                    switch (packet1.getMessageType()) {
                        case TextMessagePacket.TEXT_MESSAGE_TYPE:
                            behaviourInfo.buildAction(Action.TEXT_MSG);
                            if (null != packet1.getFromGroup()) {
                                behaviourInfo.buildAction(Action.GROUP_TEXT_MSG);
                            }
                            break;

                        case ImageMessagePacket.IMAGE_URL_MESSAGE_TYPE:
                            behaviourInfo.buildAction(Action.PICTURE_MSG);
                            if (null != packet1.getFromGroup()) {
                                behaviourInfo.buildAction(Action.GROUP_PICTURE_MSG);
                            }
                            break;

                        case VoiceMessagePacket.VOICE_MESSAGE_TYPE:
                            behaviourInfo.buildAction(Action.AUDIO_MSG);
                            if (null != packet1.getFromGroup()) {
                                behaviourInfo.buildAction(Action.GROUP_AUDIO_MSG);
                            }
                            break;

                        case VideoMessagePacket.VIDEO_MESSAGE_TYPE:

                            boolean isAction = false;
                            try {
                                byte[] contents = packet1.getContent();
                                if (ArrayUtils.isNotEmpty(contents)) {
                                    String c = new String(contents);
                                    Map<String, String> m = JSON.parseObject(c, Map.class);
                                    if (MapUtils.isNotEmpty(m)) {
                                        String desc = m.get("desc");
                                        if (StringUtils.isNotBlank(desc)) {
                                            Map<String, Object> dm = JSON.parseObject(desc, Map.class);
                                            Object oT;
                                            if (MapUtils.isNotEmpty(dm) && (oT = dm.get("type")) != null) {
                                                if (oT.toString().equals("2")) {
                                                    behaviourInfo.buildAction(Action.VIDEO_FACE_MSG);
                                                    if (LOGGER.isInfoEnabled()) {
                                                        LOGGER.info("[ActionFace] type:2");
                                                    }
                                                    isAction = true;
                                                }
                                            }
                                        }
                                    }
                                }
                            } catch (Exception e) {
                            }

                            if (!isAction) {
                                behaviourInfo.buildAction(Action.VIDEO_MSG);
                                if (null != packet1.getFromGroup()) {
                                    behaviourInfo.buildAction(Action.GROUP_VIDEO_MSG);
                                }
                            }
                            break;

                        default:
                            //不需要写审计日志
                            return;
                    }
                    if (null != packet1.getFromGroup()) {
                        behaviourInfo.buildReceiver(packet1.getFromGroup());
                    } else {
                        behaviourInfo.buildReceiver(packet1.getToUser());
                    }
                    behaviourInfo.addAttribe(packet1.getCmsgid())
                            .addAttribe(String.valueOf(packet1.getMsgFlag()))
                            .addAttribe(null != packet1.getContent() ? String.valueOf(packet1.getContent().length) : "0")
                            .addAttribe(getUserCountryCode(channel))
                            .addAttribe(networkType);

                } else if (packet instanceof GameCallPacket) {
                    GameCallPacket gameCallPacket = (GameCallPacket) packet;
                    switch (gameCallPacket.getCallStatus()) {
                        case VIDEO_REQUEST_AGAIN:
                        case AUDIO_REQUEST_AGAIN:
                            //辅cal不进行审计
                            return;
                    }
                    behaviourInfo.buildAction(Action.GAME_CALL);
                    behaviourInfo.buildReceiver(gameCallPacket.getPeerName())
                            .addAttribe(String.valueOf(gameCallPacket.getCallStatus().id()))
                            .addAttribe(null != gameCallPacket.getStatus() ? String.valueOf(gameCallPacket.getStatus()) : "")
                            //.addAttribe(Arrays.toString(gameCallPacket.getSubCallTypes()))
                            .addAttribe(gameCallPacket.getTraceId())
                            .addAttribe(gameCallPacket.getRoomId())
                            .addAttribe(getUserCountryCode(channel))
                            .addAttribe(networkType);
                } else if (packet instanceof CallPacket) {
                    CallPacket callPacket = (CallPacket) packet;
                    switch (callPacket.getCallStatus()) {
                        case VIDEO_REQUEST_AGAIN:
                        case AUDIO_REQUEST_AGAIN:
                            //辅cal不进行审计
                            return;
                    }
                    behaviourInfo.buildAction(Action.CALL);
                    behaviourInfo.buildReceiver(callPacket.getPeerName())
                            .addAttribe(String.valueOf(callPacket.getCallStatus().id()))
                            .addAttribe(null != callPacket.getStatus() ? String.valueOf(callPacket.getStatus()) : "")
                            .addAttribe(callPacket.getTraceId())
                            .addAttribe(callPacket.getRoomId())
                            .addAttribe(getUserCountryCode(channel))
                            .addAttribe(networkType);
                } else if (packet instanceof SystemNotifyPacket) {
                    SystemNotifyPacket notifyPacket = (SystemNotifyPacket) packet;
                    behaviourInfo.buildAction(Action.SYSTEM_NOTIFY)
                            .buildReceiver(notifyPacket.getTo())
                            .addAttribe(String.valueOf(notifyPacket.getMsgType()))
                            .addAttribe(String.valueOf(notifyPacket.getServeType()))
                            .addAttribe(notifyPacket.getCmsgId())
                            .addAttribe(notifyPacket.getTraceId())
                            .addAttribe(networkType);
                } else if (packet instanceof GenericPacket) {
                    GenericPacket genericPacket = (GenericPacket) packet;
                    PacketHead head = genericPacket.getPacketHead();
                    behaviourInfo.buildAction(Action.V5_MESSAGE)
                            .buildReceiver(head.getTo())
                            .addAttribe(head.getService())
                            .addAttribe(head.getMessageID())
                            .addAttribe(networkType);
                } else if (packet instanceof LogoutPacket) {
                    LogoutPacket logoutPacket = new LogoutPacket();
                    behaviourInfo.buildAction(Action.LOGOUT_ACTION);
                    behaviourInfo.addAttribe(getUserCountryCode(channel));
                    behaviourInfo.addAttribe(null == logoutPacket.getChannelMode() ? "" : String.valueOf(logoutPacket.getChannelMode().getValue()))
                            .addAttribe(networkType);
                } else {
                    //不需要记录日志
                    return;
                }


                LOGGER_AUDIT.info(behaviourInfo.generateLog());

            } catch (Throwable e) {
                LOGGER.error("fails to audit user behaviour.", e);
            }
        });
    }

    private String getUserCountryCode(Channel channel) {
        String countryCode = "";
        if (null != channel && null != channel.getChannelInfo()) {
            countryCode = channel.getChannelInfo().getUserZoneCode();
        }
        return countryCode;
    }


    private enum Action {
        CALL("call"),
        TEXT_MSG("text"),
        GROUP_TEXT_MSG("group_text"),
        PICTURE_MSG("pic"),
        GROUP_PICTURE_MSG("group_pic"),
        GAME_CALL("game_call"),
        SYSTEM_NOTIFY("notify"),
        GROUP_SYSTEM_NOTIFY("group_notify"),
        V5_MESSAGE("v5"),
        LOGIN_ACTION("login"),
        LOGOUT_ACTION("logout"),
        VIDEO_MSG("video_msg"),
        VIDEO_FACE_MSG("video_face_msg"),
        GROUP_VIDEO_MSG("group_video_msg"),
        AUDIO_MSG("audio_msg"),
        GROUP_AUDIO_MSG("group_audio_msg"),
        LIVE_MESSAGE_SEND_COUNT("live_message_send_count");


        private String value;

        private Action(String value) {
            this.value = value;
        }

        public String value() {
            return this.value;
        }

    }

    private static class BehaviourInfo {
        private String userId;
        private String sessionId;
        private Action action;
        private long time;
        private String receiver;
        private String channelUuid;
        private String ip;
        private String channelMode;
        private List<String> otherAttributes = new ArrayList();
        private int appID;


        public BehaviourInfo buildChannelUuid(String channelUuid) {
            this.channelUuid = channelUuid;
            return this;
        }

        public BehaviourInfo buildAction(Action action) {
            this.action = action;
            return this;
        }

        public BehaviourInfo buildTime(long time) {
            this.time = time;
            return this;
        }

        public BehaviourInfo buildReceiver(String receiver) {
            this.receiver = receiver;
            return this;
        }

        public BehaviourInfo addAttribe(String attribute) {
            this.otherAttributes.add(attribute);
            return this;
        }

        public BehaviourInfo buildUserId(String userId) {
            this.userId = userId;
            return this;
        }

        public BehaviourInfo buildIp(String ip) {
            this.ip = ip;
            return this;
        }

        public BehaviourInfo buildSessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public BehaviourInfo buildChannelMode(String channelMode) {
            this.channelMode = channelMode;
            return this;
        }

        public BehaviourInfo buildAppId(int appID) {
            this.appID = appID;
            return this;
        }

        public String generateLog() {
            StringBuilder sb = new StringBuilder();
            sb.append(time).append(LOG_SPLIT_CHAR)
                    .append(null != action ? action.value() : "").append(LOG_SPLIT_CHAR)
                    .append(trimString(userId)).append(LOG_SPLIT_CHAR)
                    .append(trimString(receiver)).append(LOG_SPLIT_CHAR)
                    .append(trimString(sessionId)).append(LOG_SPLIT_CHAR)
                    .append(trimString(ip)).append(LOG_SPLIT_CHAR)
                    .append(trimString(channelUuid)).append(LOG_SPLIT_CHAR)
                    .append(trimString(channelMode));
            if (otherAttributes.size() > 0) {
                for (String attribute : otherAttributes) {
                    sb.append(LOG_SPLIT_CHAR)
                            .append(attribute);
                }
            }

            sb.append(LOG_SPLIT_CHAR).append(String.valueOf(appID));

            return sb.toString();
        }


        private String trimString(String value) {
            return null != value ? value.trim() : "";
        }
    }


    public void logChatMessageCount(Integer appID, String chatRoomID, int count) {
        StringBuilder sb = new StringBuilder();
        sb.append(System.currentTimeMillis()).append(LOG_SPLIT_CHAR)
                .append( Action.LIVE_MESSAGE_SEND_COUNT.value() ).append( LOG_SPLIT_CHAR )
                .append(appID).append(LOG_SPLIT_CHAR).append(chatRoomID)
                .append(LOG_SPLIT_CHAR).append(count);
        LOGGER_AUDIT.info(sb.toString());
    }

    public String getChannelNetworkType(String userId, int appId, String channelUuid) {
        String result = "";
        String appUserId = UserUtils.getAppUserId(userId, appId);
        ChannelInfo channelInfo = onlineStatusService.getChannelInfo(appUserId, channelUuid);

        if (null != channelInfo) {
            String networkType = channelInfo.getNetworkType();
            result = null != networkType ? networkType : "";
        }
        LOGGER.debug("channelUuid:{},networkType:{}", channelUuid, result);
        return result;
    }

    public static void main(String[] args) {

        Base64 base64 = new Base64();

        String str = "hellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohello";

        //byte[] enbytes = base64.encode(str.getBytes());
        //byte[] enbytes = base64.encodeBase64(str.getBytes(),true);
        byte[] enbytes = base64.encodeBase64Chunked(str.getBytes());


        //byte[] debytes = base64.decode(new String(enbytes).getBytes());
        //byte[] debytes = base64.decodeBase64(new String(enbytes).getBytes());
        byte[] debytes = base64.decodeBase64(new String(enbytes).getBytes());

        System.out.println("编码前:" + str);
        System.out.println("编码后:" + new String(enbytes));
        System.out.println("解码后:" + new String(debytes));


    }


}
