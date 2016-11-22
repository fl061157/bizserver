package com.handwin.server.handler;

import com.handwin.packet.VoiceMessagePacket;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class VoiceMessageHandler extends SimpleMessageHandler<VoiceMessagePacket> implements InitializingBean {

    @Value("${country.codes}")
    private String currentRegionCode;

    public void afterPropertiesSet() throws Exception {
        register(VoiceMessagePacket.class);
    }

    @Override
    protected int messagePacketType() {
        return VoiceMessagePacket.VOICE_MESSAGE_PACKET_TYPE;
    }
}
