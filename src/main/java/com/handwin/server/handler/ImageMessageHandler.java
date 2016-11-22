package com.handwin.server.handler;

import com.handwin.packet.ImageMessagePacket;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ImageMessageHandler extends SimpleMessageHandler<ImageMessagePacket> implements InitializingBean {

    @Value("${country.codes}")
    private String currentRegionCode;

    public void afterPropertiesSet() throws Exception {
        register(ImageMessagePacket.class);
    }

    @Override
    protected int messagePacketType() {
        return ImageMessagePacket.IMAGE_MESSAGE_PACKET_TYPE;
    }

}
