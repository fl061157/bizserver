package com.handwin.admin;

import com.handwin.codec.PacketCodecs;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by fangliang on 19/12/14.
 */
@Service
public class AdminChannelInitializer extends ChannelInitializer<SocketChannel> {

    @Autowired
    private AdminBusinessHandler adminPongHandler;

    @Autowired
    private PacketCodecs packetCodecs;


    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline channelPipeline = socketChannel.pipeline();
        channelPipeline.addLast(new AdminFrameEncoder(packetCodecs));
        channelPipeline.addLast(new AdminFrameDecoder(packetCodecs));
        channelPipeline.addLast(adminPongHandler);
    }
}
