package com.handwin.server;

import com.handwin.codec.PacketCodecs;
import com.handwin.entity.ChannelInfo;
import com.handwin.packet.BasePacket;
import com.handwin.packet.ChannelMode;
import com.handwin.packet.GenericPacket;
import com.handwin.packet.PacketHead;
import com.handwin.server.proto.ChannelAction;
import com.handwin.utils.SystemConstant;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author fangliang
 */
public abstract class AbstractChannelImpl implements Channel {

    private static final Logger logger = LoggerFactory.getLogger(AbstractChannelImpl.class);

    protected boolean isLocalUser = true;
    protected String traceId;
    protected ChannelInfo channelInfo;
    private PacketCodecs packetCodecs;

    public AbstractChannelImpl(String traceId, PacketCodecs packetCodecs) {
        this.traceId = traceId;
        this.packetCodecs = packetCodecs;
    }

    @Override
    public void write(BasePacket packet) {
        write(packet, (byte[]) null, ChannelAction.SEND);
    }

    @Override
    public void write(BasePacket packet, ChannelAction... tcpActions) {
        write(packet, null, tcpActions);
    }


    @Override
    public void write(BasePacket packet, byte[] extraBody,
                      ChannelAction... tcpActions) {

        PacketHead packetHead = packet.getPacketHead();
        if (packetHead == null && !(packet instanceof GenericPacket)) {
            packetHead = new PacketHead();
            packet.setPacketHead(packetHead);
        }

        if (!(packet instanceof GenericPacket)) {
            packetHead.setAppId(channelInfo.getAppID() < SystemConstant.CG_MAX_APP_ID ? channelInfo.getAppID() : 65535);
            packetHead.setVersion((byte) channelInfo.getClientVersion());
        }

        byte[] packetBody = packetCodecs.encode(channelInfo.getClientVersion(), packet);
        if (StringUtils.isBlank(traceId) && StringUtils.isNotBlank(packet.getTraceId())) {
            if (logger.isDebugEnabled()) {
                logger.debug("Append traceId toChannel traceId:{} , channelUUID:{}", packet.getTraceId(), channelInfo.getUuid());
            }
            setTraceId(packet.getTraceId());
        }
        write(packetBody, extraBody, tcpActions);
    }

    public abstract void write(byte[] packetBody, ChannelAction... tcpActions);

    public abstract void write(byte[] packetBody, byte[] extraBody, ChannelAction... tcpActions);

    @Override
    public abstract void close();

    @Override
    public abstract void changeMode(ChannelMode channelMode);


    public void setLocalUser(boolean isLocalUser) {
        this.isLocalUser = isLocalUser;
    }


    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }


    public ChannelInfo getChannelInfo() {
        return channelInfo;
    }

    public void setChannelInfo(ChannelInfo channelInfo) {
        this.channelInfo = channelInfo;
    }

    @Override
    public String getTraceId() {
        return traceId;
    }

    @Override
    public String getIp() {
        return channelInfo == null ? null : channelInfo.getIp();
    }

    @Override
    public int getPort() {
        return channelInfo == null ? 0 : channelInfo.getPort();
    }

}
