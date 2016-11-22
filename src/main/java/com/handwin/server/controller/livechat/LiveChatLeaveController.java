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
import com.handwin.server.controller.livechat.cross.LiveChatLeave;
import com.handwin.server.proto.ChannelAction;
import com.handwin.service.LiveChatService;
import com.handwin.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Created by fangliang on 16/7/8.
 */

@Service
@Controller(value = "/v5/live/leave")
public class LiveChatLeaveController implements ServiceController {


    private static final Logger logger = LoggerFactory.getLogger(LiveChatLeaveController.class);

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

        if (!userService.isLocalUser(fromRegion)) {
            logger.error("[LiveChat] leave from:{} , fromRegion:{}", from, fromRegion);
            writeResponse(packetHead, new LiveResponse().buildResult(-1).buildAction(new int[]{ChannelAction.SEND.getAction()}), channel);
            return;
        }

        String roomID = packetHead.getTo();
        String[] toAndRegion = roomID.split("@");
        if (toAndRegion == null || toAndRegion.length != 2) {
            logger.error("[LiveChat] leave to format error to:{}", roomID);
            writeResponse(packetHead, new LiveResponse().buildResult(-1).buildAction(new int[]{ChannelAction.SEND.getAction()}), channel);
            return;
        }

        String toRegion = toAndRegion[1];

        ChannelInfo channelInfo = channel.getChannelInfo();
        if (channelInfo == null) {
            logger.error("[LiveChat] leave  channelInfo null from:{} , roomID:{} , toRegion:{}", from, roomID, toAndRegion);
            writeResponse(packetHead, new LiveResponse().buildResult(-1).buildAction(new int[]{ChannelAction.SEND.getAction()}), channel);
            return;
        }

        String tcpNode = channelInfo.getNodeId();


        if (!userService.isLocalUser(toRegion)) {

            liveChatService.leaveSecondRoom(roomID, tcpNode, from);

            LiveChatLeave liveChatLeave = new LiveChatLeave();
            liveChatLeave.setRoomID(roomID);
            liveChatLeave.setFrom(from);
            liveChatLeave.setFromRegion(fromRegion);
            liveChatLeave.setTcpNodeID(tcpNode);
            liveChatLeave.setTraceID(channel.getTraceId());
            liveChatCrossService.leave(liveChatLeave);

        } else {
            liveChatService.leaveRoom(roomID, tcpNode, from);
        }

        writeResponse(packetHead, new LiveResponse().buildResult(0).buildAction(new int[]{ChannelAction.SEND.getAction(), ChannelAction.QUIT_CHATROOM_ACTION.getAction()}),
                channel);

    }


    private void writeResponse(V5PacketHead packetHead, LiveResponse liveResponse, Channel channel) {
        V5PacketHead copyHead = packetHead.copy();
        copyHead.setService(ServiceType.ChatLeaveResponse.getType());

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
        channel.write(genericPacket, channelActions);
    }


}
