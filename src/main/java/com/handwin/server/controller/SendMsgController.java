package com.handwin.server.controller;

import com.handwin.packet.CommonMsgPackage;
import com.handwin.packet.GenericPacket;
import com.handwin.packet.PacketHead;
import com.handwin.protocal.v5.codec.V5GenericPacket;
import com.handwin.protocal.v5.codec.V5PacketHead;
import com.handwin.server.Channel;
import com.handwin.server.handler.CommonMessageHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.handwin.utils.V5ProtoConstant.*;

/**
 * Created by piguangtao on 15/7/14.
 */
@Service
@Controller(value = {SERVICE_SEND_SINGE_TEXT, SERVICE_SEND_SINGE_IMG, SERVICE_SEND_SINGE_AUDIO, SERVICE_SEND_SINGE_VIDEO, SERVICE_SEND_SINGE_CMD,
        SERVICE_SEND_GROUP_TEXT, SERVICE_SEND_GROUP_IMG, SERVICE_SEND_GROUP_AUDIO, SERVICE_SEND_GROUP_VIDEO, SERVICE_SEND_GROUP_CMD,
        SERVICE_SEND_CHATROOM_TEXT, SERVICE_SEND_CHATROOM_IMG, SERVICE_SEND_CHATROOM_AUDIO, SERVICE_SEND_CHATROOM_VIDEO, SERVICE_SEND_CHATROOM_CMD})
public class SendMsgController implements ServiceController {

    @Autowired
    private CommonMessageHandler commonMessageHandler;

    @Override
    public void handle(Channel channel, V5PacketHead packetHead, V5GenericPacket genericPacket) {

        GenericPacket gPacket = new GenericPacket();
        genericPacket.setPacketHead(packetHead);
        genericPacket.setBodySrcBytes(genericPacket.getBodySrcBytes());
        genericPacket.setBodyMap(genericPacket.getBodyMap());
        genericPacket.setBodyType(genericPacket.getBodyType());
        CommonMsgPackage commonMsgPackage = new CommonMsgPackage(gPacket);
        commonMessageHandler.handleMsg(channel, commonMsgPackage);
    }
}
