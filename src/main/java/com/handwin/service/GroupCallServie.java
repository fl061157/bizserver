package com.handwin.service;

import com.handwin.entity.Group;
import com.handwin.packet.*;
import com.handwin.persist.StatusStore;
import com.handwin.server.Channel;
import com.handwin.utils.UserUtils;
import org.apache.http.client.fluent.Request;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;


@Service
public class GroupCallServie {

    private static final Logger LOGGER = LoggerFactory.getLogger(GroupCallServie.class);

    private static final Integer CALL_TYPE_GROUP_CALL = 3;

    private static final Integer CALL_TYPE_LIVE_CALL = 5;

    @Autowired
    private GroupService groupService;

    @Autowired
    @Qualifier(value = "statusClusterStoreImpl")
    private StatusStore statusStore;


    @Value("${http.serve.url}")
    protected String httpServerUrl;


    public boolean isMcuCall(GameCallReqPacket packet) {
        boolean result = false;
        if (null != packet) {
            Integer[] callTypes = packet.getSubCallTypes();
            for (Integer callType : callTypes) {
                if (CALL_TYPE_GROUP_CALL == callType || CALL_TYPE_LIVE_CALL == callType) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }


    private boolean isLiveCall(GameCallReqPacket packet) {
        boolean result = false;
        if (null != packet) {
            Integer[] callTypes = packet.getSubCallTypes();
            for (Integer callType : callTypes) {
                if (CALL_TYPE_LIVE_CALL == callType) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    public void handle(Channel channel, GameCallReqPacket packet) {
        if (null == packet || null == channel) {
            return;
        }
        String fromUserId = channel.getChannelInfo().getUserId();
        String sessionId = channel.getChannelInfo().getSessonId();
        String groupId = packet.getPeerName();
        LOGGER.debug("[group call] from:{} groupId:{},packet:{}, status:{}", fromUserId, groupId, packet, packet.getCallStatus());

        switch (packet.getCallStatus()) {
            case VIDEO_REQUEST:
            case AUDIO_REQUEST: {
                //群组视频呼叫请求
                //增加群组成员 中参与者的角色  成员数目 限制群组参与者的数目

                //发送接受请求
                CallPacket response = buildGroupCallResponsePacket(groupId, CallStatus.VIDEO_ACCEPT, packet, null);

                channel.write(response);

                break;
            }
            case HANGUP: {
                //退出 即挂断
                //群组视频通话总人数
                if (!isLiveCall(packet)) {
                    sendGroupCallExit(sessionId, groupId, packet.getRoomId(), packet.getPacketHead().getAppId());
                }
                break;
            }
            //心跳 维持 呼叫信息
            case GROUP_CALL_HEARTBEAT: {
                LOGGER.debug("[group call] from:{} groupId:{} heartbeat", fromUserId, groupId);

                if (isLiveCall(packet)) {
                    try {
                        if (LOGGER.isInfoEnabled()) {
                            LOGGER.info("[LiveHeart] Zadd chatRoomID:{} , userID:{} ", packet.getPeerName(), fromUserId);
                        }
                        statusStore.zAdd("LINK_MIC_LASTTIME_KEY", System.currentTimeMillis(), String.format("%s,%s", UserUtils.chatRoomId(packet.getPeerName()), fromUserId));
                    } catch (Exception e) {
                    }
                } else {
                    sendGroupCallHeartbeat(sessionId, groupId, packet.getRoomId(), packet.getPacketHead().getAppId());
                }
                break;
            }
            default: {
                LOGGER.info("call status:{} not support.", packet.getCallStatus());
                break;
            }
        }


    }


    public CallPacket buildGroupCallResponsePacket(String peerName, CallStatus callStatus,
                                                   CallPacket callPacket, byte[] extraData) {
        GameCallRespPacket respPacket = new GameCallRespPacket();
        PacketHead packetHead = new PacketHead();
        packetHead.setVersion((byte) 0x04);
        respPacket.setPacketHead(packetHead);
        respPacket.setCallStatus(callStatus);
        respPacket.setPeerName(peerName);
        respPacket.setSsrc(((GameCallReqPacket) callPacket).getSsrc());
        respPacket.setSubCallTypes(((GameCallReqPacket) callPacket).getSubCallTypes());
        respPacket.setGameRooms(((GameCallReqPacket) callPacket).getGameRooms());
        if (null != extraData) {
            respPacket.setExtraData(extraData);
        } else {
            respPacket.setExtraData(((GameCallReqPacket) callPacket).getExtraData());

        }
        return respPacket;
    }


    /**
     * 发送群组视频的心跳
     * TODO 心跳时间需要间隔一定时间发送
     *
     * @param groupId
     * @param roomId
     */
    public void sendGroupCallHeartbeat(String sessionId, String groupId, String roomId, Integer appID) {
        Group group = groupService.findGroupInfo(groupId, appID);
        if (group == null) {
            LOGGER.error("[GroupCallService] group:{} , appId:{} , roomId:{} group empty !", groupId, appID, roomId);
            return;
        }
        String region = group.getRegion();
        String heartbeatPath = "/api/mcu/call/heartbeat";

        boolean isNeedResend = true;

        for (int i = 0; i < 3 && isNeedResend; i++) {
            try {
                Request request = Request.Post(String.format("%s%s", httpServerUrl, heartbeatPath))
                        .bodyForm(((Supplier<List<BasicNameValuePair>>) () -> {
                            List<BasicNameValuePair> basicNameValuePairList = Arrays.asList(new BasicNameValuePair("groupId", group.getId()), new BasicNameValuePair("roomId", roomId));
                            return basicNameValuePairList;
                        }).get(), Charset.forName("UTF-8"));
                request.addHeader("client-session", sessionId);
                request.addHeader("region-code", region);
                request.addHeader("x-group-region-code", region);
                request.addHeader("x-from-module", "bizServer");
                request.addHeader("app-id", String.valueOf(appID));
                String result = request.execute().returnContent().asString();
                LOGGER.info("[group call ]heartbeat. groupId:{}, roomId:{}. resp:{}", groupId, roomId, result);
                isNeedResend = false;
            } catch (Throwable e) {
                LOGGER.error("[share video urls].fails to get share video urls.", e);
                isNeedResend = true;
            }
        }
    }


    /**
     * 发送群组视频退出
     * TODO 客户端退出视频 采用mcu接口时 去掉此功能
     *
     * @param groupId
     * @param roomId
     */
    public void sendGroupCallExit(String sessionId, String groupId, String roomId, Integer appID) {
        Group group = groupService.findGroupInfo(groupId, appID);
        String region = group.getRegion();
        String groupCallExitPath = "/api/mcu/call/role/change";

        boolean isNeedResend = true;

        for (int i = 0; i < 3 && isNeedResend; i++) {
            try {
                Request request = Request.Post(String.format("%s%s", httpServerUrl, groupCallExitPath))
                        .bodyForm(((Supplier<List<BasicNameValuePair>>) () -> {
                            List<BasicNameValuePair> basicNameValuePairList = Arrays.asList(new BasicNameValuePair("groupId", group.getId()), new BasicNameValuePair("role", "0"), new BasicNameValuePair("roomId", roomId));
                            return basicNameValuePairList;
                        }).get(), Charset.forName("UTF-8"));
                request.addHeader("client-session", sessionId);
                request.addHeader("region-code", region);
                request.addHeader("x-group-region-code", region);
                request.addHeader("x-from-module", "bizServer");
                request.addHeader("app-id", String.valueOf(appID));
                String result = request.execute().returnContent().asString();
                LOGGER.info("[group call ]heartbeat. groupId:{}, roomId:{}. resp:{}", groupId, roomId, result);
                isNeedResend = false;
            } catch (Throwable e) {
                LOGGER.error("[share video urls].fails to get share video urls.", e);
                isNeedResend = true;
            }
        }
    }

}
