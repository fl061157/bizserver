package com.handwin.admin;

import com.handwin.codec.PacketCodecs;
import com.handwin.packet.BasePacket;
import com.handwin.packet.PacketHead;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by fangliang on 19/12/14.
 */
public class AdminFrameDecoder extends ByteToMessageDecoder {

    public final static int PACKET_HEAD_SIZE = 16;

    private static final Logger logger = LoggerFactory.getLogger(AdminFrameDecoder.class);

    private final PacketCodecs packetCodecs;

    private PacketHead packetHead;

    public AdminFrameDecoder(PacketCodecs packetCodecs) {
        this.packetCodecs = packetCodecs;
    }


    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < PACKET_HEAD_SIZE) {
            return;
        }
        packetHead = null;
        try {
            packetHead = packetCodecs.decodePacketHead(in);
        } catch (Exception e) {
            logger.error("decode packetHead Error !", e);
            ctx.fireExceptionCaught(e);
            return;
        }
        if (packetHead == null) {
            logger.error("decode packetHead Null");
            ctx.fireExceptionCaught(new Exception("packetHead NullPointer !"));
        }
        int packetSize = packetHead.getPacketSize();
        if (in.readableBytes() < packetSize) {
            return;
        }
        BasePacket basePacket = null;
        try {
            basePacket = packetCodecs.decode(in, packetHead);
        } catch (Exception e) {
            logger.error("decode BasePacket Error !", e);
        }
        if (basePacket == null) {
            logger.error("decode BasePacket NullPointer !");
        }
        out.add(basePacket);
    }

    @Override
    protected void decodeLast(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        decode(ctx, in, out);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        logger.error("channel exception caught !", cause);
        ctx.close() ;
    }
}
