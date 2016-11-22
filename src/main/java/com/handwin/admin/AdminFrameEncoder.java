package com.handwin.admin;

import com.handwin.codec.PacketCodecs;
import com.handwin.packet.BasePacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Created by fangliang on 19/12/14.
 */
public class AdminFrameEncoder extends MessageToByteEncoder<BasePacket> {

    private final PacketCodecs packetCodecs;

    public AdminFrameEncoder(PacketCodecs packetCodecs) {
        this.packetCodecs = packetCodecs;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, BasePacket basePacket, ByteBuf out) throws Exception {
        byte[] bytes = packetCodecs.encode(basePacket.getPacketHead().getVersion(), basePacket);
        if (bytes != null) {
            out.writeBytes(bytes);
        }
    }



}
