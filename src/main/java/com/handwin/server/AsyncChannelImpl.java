package com.handwin.server;

import com.handwin.codec.PacketCodecs;
import com.handwin.entity.ChannelInfo;
import com.handwin.entity.TcpMessage;
import com.handwin.entity.User;
import com.handwin.packet.ChannelMode;
import com.handwin.server.proto.BaseRequestMessage;
import com.handwin.server.proto.ChannelAction;
import com.handwin.service.TcpMessageDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by fangliang on 16/5/15.
 */
public class AsyncChannelImpl extends AbstractChannelImpl {


    private TcpMessageDispatcher tcpMessageDispatcher;

    private ChannelStrategy channelStrategy;


    protected static final Logger logger = LoggerFactory.getLogger(AsyncChannelImpl.class);


    public AsyncChannelImpl(BaseRequestMessage baseRequestMessage, PacketCodecs packetCodecs,
                            TcpMessageDispatcher tcpMessageDispatcher, ChannelStrategy channelStrategy) {
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


        this.traceId = baseRequestMessage.getTraceId();
        this.tcpMessageDispatcher = tcpMessageDispatcher;
        this.channelStrategy = channelStrategy;
    }


    public AsyncChannelImpl(User user, ChannelInfo channelInfo, PacketCodecs packetCodecs,
                            TcpMessageDispatcher tcpMessageDispatcher, ChannelStrategy channelStrategy) {
        super(null, packetCodecs);
        this.channelInfo = channelInfo;
        if (user != null) {
            this.channelInfo.setUserId(user.getId());
            this.channelInfo.setUserZoneCode(user.getCountrycode());
            this.channelInfo.setAppID(user.getAppId());
        }
        this.tcpMessageDispatcher = tcpMessageDispatcher;
        this.channelStrategy = channelStrategy;
    }


    public AsyncChannelImpl(ChannelInfo channelInfo, PacketCodecs packetCodecs,
                            TcpMessageDispatcher tcpMessageDispatcher, ChannelStrategy channelStrategy) {
        super(null, packetCodecs);
        this.channelInfo = channelInfo;
        this.tcpMessageDispatcher = tcpMessageDispatcher;
        this.channelStrategy = channelStrategy;
    }


    @Override
    public void write(byte[] packetBody, ChannelAction... tcpActions) {
        write(packetBody, null, tcpActions);
    }

    @Override
    public void write(byte[] packetBody, byte[] extraBody, ChannelAction... tcpActions) {
        TcpMessage tcpMessage = new TcpMessage(this.channelInfo, packetBody, extraBody, tcpActions, this.traceId);
        tcpMessageDispatcher.write(channelStrategy, tcpMessage);
    }

    @Override
    public void close() {
        byte[] packetBody = null;
        byte[] extraBody = null;
        write(packetBody, extraBody, new ChannelAction[]{ChannelAction.CLOSE});
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
