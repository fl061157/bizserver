package com.handwin.server.controller.livechat;

import com.handwin.protocal.v5.codec.V5GenericPacket;
import com.handwin.protocal.v5.codec.V5PacketHead;
import com.handwin.server.Channel;
import com.handwin.server.controller.Controller;
import com.handwin.server.controller.ServiceController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * Created by fangliang on 16/7/7.
 */

@Service
@Controller(value = "/v5/live/message")
public class LiveChatMessageController implements ServiceController {

    @Autowired
    private LiveChatMessageService liveChatMessageService;


    @Override
    public void handle(Channel channel, V5PacketHead packetHead, V5GenericPacket genericPacket) {

        if (channel != null && channel.getChannelInfo() != null) {
            packetHead.setAppId(channel.getChannelInfo().getAppID());
        }

        liveChatMessageService.handle(channel, packetHead, genericPacket);
    }

}
