package com.handwin.server.handler;

import com.handwin.packet.*;
import com.handwin.server.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by fangliang on 16/6/28.
 */

@Service
public class DefaultSimpleHandler extends SimpleMessageHandler<SimpleMessagePacket> {

    private static final Logger logger = LoggerFactory.getLogger(DefaultSimpleHandler.class);

    @Autowired
    private TextMessageHandler textMessageHandler;

    @Autowired
    private ImageMessageHandler imageMessageHandler;

    @Autowired
    private VideoMessageHandler videoMessageHandler;

    @Autowired
    private VoiceMessageHandler voiceMessageHandler;

    @Override
    protected int messagePacketType() {
        return 0;
    }


    @Override
    public void handle(Channel channel, SimpleMessagePacket simpleMessagePacket) {

        if (simpleMessagePacket.getPacketHead() == null) {
            simpleMessagePacket.setPacketHead(new PacketHead());
        }

        try {
            switch (simpleMessagePacket.getMessageType()) {
                case TextMessagePacket.TEXT_MESSAGE_TYPE:
                    textMessageHandler.handle(channel, simpleMessagePacket);
                    break;
                case ImageMessagePacket.IMAGE_URL_MESSAGE_TYPE:
                    imageMessageHandler.handle(channel, simpleMessagePacket);
                    break;
                case VoiceMessagePacket.VOICE_MESSAGE_TYPE:
                    voiceMessageHandler.handle(channel, simpleMessagePacket);
                    break;
                case VideoMessagePacket.VIDEO_MESSAGE_TYPE:
                    videoMessageHandler.handle(channel, simpleMessagePacket);
                    break;
                default:
                    logger.error("Not support messageType:{}", simpleMessagePacket.getMessageType());

            }
        } catch (Exception e) {
            logger.error("", e);
        }
    }
}
