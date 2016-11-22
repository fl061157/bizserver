package com.handwin.server.handler;

import com.handwin.bean.Platform;
import com.handwin.entity.User;
import com.handwin.exception.ServerException;
import com.handwin.packet.GameCallReqPacket;
import com.handwin.server.Channel;
import com.handwin.service.*;
import com.handwin.utils.ChannelUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Created by wyang on 2014/8/18.
 */
@Service
public class GameCallHandler extends AbstractHandler<GameCallReqPacket>
        implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(GameCallHandler.class);

    @Autowired
    private TcpSessionService onlineStatusService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private GameCallService callService;

    @Autowired
    private GroupCallServie groupCallServie;

    public void afterPropertiesSet() throws Exception {
        register(GameCallReqPacket.class);
    }

    @Override
    public void handle(Channel channel, GameCallReqPacket packet) {
        //如果是群组呼叫，则自己处理
        if (groupCallServie.isMcuCall(packet)) {

            ChannelUtils.wrapAppID(packet.getPacketHead(), channel);

            groupCallServie.handle(channel, packet);
            return;
        }

        String fromUserId = channel.getChannelInfo().getUserId();
        String toUserId = packet.getPeerName();
        if (logger.isInfoEnabled()) {
            logger.info("[Conn-Call].sender:{},receiver:{},code:{}.roomId:{}.start handle.",
                    fromUserId, toUserId, packet.getCallStatus(), packet.getRoomId());
        }
        User toUser;
        User fromUser;
        try {
            toUser = userService.findById(toUserId, channel.getChannelInfo().getAppID());
            fromUser = userService.findById(fromUserId, channel.getChannelInfo().getAppID());
        } catch (ServerException e) {
            logger.error("[GameCallHandler] findUser user.id:{} to.id:{} , error:{}",
                    fromUserId, toUserId);
            return;
        }
        if (toUser == null || fromUser == null) {
            logger.error("[GameCallHandler].toUserNull.toUserId:{}", toUserId);
            return;
        }


        if (StringUtils.isBlank(fromUser.getCountrycode()) || userService.isLocalUser(fromUser.getCountrycode())) {
            switch (packet.getCallStatus()) {
                case VIDEO_ACCEPT:
                case AUDIO_ACCEPT:
                case REJECT:
                case BUSY:
                case RECEIVED:
                    callService.handleCallReceiveStatus(fromUser, toUserId, packet);
                default:
                    break;
            }
        }

        boolean isSystemAccount = userService.isSystemAccount(toUser);
        if (!isSystemAccount && StringUtils.isBlank(toUser.getCountrycode())) {
            logger.warn("[GameCallHandler].toUser regionCode is Empty toUser.id:{} ", toUserId);
            return;
        }


        if (!isSystemAccount && !userService.isLocalUser(toUser.getCountrycode())) {
            proxyMessageSender.write(toUser.getCountrycode(), packet);
            if (logger.isInfoEnabled()) {
                logger.info("[Conn-Call].ForwardMessage.traceId:{},sender:{},receiver:{},code:{}.roomId:{}.start handle.",
                        channel.getTraceId(), fromUserId, toUserId, packet.getCallStatus(), packet.getGameRooms());
            }
            return;
        }


        //Channel toChannel = channelService.findChannel(toUser);

        Map<Platform, List<Channel>> toChannelMap = channelService.findChannelMap(toUser);

        switch (packet.getCallStatus()) {
            case VIDEO_REQUEST:
            case AUDIO_REQUEST:
                callService.call(channel, packet, toChannelMap, fromUser, toUser);
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

            case RECEIVED:
                callService.received(channel, packet, toChannelMap, fromUser, toUser);
                break;

            case BUSY:
                callService.busyCommand(channel, packet, toChannelMap, toUser);
                break;

            case VIDEO_REQUEST_AGAIN:
            case AUDIO_REQUEST_AGAIN:
            case ACCEPT_ACK:
                callService.transCommand(channel, packet, toChannelMap, fromUser, toUser);
                break;

            default:
                logger.warn("[Conn-Call].traceId:{},sender:{},receiver:{},code:{}.code not supported.",
                        channel.getTraceId(), fromUserId, toUserId, packet.getCallStatus());
                break;
        }

    }

}
