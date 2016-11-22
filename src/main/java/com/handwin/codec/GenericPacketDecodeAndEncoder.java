package com.handwin.codec;

import com.handwin.exception.ServerException;
import com.handwin.packet.GenericPacket;
import com.handwin.packet.PacketHead;
import com.handwin.protocal.v5.codec.V5CodecException;
import com.handwin.protocal.v5.codec.V5GenericPacket;
import com.handwin.protocal.v5.codec.V5GenericPacketDecodeAndEncoder;
import com.handwin.protocal.v5.codec.V5PacketHead;
import com.handwin.utils.UserUtils;
import io.netty.buffer.ByteBuf;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GenericPacketDecodeAndEncoder
        extends BasePacketDecodeAndEncoder<GenericPacket>
        implements InitializingBean {

    private static Logger logger = LoggerFactory.getLogger(GenericPacketDecodeAndEncoder.class);

    @Autowired
    private PacketCodecs packetCodecs;


    private V5GenericPacketDecodeAndEncoder decodeAndEncoder = new V5GenericPacketDecodeAndEncoder();

    public void afterPropertiesSet() throws Exception {
        register();
    }

    @Override
    public void register() {
        packetCodecs.register((int) GenericPacket.GENERIC_PACKET_TYPE, this);
    }

    @Override
    public GenericPacket decode(ByteBuf buf, PacketHead head) {


        GenericPacket p = new GenericPacket();
        p.setPacketHead(head);

        V5PacketHead v5PacketHead = new V5PacketHead();
        v5PacketHead.setVersion(head.getVersion());
        v5PacketHead.setPacketType((byte) head.getPacketType());
        v5PacketHead.setZip((head.getSecret() & 0x02) != 0);
        v5PacketHead.setSecret((byte) (head.getSecret() & 0x01) != 0);
        v5PacketHead.setPacketLen(head.getPacketSize());
        v5PacketHead.setHeadSize(head.getHeadSize());
        V5GenericPacket genericPacket;
        try {
            genericPacket = decodeAndEncoder.decode(buf, v5PacketHead);
        } catch (V5CodecException e) {
            throw new ServerException("fails to parse generic protocal. " + e.getMessage());
        }
        p.setV5GenericPacket(genericPacket);
        return p;
    }

    @Override
    public void encode(GenericPacket msg, ByteBuf bodyBuf) {
        V5GenericPacket v5GenericPacket = msg.getV5GenericPacket();
        if (null == v5GenericPacket) {
            throw new ServerException("no generic protocol body.");
        }
        try {

            if (msg.getV5GenericPacket() != null) {
                V5PacketHead vph = msg.getV5GenericPacket().getPacketHead();
                String from = vph.getFrom();
                vph.setFrom(UserUtils.outThirdUserID(from, vph.getAppId()));

                if (!vph.getService().contains("live")) {
                    String to = vph.getTo();
                    vph.setTo(UserUtils.outThirdUserID(to, vph.getAppId()));
                }
            }

            decodeAndEncoder.encodePacketBody(msg.getV5GenericPacket(), bodyBuf);
            V5PacketHead packetHead = v5GenericPacket.getPacketHead();
            if (null != packetHead) {
                PacketHead packetHead1 = new PacketHead();
                packetHead1.setAppId(packetHead.getAppId());
                packetHead1.setTempId(packetHead.getTempId());
                packetHead1.setHead(packetHead.getHead());
                packetHead1.setVersion(packetHead.getVersion());
                packetHead1.setPacketSize(packetHead.getPacketLen());
                packetHead1.setHeadSize(packetHead.getHeadSize());
                msg.setPacketHead(packetHead1);
            }
        } catch (V5CodecException e) {
            throw new ServerException("fails to encode generic protocal. " + e.getMessage());
        }
    }
}
