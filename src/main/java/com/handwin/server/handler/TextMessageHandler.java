package com.handwin.server.handler;

import com.handwin.packet.TextMessagePacket;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TextMessageHandler extends SimpleMessageHandler<TextMessagePacket> implements InitializingBean {

    @Value("${country.codes}")
    private String currentRegionCode;

    public void afterPropertiesSet() throws Exception {
        register(TextMessagePacket.class);
    }

    @Override
    protected int messagePacketType() {
        return TextMessagePacket.TEXT_MESSAGE_PACKET_TYPE;
    }
}
