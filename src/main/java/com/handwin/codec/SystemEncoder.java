package com.handwin.codec;

import com.alibaba.fastjson.JSON;
import com.chatgame.protobuf.TcpBiz;
import com.google.protobuf.ByteString;
import com.handwin.api.sysmsg.bean.SysMessage;
import com.handwin.packet.PacketHead;
import com.handwin.packet.SystemNotifyPacket;
import com.handwin.protocal.v5.codec.V5GenericPacket;
import com.handwin.protocal.v5.codec.V5PacketHead;
import com.handwin.server.handler.GroupSystemNotifyFromCoreServerHandler;
import com.handwin.utils.MessageUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;


/**
 * Created by fangliang on 16/6/12.
 */
@Service
public class SystemEncoder {


    @Autowired
    private MessageUtils messageUtils;

    @Autowired
    private PacketCodecs packetCodecs;

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemEncoder.class);

    static final Charset UTF8 = Charset.forName("UTF-8");

    public SystemNotifyPacket encodePacket(SysMessage sysMessage, String messageBody, String extra) {

        PacketHead packetHead = new PacketHead();
        packetHead.setPacketType(SystemNotifyPacket.SYSTEM_NOTIFY_PACKAGE_TYPE);
        packetHead.setAppId(sysMessage.getAppID());
        packetHead.setPushContent(sysMessage.getPushContent());
        packetHead.setPush(sysMessage.isPush());
        packetHead.setServerReceivedConfirm(sysMessage.isAck());
        packetHead.setFrom(sysMessage.getFrom());
        packetHead.setTo(sysMessage.getTo());
        packetHead.setMessageID(sysMessage.getCmsgID());

        SystemNotifyPacket systemNotifyPacket = new SystemNotifyPacket();
        systemNotifyPacket.setPacketHead(packetHead);
        systemNotifyPacket.setFrom(sysMessage.getFrom());
        systemNotifyPacket.setTo(sysMessage.getTo());
        systemNotifyPacket.setTraceId(sysMessage.getTraceID());

        if (StringUtils.isNotBlank(sysMessage.getCmsgID())) {
            systemNotifyPacket.setCmsgId(sysMessage.getCmsgID());
        } else {
            systemNotifyPacket.setCmsgId(UUID.randomUUID().toString());
        }

        systemNotifyPacket.setPacketHead(packetHead);

        if (StringUtils.isNotBlank(extra)) {
            try {
                systemNotifyPacket.setExtra(JSON.parseObject(extra, Map.class));
            } catch (Exception e) {
            }
        }

        systemNotifyPacket.setMsgType(sysMessage.getMsgType());
        byte serveType = 0;

        if (sysMessage.isAck()) {
            serveType |= SystemNotifyPacket.SERVICE_TYPE_NEED_ACK;
        }

        if (sysMessage.isIncrOfflineCount()) {
            serveType |= SystemNotifyPacket.SERVICE_TYPE_UNREAD_INCREASE;
        }

        if (sysMessage.isPush()) {
            serveType |= SystemNotifyPacket.SERVICE_TYPE_NEED_PUSH;
        }

        serveType |= SystemNotifyPacket.SERVICE_TYPE_NEED_SAVE;

        if (sysMessage.isGroup()) {
            serveType |= SystemNotifyPacket.SERVICE_TYPE_GROUP;
        }

        if (sysMessage.isStore()) {
            serveType |= SystemNotifyPacket.SERVICE_TYPE_NEED_SAVE;
        }

        if (sysMessage.isReplyRead()) {
            serveType |= SystemNotifyPacket.SERVICE_TYPE_NEED_READ;
        }

        if (sysMessage.isIncreadByOneFromPush()) {
            serveType |= SystemNotifyPacket.SERVICE_TYPE_UNREAD_INCREASE;
        }


        systemNotifyPacket.setServeType(serveType);

        if (StringUtils.isNotBlank(sysMessage.getPushContent())) {
            systemNotifyPacket.setPushContentLength(sysMessage.getPushContent().getBytes(UTF8).length);
            systemNotifyPacket.setPushContentBody(sysMessage.getPushContent());
            systemNotifyPacket.setPushContentTemplate(messageUtils.generateRichMessage(sysMessage.getPushContent()));
        }

        systemNotifyPacket.setMessageBody(messageBody);
        systemNotifyPacket.setMesssageLength(messageBody.getBytes(UTF8).length);

        try {
            byte[] data = packetCodecs.encode(0, systemNotifyPacket);
            TcpBiz.Tcp2BizReq.Builder builder = TcpBiz.Tcp2BizReq.newBuilder();
            builder.setSource(TcpBiz.SourceType.CORE_SERVER).setAppId(sysMessage.getAppID()).setUserId(sysMessage.getFrom()).setTraceId(sysMessage.getTraceID()).setMsgBody(ByteString.copyFrom(data));
            byte[] srcMsgBytes = builder.build().toByteArray();
            systemNotifyPacket.setSrcMsgBytes(srcMsgBytes);
        } catch (Exception e) {
            LOGGER.error("[SystemEncoder] error from:{} , to:{} , traceID:{}", sysMessage.getFrom(), sysMessage.getTo(), sysMessage.getTraceID(), e);
        }

        return systemNotifyPacket;

    }


    /**
     * @param sysMessage
     * @param toUserIDs   TODO 兼容以前, 不太合理
     * @param messageBody
     * @param extra
     * @return
     */
    public V5GenericPacket encodeV5Packet(SysMessage sysMessage, String[] toUserIDs, String messageBody, String extra) {
        V5GenericPacket v5GenericPacket = new V5GenericPacket();
        V5PacketHead v5PacketHead = new V5PacketHead();
        v5GenericPacket.setPacketHead(v5PacketHead);
        v5PacketHead.setTimestamp(System.currentTimeMillis());
        v5PacketHead.setAppId(sysMessage.getAppID());
        v5PacketHead.setFrom(sysMessage.getFrom());
        String to; //TODO
        if (ArrayUtils.isEmpty(toUserIDs)) {
            to = sysMessage.getTo();
        } else {
            to = StringUtils.join(toUserIDs, ",");
        }
        v5PacketHead.setTo(to);
        v5PacketHead.setFromRegion(sysMessage.getFormRegion());
        v5PacketHead.setToRegion(sysMessage.getToRegion());
        v5PacketHead.setVia("coreServer"); //TODO
        v5PacketHead.setServerReceivedConfirm(sysMessage.isServerReceiveConfirm());
        v5PacketHead.setStore(sysMessage.isStore());
        v5PacketHead.setPush(sysMessage.isPush());
        v5PacketHead.setPushContent(sysMessage.getPushContent());
        v5PacketHead.setPushIncr(sysMessage.isIncrOfflineCount());
        v5PacketHead.setClientReceivedConfirm(sysMessage.isStore());
        v5PacketHead.setEnsureArrive(sysMessage.isEnsureArrive());
        v5PacketHead.setMessageID(sysMessage.getCmsgID());
        v5PacketHead.setTraceId(sysMessage.getTraceID());
        v5PacketHead.setService(sysMessage.getService());
        if (StringUtils.isNotBlank(extra)) {
            Map<String, Object> extraMap = JSON.parseObject(extra, Map.class);
            extraMap.entrySet().stream().forEach(e -> v5PacketHead.addHead(e.getKey(), e.getValue()));
        }
        v5GenericPacket.setBodySrcBytes(messageBody.getBytes(StandardCharsets.UTF_8));
        return v5GenericPacket;
    }

}
