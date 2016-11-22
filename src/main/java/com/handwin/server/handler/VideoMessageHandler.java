package com.handwin.server.handler;

import com.handwin.packet.VideoMessagePacket;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class VideoMessageHandler extends SimpleMessageHandler<VideoMessagePacket> implements InitializingBean {


    @Value("${country.codes}")
    private String currentRegionCode;

    public void afterPropertiesSet() throws Exception {
        register(VideoMessagePacket.class);
    }

    @Override
    protected int messagePacketType() {
        return VideoMessagePacket.VIDEO_MESSAGE_PACKET_TYPE;
    }
}
