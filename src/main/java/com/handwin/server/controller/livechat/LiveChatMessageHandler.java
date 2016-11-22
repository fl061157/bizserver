package com.handwin.server.controller.livechat;

import com.handwin.api.sysmsg.bean.LiveSysMessage;
import com.handwin.api.sysmsg.service.LiveSysMessageService;
import com.handwin.entity.ChannelInfo;
import com.handwin.entity.ServiceType;
import com.handwin.packet.ChannelMode;
import com.handwin.protocal.v5.codec.V5GenericPacket;
import com.handwin.protocal.v5.codec.V5PacketHead;
import com.handwin.protocal.v5.constant.PacketHeadConstant;
import com.handwin.server.PseudoChannelImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * Created by fangliang on 16/7/14.
 */

public class LiveChatMessageHandler implements LiveSysMessageService {

    private static final Logger logger = LoggerFactory.getLogger(LiveChatMessageHandler.class);

    @Autowired
    private LiveChatMessageService liveChatMessageService;

    @Override
    public void send(LiveSysMessage liveSysMessage) {


        if (logger.isDebugEnabled()) {
            logger.debug("[LiveChatRpc] from:{} , fromRegion:{} , to:{} , traceID:{}  ", liveSysMessage.getFrom(), liveSysMessage.getFromRegion(),
                    liveSysMessage.getTo(), liveSysMessage.getTraceID());
        }

        V5PacketHead v5PacketHead = new V5PacketHead();
        v5PacketHead.setAppId(liveSysMessage.getAppID());
        v5PacketHead.setFromRegion(liveSysMessage.getFromRegion());
        v5PacketHead.setFrom(liveSysMessage.getFrom());
        v5PacketHead.setTo(liveSysMessage.getTo());
        v5PacketHead.setService(ServiceType.ChatSysMessage.getType());
        v5PacketHead.setContentType(PacketHeadConstant.CONTENT_TYPE_BYTES);


        V5GenericPacket v5GenericPacket = new V5GenericPacket();
        v5GenericPacket.setBodySrcBytes(liveSysMessage.getBodySrc());
        v5GenericPacket.setBodyType( Byte.valueOf( PacketHeadConstant.CONTENT_TYPE_BYTES ) );
        v5GenericPacket.setPacketHead(v5PacketHead);

        ChannelInfo channelInfo = new ChannelInfo();
        channelInfo.setUserId(liveSysMessage.getFrom());
        channelInfo.setAppID(liveSysMessage.getAppID());
        channelInfo.setClientVersion(0x06);
        channelInfo.setChannelMode(ChannelMode.FOREGROUND );

        PseudoChannelImpl pseudoChannel = new PseudoChannelImpl(liveSysMessage.getTraceID(), channelInfo);

        liveChatMessageService.handle(pseudoChannel, v5PacketHead, v5GenericPacket);
    }

}
