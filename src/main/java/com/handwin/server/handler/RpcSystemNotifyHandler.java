package com.handwin.server.handler;

import com.handwin.api.sysmsg.bean.SimpleMessage;
import com.handwin.api.sysmsg.bean.SysMessage;
import com.handwin.api.sysmsg.service.SysMessageService;
import com.handwin.api.sysmsg.util.SysMessageUtil;
import com.handwin.audit.BehaviourLog;
import com.handwin.codec.SimpleEncoder;
import com.handwin.codec.SystemEncoder;
import com.handwin.entity.ChannelInfo;
import com.handwin.packet.SimpleMessagePacket;
import com.handwin.packet.SystemNotifyPacket;
import com.handwin.protocal.v5.codec.V5GenericPacket;
import com.handwin.protocal.v5.codec.V5GenericPacketDecodeAndEncoder;
import com.handwin.protocal.v5.codec.V5PacketHead;
import com.handwin.server.Channel;
import com.handwin.server.PseudoChannelImpl;
import com.handwin.server.controller.ServiceControllerManager;
import com.handwin.utils.SystemConstant;
import io.netty.buffer.ByteBuf;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.UUID;

/**
 * Created by fangliang on 16/6/14.
 */

public class RpcSystemNotifyHandler implements SysMessageService {

    @Autowired
    private SystemNotifyHandler notifyHandler;

    @Autowired
    private GroupSystemNotifyFromCoreServerHandler groupNotifyHandler;

    @Autowired
    private SystemEncoder systemEncoder;

    @Autowired
    private SimpleEncoder simpleEncoder;

    @Autowired
    private DefaultSimpleHandler defaultSimpleHandler;

    @Autowired
    private ServiceControllerManager serviceControllerManager;

    @Autowired
    private BehaviourLog behaviourLog;

    private V5GenericPacketDecodeAndEncoder decodeAndEncoder = new V5GenericPacketDecodeAndEncoder();

    private static final Logger logger = LoggerFactory.getLogger(RpcSystemNotifyHandler.class);

    @Override
    public void send(SysMessage sysMessage, byte[] bytes, String extra) {
        send(sysMessage, new String(bytes, Charset.forName("UTF-8")), extra);
    }

    @Override
    public void send(SysMessage sysMessage, String s, String extra) {
        addMDC(sysMessage.getTraceID());
        doSend(sysMessage, s, extra);
    }

    private void doSend(SysMessage sysMessage, String s, String extra) {

        SystemNotifyPacket packet = systemEncoder.encodePacket(sysMessage, s, extra);

        if (packet == null) {
            logger.error("[RpcSystem] encode systemNotifyPacket error from:{} , to:{} ", sysMessage.getFrom(), sysMessage.getTo());
            return;
        }

        ChannelInfo channelInfo = new ChannelInfo();
        channelInfo.setUserId(sysMessage.getFrom());
        channelInfo.setAppID(sysMessage.getAppID());
        channelInfo.setClientVersion(0x04);

        PseudoChannelImpl pseudoChannel = new PseudoChannelImpl(sysMessage.getTraceID(), channelInfo);

        if (logger.isInfoEnabled()) {
            logger.info("[RpcSystem] send from:{} , to:{} , traceID:{} , cmsgID:{} ", sysMessage.getFrom(), sysMessage.getTo(),
                    sysMessage.getTraceID(), sysMessage.getCmsgID());
        }

        try {
            Map<String, Object> extraMap = packet.getExtra();
            if (null != extra && SystemConstant.SYSTEM_NOTIFY_EXTRAT_TYPE_GROUP.equals(extraMap.get(SystemConstant.SYSTEM_NOTIFY_EXTRA_KEY_TYPE))) {
                groupNotifyHandler.handle(pseudoChannel, packet);
            } else {
                notifyHandler.handle(pseudoChannel, packet);
            }
        } finally {
            behaviourLog.audit(packet);
        }
    }


    @Override
    public void send(SysMessage sysMessage, String[] toUserIDs, String s, String extra) {
        addMDC(sysMessage.getTraceID());

        if (ArrayUtils.isEmpty(toUserIDs)) {
            logger.error("[RpcSystem]  toUserIDs is empty from:{} , traceID:{} ", sysMessage.getFrom(), sysMessage.getTraceID());
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("[RpcSystem] Send  toIDs Begin ===> ");
        }

        for (String to : toUserIDs) {

            SysMessage sm = SysMessageUtil.copy(sysMessage);
            sm.setTo(to);

            doSend(sm, s, extra);

        }

        if (logger.isDebugEnabled()) {
            logger.debug("[RpcSystem] Send  toIDs End ===> ");
        }

    }

