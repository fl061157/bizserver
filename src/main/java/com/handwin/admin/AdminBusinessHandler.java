package com.handwin.admin;

import com.handwin.packet.BasePacket;
import com.handwin.packet.HeartbeatPacket;
import com.handwin.packet.HeartbeatResponsePacket;
import com.handwin.packet.PacketHead;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Created by fangliang on 19/12/14.
 */
@ChannelHandler.Sharable
@Service
public class AdminBusinessHandler extends SimpleChannelInboundHandler<BasePacket> {

    private static final Logger logger = LoggerFactory.getLogger(AdminBusinessHandler.class);


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, BasePacket basePacket) throws Exception {
        if (basePacket == null) {
            logger.error("BasePacket NullPointer !");
            ctx.fireExceptionCaught(new Exception("BasePacket NullPointer !"));
            return;
        }
        if (basePacket instanceof HeartbeatPacket) {
            HeartbeatPacket heartbeatPacket = (HeartbeatPacket) basePacket;
            if (logger.isDebugEnabled()) {
                logger.debug("receive heartBeatPacket:{}", heartbeatPacket);
            }
            HeartbeatResponsePacket heartbeatResponsePacket = new HeartbeatResponsePacket();
            PacketHead head = basePacket.getPacketHead() ;
            heartbeatResponsePacket.setPacketHead(head);
            heartbeatResponsePacket.setHeartBeatSentTime(head.getTimestamp());
            head.setTimestamp(System.currentTimeMillis());
            ctx.writeAndFlush(heartbeatResponsePacket);
        } else {
            logger.error("Not support other packet now !");
            ctx.fireExceptionCaught(new Exception("Not support other packet now !"));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        logger.error("channel exception caught !", cause);
        ctx.close();
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info( "channelInactive !" );
        ctx.close() ;
    }


}
