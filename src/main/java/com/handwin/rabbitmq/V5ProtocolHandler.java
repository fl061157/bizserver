package com.handwin.rabbitmq;

import com.handwin.entity.ChannelInfo;
import com.handwin.entity.User;
import com.handwin.exception.ServerException;
import com.handwin.mq.MessageListener;
import com.handwin.protocal.v5.codec.V5GenericPacket;
import com.handwin.protocal.v5.codec.V5GenericPacketDecodeAndEncoder;
import com.handwin.protocal.v5.codec.V5PacketHead;
import com.handwin.server.Channel;
import com.handwin.server.PseudoChannelImpl;
import com.handwin.server.controller.ServiceControllerManager;
import com.handwin.service.ChannelService;
import com.handwin.service.UserService;
import com.handwin.utils.SystemConstant;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

/**
 * Created by piguangtao on 15/12/17.
 * v5Protocal协议解析 直接v5协议解析
 */
public class V5ProtocolHandler extends MessageListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(V5ProtocolHandler.class);

    private V5GenericPacketDecodeAndEncoder decodeAndEncoder = new V5GenericPacketDecodeAndEncoder();

    @Autowired
    ServiceControllerManager serviceControllerManager;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private UserService userService;

    @Override
    public void onMessage(byte[] message) throws Throwable {
        try {
            ByteBuf byteBuf = Unpooled.wrappedBuffer(message);
            V5GenericPacket v5GenericPacket = decodeAndEncoder.decode(byteBuf);

            //设置整个消息的二进制包
            v5GenericPacket.setSrcMsgBytes(message);

            //根据发送方查找用户channel
            V5PacketHead v5PacketHead = v5GenericPacket.getPacketHead();
            String traceId = v5PacketHead.getTraceId();
            if (StringUtils.isBlank(traceId)) {
                traceId = UUID.randomUUID().toString();
            }

            addMDCValue(traceId);
            String sendId = v5PacketHead.getFrom();

            //server端回复确认 才需要组装发送方通道信息
            Channel channel = null;
            if (v5PacketHead.getServerReceivedConfirm()) {
                String via = v5PacketHead.getVia();
                //不是转发的包 才需要组装真正的channel 信息
                if (StringUtils.isNotBlank(via) && via.toLowerCase().contains("bizserver")) {
                    User user = userService.findById(sendId , v5PacketHead.getAppId() );
                    if (null == user) {
                        throw new ServerException(String.format("userId:%s does not exist", sendId));
                    }
                    channel = channelService.findChannel(user);
                }
            }

            //使用伪channel
            if (null == channel) {
                User sender = userService.findById(sendId , v5PacketHead.getAppId());
                ChannelInfo channelInfo = new ChannelInfo();
                channelInfo.setUserId(sendId);
                if (null != sender) {
                    channelInfo.setAppID(sender.getAppId());
                    channelInfo.setUserZoneCode(sender.getCountrycode());
                }
                channelInfo.setTcpZoneCode(v5PacketHead.getFromRegion());
                channel = new PseudoChannelImpl(v5PacketHead.getTraceId(), channelInfo);
            }
            serviceControllerManager.handler(channel, v5GenericPacket);
        } catch (Exception e) {
            LOGGER.error("fails to handle v5 protocol", e);
        } finally {
            cleanMDCValue();
        }
    }


    private void addMDCValue(String traceId) {
        MDC.put("TraceID", traceId);
        String nodeId = System.getProperty("node.id");
        if (null == nodeId) nodeId = String.valueOf(SystemConstant.NODE_ID_DEFAULT);
        String nodeIp = System.getProperty("node.ip");
        if (null == nodeIp) nodeIp = SystemConstant.NODE_IP_DEFAULT;
        System.setProperty("LOG_PREFIX", String.format("[biz:%s]", nodeId));
        System.setProperty("LOG_FLUME_IP", nodeIp);
        MDC.put("PREFIX", String.format("%s|%s|%s", nodeId, nodeIp, "BizServer"));
    }

    private void cleanMDCValue() {
        MDC.remove("TraceID");
    }
}
