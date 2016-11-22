package com.handwin.server.controller;

import com.handwin.packet.GenericPacket;
import com.handwin.packet.PacketHead;
import com.handwin.protocal.v5.codec.V5GenericPacket;
import com.handwin.protocal.v5.codec.V5PacketHead;
import com.handwin.server.Channel;
import com.handwin.service.MessageService;
import com.handwin.utils.V5ProtoConstant;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * Created by piguangtao on 15/7/15.
 */
@Service
@Controller(value = {V5ProtoConstant.SERVCIE_MSG_STATUS})
public class MsgStatusController implements ServiceController {
    private static final Logger LOGGER = LoggerFactory.getLogger(MsgStatusController.class);

    @Autowired
    private MessageService messageService;


    @Override
    public void handle(Channel channel, V5PacketHead packetHead, V5GenericPacket genericPacket) {

        String userId = channel.getChannelInfo().getUserId();
        //状态发送到用户 登录地IDC 一定是出生地
        String messageIdStr = genericPacket.getPacketHead().getServerMessageID();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[StatusMessageHandler:removeMessage],userId:{},messageId:{},status:{}", userId, messageIdStr, Arrays.toString(genericPacket.getBodySrcBytes()));
        }
        if (StringUtils.isBlank(messageIdStr)) {
            return;
        }

        messageService.removeMessage(userId, Long.valueOf(messageIdStr));

    }

    public static void main(String[] args) {
        byte[] test = new byte[]{0x01, 0x02};
        System.out.println(Arrays.toString(test));
    }
}
