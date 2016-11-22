package com.handwin.server.controller.livechat.cross;

import com.handwin.entity.ChannelInfo;
import com.handwin.entity.ServiceType;
import com.handwin.packet.ChannelMode;
import com.handwin.packet.GenericPacket;
import com.handwin.protocal.v5.codec.V5GenericPacket;
import com.handwin.protocal.v5.codec.V5PacketHead;
import com.handwin.protocal.v5.constant.PacketHeadConstant;
import com.handwin.server.AbstractChannelImpl;
import com.handwin.server.Channel;
import com.handwin.server.proto.ChannelAction;
import com.handwin.service.LiveChatService;
import com.handwin.service.UserService;
import com.handwin.service.impl.ChannelFactory;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

/**
 * Created by fangliang on 16/7/18.
 */
public class LiveChatCrossHandler implements LiveChatCrossService {

    private static final Logger logger = LoggerFactory.getLogger(LiveChatCrossHandler.class);

    @Autowired
    private UserService userService;

    @Autowired
    private LiveChatService liveChatService;

    @Autowired
    private ChannelFactory channelFactory;


    @Override
    public void join(LiveChatJoin liveChatJoin) { //TODO MDC TraceID

        if (logger.isDebugEnabled()) {
            logger.debug("[LiveChatCrossJoin] LiveChatJoin:{}  ", liveChatJoin);
        }

        String fromRegion = liveChatJoin.getFromRegion();

        if (StringUtils.isBlank(fromRegion)) {
            logger.error("[LiveChatCrossJoin] fromRegion is empty !");
            return;
        }

        if (userService.isLocalUser(fromRegion)) {
            logger.error("[LiveChatCrossJoin] fromRegion:{} must not be local ! ");
            return;
        }

        String roomID = liveChatJoin.getRoomID();
        String[] toAndRegion = roomID.split("@");
        if (toAndRegion == null || toAndRegion.length != 2) {
            logger.error("[LiveChatCrossJoin] join to format error to:{}", roomID);
            return;
        }
        String toRegion = toAndRegion[1];

        if (!userService.isLocalUser(toRegion)) {
            logger.error("[LiveChatCrossJoin] toRegion:{} must be  local , roomID:{} ! ", roomID);
            return;
        }

        liveChatService.joinRoom(roomID, liveChatJoin.getTcpNodeID(), liveChatJoin.getFrom());

    }


    @Override
    public void send(LiveChatMessage liveChatMessage) {

        if (logger.isDebugEnabled()) {
            logger.debug("[LiveChatCrossMessage] LiveChatMessage:{}", liveChatMessage);
        }

        String fromRegion = liveChatMessage.getFromRegion();

        if (StringUtils.isBlank(fromRegion)) {
            logger.error("[LiveChatCrossMessage] fromRegion is empty !");
            return;
        }

        if (userService.isLocalUser(fromRegion)) {
            logger.error("[LiveChatCrossMessage] fromRegion:{} must not be local ! ");
            return;
        }

        String roomID = liveChatMessage.getRoomID();
        String[] toAndRegion = roomID.split("@");
        if (toAndRegion == null || toAndRegion.length != 2) {
            logger.error("[LiveChatCrossMessage] join to format error to:{}", roomID);
            return;
        }

        Set<String> tcpNodeSet = liveChatService.findTcpNode(roomID);
        if (CollectionUtils.isEmpty(tcpNodeSet)) {
            logger.error("[LiveChatCrossMessage] message error for tcpNode empty from:{} , to:{}", liveChatMessage.getFrom(), roomID);
            return;
        }

        final GenericPacket gPacket = createPacket(liveChatMessage);
        tcpNodeSet.stream().forEach(tcpNode -> {
            if (userService.isLocal(tcpNode)) {
                ChannelInfo cInfo = createChannelInfo(liveChatMessage);
                Channel c = createChannel(cInfo, tcpNode, roomID, liveChatMessage.getTraceID());

                if (logger.isDebugEnabled()) {
                    logger.debug("[LiveChatCrossMessage] Send tcpNode:{} ,  Content:{}", tcpNode, new String(liveChatMessage.getContent()));
                }
                c.write(gPacket, ChannelAction.MESSAGE_CHATROOM_ACTION);
            }
        });
    }


    @Override
    public void leave(LiveChatLeave liveChatLeave) { // 转发过来的一定是在主区 Leave

        if (logger.isDebugEnabled()) {
            logger.debug("[LiveChatCrossLeave] LiveChatLeave:{}", liveChatLeave);
        }

        String fromRegion = liveChatLeave.getFromRegion();

        if (StringUtils.isBlank(fromRegion)) {
            logger.error("[LiveChatCrossLeave] fromRegion is empty !");
            return;
        }

        if (userService.isLocalUser(fromRegion)) {
            logger.error("[LiveChatCrossLeave] fromRegion:{} must not be local ! ");
            return;
        }

        String roomID = liveChatLeave.getRoomID();
        String[] toAndRegion = roomID.split("@");
        if (toAndRegion == null || toAndRegion.length != 2) {
            logger.error("[LiveChatCrossLeave] leave to format error to:{}", roomID);
            return;
        }
        String toRegion = toAndRegion[1];

        if (!userService.isLocalUser(toRegion)) {
            logger.error("[LiveChatCrossLeave] toRegion:{} must be local , roomID:{} ! ", roomID);
            return;
        }

        liveChatService.leaveRoom(roomID, liveChatLeave.getTcpNodeID(), liveChatLeave.getFrom());


    }

    private Channel createChannel(ChannelInfo channelInfo, String tcpNode, String roomID, String traceID) {
        ChannelInfo cInfo = channelInfo.copy();
        cInfo.setChatRoomID(roomID);
        cInfo.setNodeId(tcpNode);
        AbstractChannelImpl channel = (AbstractChannelImpl) channelFactory.createChannel(cInfo);
        channel.setTraceId(traceID);
        return channel;
    }

    private GenericPacket createPacket(LiveChatMessage liveChatMessage) {
        V5PacketHead v5PacketHead = new V5PacketHead();
        v5PacketHead.setAppId(liveChatMessage.getAppID());
        v5PacketHead.setFromRegion(liveChatMessage.getFromRegion());
        v5PacketHead.setFrom(liveChatMessage.getFrom());
        v5PacketHead.setTo(liveChatMessage.getFromRegion());
        v5PacketHead.setService(ServiceType.ChatSysMessage.getType());
        v5PacketHead.setContentType(PacketHeadConstant.CONTENT_TYPE_BYTES);
        v5PacketHead.setService(liveChatMessage.getService());
        V5GenericPacket v5GenericPacket = new V5GenericPacket();
        v5GenericPacket.setBodySrcBytes(liveChatMessage.getContent());
        v5GenericPacket.setBodyType(Byte.valueOf(PacketHeadConstant.CONTENT_TYPE_BYTES));
        v5GenericPacket.setPacketHead(v5PacketHead);
        GenericPacket genericPacket = new GenericPacket();
        genericPacket.setV5GenericPacket(v5GenericPacket);
        genericPacket.setBodyType(GenericPacket.BODY_TYPE_BYTES);
        return genericPacket;
    }

    private ChannelInfo createChannelInfo(LiveChatMessage liveChatMessage) {
        ChannelInfo channelInfo = new ChannelInfo();
        channelInfo.setUserId(liveChatMessage.getFrom());
        channelInfo.setAppID(liveChatMessage.getAppID());
        channelInfo.setClientVersion(0x06);
        channelInfo.setChannelMode(ChannelMode.FOREGROUND);
        return channelInfo;
    }

}
