package com.handwin.server.handler;

import com.handwin.bean.Platform;
import com.handwin.entity.User;
import com.handwin.exception.TraversingServerNotFoundException;
import com.handwin.packet.CallPacket;
import com.handwin.server.Channel;
import com.handwin.service.CallService;
import com.handwin.service.CallStatusService;
import com.handwin.service.ChannelService;
import com.handwin.service.LiveService;
import com.handwin.utils.ChannelUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CallHandler extends AbstractHandler<CallPacket> implements InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(CallHandler.class);

    @Autowired
    private ChannelService channelService;

    @Autowired
    private CallService callService;

    @Autowired
    private LiveService liveService;

    @Autowired
    protected CallStatusService callStatusService;

    public void afterPropertiesSet() throws Exception {
        register(CallPacket.class);
    }

    /**
     * 多机房的处理场景
     * A用户登录IDC1,出生地为IDC2
     * B用户登录IDC3，出生地为IDC4
     * A呼叫B，B响应
     * 呼叫流程中不同的指令，需要在不同的IDC进行处理
     *
     * @param packet 解码后的消息包
     *               srcMsgBytes        源字节数组（包括tcpServer发送的元消息和客户端发送到tcpServer的字节数组）
     */
    @Override
    public void handle(Channel channel, CallPacket packet) {
        String fromUserId = channel.getChannelInfo().getUserId();
        String toUserId = packet.getPeerName();
        if (logger.isInfoEnabled()) {
            logger.info("[Conn-Call].sender:{},receiver:{},code:{}.roomId:{}.start handle.",
                    fromUserId, toUserId, packet.getCallStatus(), packet.getRoomId());
        }
        User toUser = null;
        User fromUser = null;

        if (StringUtils.isBlank(fromUserId)) {
            logger.warn("[Conn-Call].fromUserId channelInfo:{}", channel.getChannelInfo());
            return;
        }

        ChannelUtils.wrapAppID(packet.getPacketHead(), channel);

        try {
            toUser = userService.findById(toUserId, channel.getChannelInfo().getAppID());
            fromUser = userService.findById(fromUserId, channel.getChannelInfo().getAppID());
        } catch (Throwable e) {
            logger.error("[CallHandler] findUser user.id:{} to.id:{} , error:{}", fromUserId, toUserId);
        }
        if (null == toUser || null == fromUser) {
            logger.warn("[Conn-Call].toUser or toUser is nul.channelInfo:{}", channel.getChannelInfo());
            return;
        }


        if (liveService.isDirectToLive(fromUser, toUser, packet)) {
            if (logger.isInfoEnabled()) {
                logger.info("direct call to live server. channel:{},user.id:{}", channel.getChannelInfo(), fromUserId);
            }
            try {
                liveService.handleDirectToLive(channel, fromUser, packet);
                return;
            } catch (TraversingServerNotFoundException e) {
                logger.error("no traversing server. continue to execute.");
            }
        }

        if (StringUtils.isBlank(fromUser.getCountrycode()) || userService.isLocalUser(fromUser.getCountrycode())) {
            switch (packet.getCallStatus()) {
                case VIDEO_ACCEPT:
                case AUDIO_ACCEPT:
                case REJECT:
                case BUSY:
                case RECEIVED:
                    callService.handleCallReceiveStatus(fromUser, toUserId, packet);
                    break;
                default:
                    break;
            }
        }

        boolean isSystemAccount = userService.isSystemAccount(toUser);

        if (!isSystemAccount && StringUtils.isBlank(toUser.getCountrycode())) {
            logger.warn("[Conn-Call].toUser regionCode is Null toUser.id:{}", toUser.getId());
            return;
        }

        //系统账号（小秘书没有国家码，需要在发送方IDC处理）
        if (!isSystemAccount && !userService.isLocalUser(toUser.getCountrycode())) {
            if (logger.isInfoEnabled()) {
                logger.info(
                        "[Conn-Call].ForwardMessage.traceId:{},sender:{},receiver:{},code:{}.roomId:{}.start handle.",
                        channel.getTraceId(), fromUserId, toUserId, packet.getCallStatus(), packet.getRoomId());
            }
            proxyMessageSender.write(toUser.getCountrycode(), packet);

            if (callService.isSetCallRoomInfo(packet.getCallStatus())) {
                callStatusService.setCallInfo(packet.getRoomId(), fromUser.getCountrycode(), fromUser.getId(), toUser.getId());
            }
            if (callService.isCleanCallRoomResource(packet.getCallStatus())) {
                callService.cleanCallRoomCache(packet.getRoomId());
            }
            return;
        }

        //Channel toChannel = channelService.findChannel(toUser);

        Map<Platform, List<Channel>> toChannelMap = channelService.findChannelMap(toUser);

        try {
            switch (packet.getCallStatus()) {
                case VIDEO_REQUEST:
                case AUDIO_REQUEST:
                    callService.call(channel, packet, toChannelMap , fromUser, toUser);
                    break;
                case VIDEO_ACCEPT:
                case AUDIO_ACCEPT:
                    callService.accept(channel, packet, toChannelMap, fromUser, toUser);
                    break;
                case REJECT:
                    callService.reject(channel, packet, toChannelMap, fromUser, toUser);
                    break;
                case HANGUP:
                    callService.hangup(channel, packet, toChannelMap, fromUser, toUser);
                    break;
                case BUSY:
                    //发送accept指令的用户IDC处理逻辑
                    callService.busyCommand(channel, packet, toChannelMap, toUser);
                    break;
                case VIDEO_REQUEST_AGAIN:
                case AUDIO_REQUEST_AGAIN:
                case ACCEPT_ACK:
                    callService.transCommand(channel, packet, toChannelMap, fromUser, toUser);
                    break;
                case RECEIVED: {
                    callService.received(channel, packet, toChannelMap, fromUser, toUser);
                    break;
                }
                default:
                    logger.warn("[Conn-Call].traceId:{},sender:{},receiver:{},code:{}.code not supported.",
                            channel.getTraceId(), fromUserId, toUserId, packet.getCallStatus());
                    break;
            }
        } finally {
            if (callService.isCleanCallRoomResource(packet.getCallStatus())) {
                callService.cleanCallRoomCache(packet.getRoomId());
            }
        }

    }
}