package com.handwin.rabbitmq;


import com.chatgame.protobuf.TcpBiz;
import com.google.protobuf.InvalidProtocolBufferException;
import com.handwin.audit.BehaviourLog;
import com.handwin.codec.PacketCodecs;
import com.handwin.exception.ServerException;
import com.handwin.mq.MessageListener;
import com.handwin.packet.BasePacket;
import com.handwin.packet.MessageResponsePacket;
import com.handwin.server.handler.Handler;
import com.handwin.server.handler.HandlerHolder;
import com.handwin.server.proto.BaseRequestMessage;
import com.handwin.server.proto.FullProtoRequestMessage;
import com.handwin.service.ChannelService;
import com.handwin.utils.SystemConstant;
import io.netty.util.internal.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;


public class TcpMessageHandler extends MessageListener {
    private static final Logger logger = LoggerFactory.getLogger(TcpMessageHandler.class);

    @Autowired
    protected HandlerHolder hodler;

    @Autowired
    protected ChannelService channelService;

    @Autowired
    protected PacketCodecs packetCodecs;

    @Autowired
    protected BehaviourLog behaviourLog;

    public void map(final FullProtoRequestMessage requestMessage) {
    }

    @Override
    public void onMessage(final byte[] message) throws Exception {

        try {
            FullProtoRequestMessage requestMessage = decode(message);
            try {
                addMDCValue(requestMessage.getBaseRequestMessage().getTraceId(), requestMessage);
                map(requestMessage);
                handPacket(requestMessage);
            } finally {
                cleanMDCValue();
            }
        } catch (InvalidProtocolBufferException e) {
            logger.error("parse protobuf error:" + e.getMessage(), e);
        } catch (Throwable e) {
            logger.error("handle error:" + e.getMessage(), e);
        }
    }


    protected void handPacket(final FullProtoRequestMessage requestMessage) {
        Handler handler = hodler.getHandler(requestMessage.getPacket().getClass());
        if (handler != null) {
            com.handwin.server.Channel businessChannel = channelService.buildChannel(requestMessage.getBaseRequestMessage());
            try {
                handler.before(requestMessage.getBaseRequestMessage().getTraceId(), requestMessage.getPacket());
                requestMessage.getPacket().setTraceId(requestMessage.getBaseRequestMessage().getTraceId());
                try {

                    BasePacket basePacket = requestMessage.getPacket();
                    if (businessChannel != null && businessChannel.getChannelInfo() != null &&
                            businessChannel.getChannelInfo().getAppID() > 65535 && basePacket != null &&
                            basePacket.getPacketHead() != null) {
                        basePacket.getPacketHead().setAppId(businessChannel.getChannelInfo().getAppID());
                    }

                    BasePacket packet = requestMessage.getPacket();
                    attachThirdUser(packet, businessChannel);

                    handler.handle(businessChannel, requestMessage.getPacket());
                } catch (Exception e) {
                    logger.error("handPacket error: ", e);
                }
            } finally {
                handler.after(requestMessage.getBaseRequestMessage().getTraceId(), requestMessage.getPacket());
                try {
                    behaviourLog.thirdLoggerCommit(businessChannel, requestMessage);
                } catch (Exception e) {
                }
                behaviourLog.audit(businessChannel, requestMessage);
            }

        } else {
            logger.error("No handler for {}", requestMessage.getPacket().getClass());
        }
    }


    protected FullProtoRequestMessage decode(final byte[] message) throws InvalidProtocolBufferException {
        TcpBiz.Tcp2BizReq protoMessage = TcpBiz.Tcp2BizReq.getDefaultInstance()
                .getParserForType().parseFrom(message);

        byte[] packetBody = protoMessage.getMsgBody().toByteArray();

        BaseRequestMessage request = BaseRequestMessage.build(protoMessage);
        if (logger.isDebugEnabled()) {
            logger.debug("UserID:{}, TraceID:{}, tcp message {}, packet body = {}",
                    request.getUserId(), request.getTraceId(),
                    request, formatPacket(packetBody));
        }

        BasePacket packet = null;
        if (null != packetBody && packetBody.length > 0) {
            packet = packetCodecs.decode(packetBody);
        }

        if (null != packet) {
            packet.setSrcMsgBytes(message);
            request.setClientVersion(packet.getPacketHead().getVersion());
            if (!StringUtils.isEmpty(request.getBackTrackInfo()) && packet instanceof MessageResponsePacket) {
                ((MessageResponsePacket) packet).setFromIdcCountryCode(request.getBackTrackInfo());
            }
        } else {
            logger.warn("fails to parse packet.traceId:{}", request.getTraceId());
            throw new ServerException("Fails to parse packet.");
        }

        FullProtoRequestMessage result = new FullProtoRequestMessage(request, packet, protoMessage);
        if (result.getBaseRequestMessage().getAppId() == 0 && packet.getPacketHead().getAppId() != 0 && packet.getPacketHead().getAppId() < 65535) {
            BaseRequestMessage bm = result.getBaseRequestMessage();
            bm.setAppId(packet.getPacketHead().getAppId());
        }
        return result;
    }


    public static String formatPacket(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(BYTE2HEX[b & 0xff]);
        }
        return sb.toString();
    }

    private static final String[] BYTE2HEX = new String[256];

    static {
        for (int i = 0; i < BYTE2HEX.length; i++) {
            BYTE2HEX[i] = ' ' + StringUtil.byteToHexStringPadded(i);
        }
    }


    private void addMDCValue(String traceId, FullProtoRequestMessage requestMessage) {
        MDC.put("TraceID", traceId);
        String nodeId = System.getProperty("node.id");
        if (null == nodeId) nodeId = String.valueOf(SystemConstant.NODE_ID_DEFAULT);
        String nodeIp = System.getProperty("node.ip");
        if (null == nodeIp) nodeIp = SystemConstant.NODE_IP_DEFAULT;
        System.setProperty("LOG_PREFIX", String.format("[biz:%s]", nodeId));
        System.setProperty("LOG_FLUME_IP", nodeIp);
        MDC.put("PREFIX", String.format("%s|%s|%s", nodeId, nodeIp, "BizServer"));

        if (requestMessage != null) {
            BaseRequestMessage bm = requestMessage.getBaseRequestMessage();
            if (bm != null && StringUtils.isNotBlank(bm.getUserId())) {
                MDC.put("UID", bm.getUserId());
            }
        }

    }

    private void cleanMDCValue() {
        MDC.remove("TraceID");
    }

    public static void main(String[] args) {
        System.out.println(formatPacket(new byte[]{1, 2, 3, 4, 51, 0}));
    }
}