    /**
     * Deal V5Protol
     *
     * @param sysMessage
     * @param toUserIDs
     * @param content
     * @param extra
     */
    @Override
    public void sendV5(SysMessage sysMessage, String[] toUserIDs, String content, String extra) {

        V5GenericPacket v5Packet = systemEncoder.encodeV5Packet(sysMessage, toUserIDs, content, extra);

        try {
            ByteBuf byteBuf = decodeAndEncoder.encode(v5Packet);
            byte[] srcMsgBytes = byteBuf.array();
            v5Packet.setSrcMsgBytes(srcMsgBytes);

            V5PacketHead v5PacketHead = v5Packet.getPacketHead();
            String traceId = v5PacketHead.getTraceId();
            if (StringUtils.isBlank(traceId)) {
                traceId = UUID.randomUUID().toString();
            }

            addMDC(traceId);
            String sendId = v5PacketHead.getFrom();

            ChannelInfo channelInfo = new ChannelInfo();
            channelInfo.setUserId(sendId);
            channelInfo.setAppID(v5PacketHead.getAppId());
            channelInfo.setTcpZoneCode(v5PacketHead.getFromRegion());
            Channel channel = new PseudoChannelImpl(v5PacketHead.getTraceId(), channelInfo);

            if (logger.isDebugEnabled()) {
                logger.debug("[RpcSystem] SendV5  toIDs Begin ===> ");
            }


            serviceControllerManager.handler(channel, v5Packet);

            if (logger.isDebugEnabled()) {
                logger.debug("[RpcSystem] SendV5  toIDs End ===> ");
            }


        } catch (Exception e) {
            logger.error("[RpcSystem] sendV5 Error from:{} , traceID:{} ", sysMessage.getFrom(), sysMessage.getTo(), e);
        }
    }


    @Override
    public void sendSimple(SimpleMessage simpleMessage) {

        addMDC(simpleMessage.getTraceID());

        SimpleMessagePacket simpleMessagePacket = simpleEncoder.encodePacket(simpleMessage);
        if (simpleMessage == null) {
            logger.error("[RpcSystem] encode simpleMessage error from:{} , to:{}", simpleMessage.getFrom(), simpleMessage.getTo());
            return;
        }

        if (logger.isInfoEnabled()) {
            logger.info("[RpcSystem] sendSimple from:{} , to:{} , traceID:{} , cmsgID:{} ", simpleMessage.getFrom(), simpleMessage.getTo(),
                    simpleMessage.getTraceID(), simpleMessage.getCmsgID());
        }


        String to = simpleMessage.getTo();
        if (StringUtils.isNotBlank(to)) {
            if (!to.contains(",")) {
                ChannelInfo channelInfo = new ChannelInfo();
                channelInfo.setUserId(simpleMessage.getFrom());
                channelInfo.setAppID(simpleMessage.getAppID());
                channelInfo.setClientVersion(0x04);
                if (simpleMessagePacket != null) {
                    PseudoChannelImpl pseudoChannel = new PseudoChannelImpl(simpleMessage.getTraceID(), channelInfo);
                    defaultSimpleHandler.handle(pseudoChannel, simpleMessagePacket);
                }
            } else {

                String[] toArrays = to.split(",");
                for (String toU : toArrays) {
                    ChannelInfo channelInfo = new ChannelInfo();
                    channelInfo.setUserId(simpleMessage.getFrom());
                    channelInfo.setAppID(simpleMessage.getAppID());
                    channelInfo.setClientVersion(0x04);
                    PseudoChannelImpl pseudoChannel = new PseudoChannelImpl(simpleMessage.getTraceID(), channelInfo);
                    simpleMessagePacket.setToUser(toU);
                    defaultSimpleHandler.handle(pseudoChannel, simpleMessagePacket);
                }
            }
        }


    }

    private void addMDC(String traceId) {

        MDC.put("TraceID", traceId);
        String nodeId = System.getProperty("node.id");
        if (null == nodeId) nodeId = String.valueOf(SystemConstant.NODE_ID_DEFAULT);
        String nodeIp = System.getProperty("node.ip");
        if (null == nodeIp) nodeIp = SystemConstant.NODE_IP_DEFAULT;
        System.setProperty("LOG_PREFIX", String.format("[biz:%s]", nodeId));
        System.setProperty("LOG_FLUME_IP", nodeIp);
        MDC.put("PREFIX", String.format("%s|%s|%s", nodeId, nodeIp, "BizServer"));

    }


}
