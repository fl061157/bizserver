package com.handwin.server;

import com.handwin.codec.PacketCodecs;
import com.handwin.entity.BizOutputMessage;
import com.handwin.entity.ChannelInfo;
import com.handwin.entity.User;
import com.handwin.exception.ServerException;
import com.handwin.packet.BasePacket;
import com.handwin.packet.ChannelMode;
import com.handwin.rabbitmq.MessageBuilder;
import com.handwin.rabbitmq.ProtocolOutptTemplate;
import com.handwin.server.proto.BaseRequestMessage;
import com.handwin.server.proto.BaseResponseMessage;
import com.handwin.server.proto.ChannelAction;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author fangliang
 */
public class AmqpLocalChannelImpl extends AbstractChannelImpl {

    private static final Logger logger = LoggerFactory.getLogger(AmqpLocalChannelImpl.class);

    private ProtocolOutptTemplate protocolOutptTemplate;

    private MessageBuilder messageBuilder;

    public AmqpLocalChannelImpl(BaseRequestMessage baseRequestMessage,
                                MessageBuilder messageBuilder,
                                ProtocolOutptTemplate protocolOutptTemplate,
                                PacketCodecs packetCodecs) {

        super(baseRequestMessage.getTraceId(), packetCodecs);
        this.channelInfo = new ChannelInfo();
        this.channelInfo.setIp(baseRequestMessage.getUserIp());
        this.channelInfo.setPort(baseRequestMessage.getUserPort());
        this.channelInfo.setNodeId(baseRequestMessage.getTcpServerId());
        this.channelInfo.setUuid(baseRequestMessage.getTcpChannelUuid());
        this.channelInfo.setId(baseRequestMessage.getTcpChannelId());
        this.channelInfo.setTcpZoneCode(baseRequestMessage.getTcpZoneCode());
        this.channelInfo.setChannelMode(ChannelMode.getInstance((byte) baseRequestMessage.getChannelMode()));
        this.channelInfo.setClientVersion(baseRequestMessage.getClientVersion());
        this.channelInfo.setAppID(baseRequestMessage.getAppId());
        this.channelInfo.setSessonId(baseRequestMessage.getSessionId());
        this.channelInfo.setUserId(baseRequestMessage.getUserId());
        this.channelInfo.setUserZoneCode(baseRequestMessage.getUserZoneCode());

        this.channelInfo.setChatRoomID(baseRequestMessage.getChatRoomId());

        this.protocolOutptTemplate = protocolOutptTemplate;

        this.traceId = baseRequestMessage.getTraceId();

        this.messageBuilder = messageBuilder;
    }

    @Override
    public void write(BasePacket packet) {
        super.write(packet);
    }

    public AmqpLocalChannelImpl(User user,
                                ChannelInfo channelInfo,
                                MessageBuilder messageBuilder,
                                ProtocolOutptTemplate protocolOutptTemplate,
                                PacketCodecs packetCodecs) {
        super(null, packetCodecs);
        this.channelInfo = channelInfo;

        if (user != null) {
            this.channelInfo.setUserId(user.getId());
            this.channelInfo.setUserZoneCode(user.getCountrycode());
            this.channelInfo.setAppID(user.getAppId());
        }
        this.messageBuilder = messageBuilder;
        this.protocolOutptTemplate = protocolOutptTemplate;

    }

    @Override
    public void write(byte[] packetBody, ChannelAction... tcpActions) {
        write(packetBody, null, tcpActions);
    }

    @Override
    public void write(byte[] packetBody, byte[] extraBody,
                      ChannelAction... tcpActions) {
        BaseResponseMessage baseResponseMessage = BaseResponseMessage.formResponseMessage(channelInfo, traceId);
        for (ChannelAction tcpAction : tcpActions) {
            baseResponseMessage.addAction(tcpAction);
        }

        BizOutputMessage bizOutputMessage = messageBuilder.buildTcpMessage(channelInfo.getNodeId(),
                baseResponseMessage, packetBody, extraBody);
        try {

            if (StringUtils.isBlank(channelInfo.getTcpZoneCode())) {
                logger.warn("User RegionCode is empty user.id :{} ", channelInfo.getUserId());
                return;
            }
            protocolOutptTemplate.send(channelInfo.getTcpZoneCode(),
                    bizOutputMessage.getExchange(), bizOutputMessage.getRouteKey(), bizOutputMessage.getMessageBody());
        } catch (ServerException e) {
            logger.error("mq send error : " + e.getMessage(), e);
        }
    }


    @Override
    public void close() {
        BaseResponseMessage baseResponseMessage = BaseResponseMessage.formResponseMessage(channelInfo, traceId);
        baseResponseMessage.addAction(ChannelAction.CLOSE);
        BizOutputMessage bizOutputMessage = messageBuilder.buildTcpMessage(channelInfo.getNodeId(),
                baseResponseMessage, null, null);
        try {
            protocolOutptTemplate.send(channelInfo.getTcpZoneCode(),
                    bizOutputMessage.getExchange(), bizOutputMessage.getRouteKey(), bizOutputMessage.getMessageBody());
        } catch (ServerException e) {
            logger.error("mq send error : " + e.getMessage(), e);
        }
    }


    @Override
    public void changeMode(ChannelMode channelMode) {
        this.channelInfo.setChannelMode(channelMode);
    }


    @Override
    public boolean isActive() {
        return true;
    }
}
