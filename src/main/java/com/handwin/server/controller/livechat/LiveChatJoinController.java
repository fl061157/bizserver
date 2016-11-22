package com.handwin.server.controller.livechat;

import com.alibaba.fastjson.JSON;
import com.handwin.bean.LiveResponse;
import com.handwin.entity.ChannelInfo;
import com.handwin.entity.ServiceType;
import com.handwin.packet.GenericPacket;
import com.handwin.protocal.v5.codec.V5GenericPacket;
import com.handwin.protocal.v5.codec.V5PacketHead;
import com.handwin.server.Channel;
import com.handwin.server.controller.Controller;
import com.handwin.server.controller.ServiceController;
import com.handwin.server.controller.livechat.cross.LiveChatCrossService;
import com.handwin.server.controller.livechat.cross.LiveChatCrossServiceAsync;
import com.handwin.server.controller.livechat.cross.LiveChatJoin;
import com.handwin.server.proto.ChannelAction;
import com.handwin.service.LiveChatService;
import com.handwin.service.UserService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;


/**
 * Created by fangliang on 16/7/6.
 */

@Service
@Controller(value = "/v5/live/join")
public class LiveChatJoinController implements ServiceController {

    private static final Logger logger = LoggerFactory.getLogger(LiveChatJoinController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private LiveChatService liveChatService;


    @Autowired
    @Qualifier(value = "rpcLiveChatCrossServiceAsync")
    private LiveChatCrossServiceAsync liveChatCrossService;

    @Override
    public void handle(Channel channel, V5PacketHead packetHead, V5GenericPacket genericPacket) {

        String from = packetHead.getFrom();
        String fromRegion = packetHead.getFromRegion();

        if (channel != null && channel.getChannelInfo() != null) {
            packetHead.setAppId(channel.getChannelInfo().getAppID());
        }

        if (!userService.isLocalUser(fromRegion)) { //TODO StatusCode StatusReason
            logger.error("[LiveChat] join from:{} , fromRegion:{}", from, fromRegion);
            writeResponse(packetHead, new LiveResponse().buildResult(-1).buildAction(new int[]{ChannelAction.SEND.getAction()}), channel);
            return;
        }

        String roomID = packetHead.getTo();
        String[] toAndRegion = roomID.split("@");
        if (toAndRegion == null || toAndRegion.length != 2) { //TODO StatusCode StatusReason
            logger.error("[LiveChat] join to format error to:{}", roomID);
            writeResponse(packetHead, new LiveResponse().buildResult(-1).buildAction(new int[]{ChannelAction.SEND.getAction()}), channel);
            return;
        }

        String toRegion = toAndRegion[1];

        ChannelInfo channelInfo = channel.getChannelInfo();
        if (channelInfo == null) {
            logger.error("[LiveChat] join  channelInfo null from:{} , roomID:{} , toRegion:{}", from, roomID, toAndRegion);
            writeResponse(packetHead, new LiveResponse().buildResult(-1).buildAction(new int[]{ChannelAction.SEND.getAction()}), channel);
            return;
        }

        String tcpNode = channelInfo.getNodeId();


        if (logger.isDebugEnabled()) {
            logger.debug("[LiveChatJoin] PacketHead roomID:{} , MessageID:{}", roomID, packetHead.getMessageID());
        }


        if (!userService.isLocalUser(toRegion)) {
            try {
                liveChatService.addTcpNodeToRoom(roomID, channelInfo.getNodeId());
                liveChatService.addUserToTcp(tcpNode, from);
                LiveChatJoin liveChatJoin = new LiveChatJoin();
                liveChatJoin.setFrom(from);
                liveChatJoin.setFromRegion(fromRegion);
                liveChatJoin.setRoomID(roomID);
                liveChatJoin.setTcpNodeID(tcpNode);
                liveChatJoin.setTraceID(channel.getTraceId());
                liveChatCrossService.join(liveChatJoin);
                writeResponse(packetHead, new LiveResponse().buildResult(0).buildAction(new int[]{ChannelAction.SEND.getAction(),
                        ChannelAction.JOIN_CHATROOM_ACTION.getAction()}), channel);

            } catch (Exception e) {
                logger.error("[LiveChat] join forward error from:{} , fromRegion:{} , roomID:{} ", from, fromRegion, roomID, e);
            }
            return;
        }


        String hostess = liveChatService.getHostessID(roomID);
        if (StringUtils.isBlank(hostess)) { //TODO StatusCode StatusReason
            logger.error("[LiveChat] join error for room not exists from:{} , to:{}", from, roomID);
            writeResponse(packetHead, new LiveResponse().buildResult(-1).buildAction(new int[]{ChannelAction.SEND.getAction()}), channel);
            return;
        }

        boolean isHostess = hostess.equals(from); //TODO ......

        boolean addTcp = liveChatService.joinRoom(roomID, tcpNode, from); // Mapper TcpNode RoomID

        if (!addTcp) {
            logger.error("[LiveChat] join  addTcpMapper error from:{} , roomID:{} , toRegion:{}", from, roomID, toAndRegion);
            writeResponse(packetHead, new LiveResponse().buildResult(-1).buildAction(new int[]{ChannelAction.SEND.getAction()}), channel);
        } else {
            writeResponse(packetHead, new LiveResponse().buildResult(0).buildAction(new int[]{ChannelAction.SEND.getAction(),
                    ChannelAction.JOIN_CHATROOM_ACTION.getAction()}), channel);
        }

        //TODO 给关注 主播的人发消息

    }


    private void writeResponse(V5PacketHead packetHead, LiveResponse liveResponse, Channel channel) {
        V5PacketHead copyHead = packetHead.copy();
        copyHead.setService(ServiceType.ChatJoinResponse.getType());

        if (logger.isDebugEnabled()) {
            logger.debug("[LiveChatJoinResponse] PacketHead  MessageID:{}",  packetHead.getMessageID());
        }

        int[] actions = liveResponse.getAction();
        int length = actions.length;
        ChannelAction[] channelActions = new ChannelAction[length];
        for (int i = 0; i < length; i++) {
            channelActions[i] = ChannelAction.getChannelAction(actions[i]);
        }

        V5GenericPacket v5GenericPacket = new V5GenericPacket();
        v5GenericPacket.setPacketHead(copyHead);

        byte[] content = JSON.toJSONBytes(liveResponse);
        v5GenericPacket.setBodySrcBytes(content);

        GenericPacket genericPacket = new GenericPacket();
        genericPacket.setV5GenericPacket(v5GenericPacket);
        channel.getChannelInfo().setChatRoomID(packetHead.getTo());
        channel.write(genericPacket, channelActions);
    }


}
