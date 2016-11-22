package com.handwin.codec;

import com.google.common.collect.Maps;
import com.handwin.exception.ServerException;
import com.handwin.genericmap.GMapDecodeAndEncoder;
import com.handwin.packet.BasePacket;
import com.handwin.packet.GenericPacket;
import com.handwin.packet.PacketHead;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;


@Service
public class PacketCodecs {
    private static Logger logger = LoggerFactory.getLogger(PacketCodecs.class);

    private Map<Integer, BasePacketDecodeAndEncoder<? extends BasePacket>> codecsCache = Maps.newHashMap();

    public void register(Integer packetType, BasePacketDecodeAndEncoder<? extends BasePacket> encoderAndDecoder) {
        logger.info("register encoder/decoder {} for packet type {}", encoderAndDecoder, packetType);
        codecsCache.put(packetType, encoderAndDecoder);
    }

    public Map<Integer, BasePacketDecodeAndEncoder<? extends BasePacket>> codecs() {
        return Collections.unmodifiableMap(codecsCache);
    }

    public BasePacketDecodeAndEncoder getEncoderDecoder(Integer packetType) {
        return codecsCache.get(packetType);
    }

    public BasePacket decode(byte[] content) {
        ByteBuf buf = Unpooled.wrappedBuffer(content);
        try {
            return decode(buf);
        } finally {
            buf.release();
        }
    }

    public BasePacket decode(ByteBuf buf) {
        PacketHead packetHead = decodePacketHead(buf);
        int packetType = packetHead.getPacketType();
        BasePacketDecodeAndEncoder decoderAndEncoder = getEncoderDecoder(packetType);
        BasePacket packet = null;
        if (decoderAndEncoder != null) {
            packet = decoderAndEncoder.decode(buf, packetHead);
        } else {
            logger.error("No decoder for packet type {}", packetType);
        }
        return packet;
    }

    public byte[] encode(int clientVersion, BasePacket packet) {
        if (packet == null) {
            return new byte[]{};
        }
        ByteBuf buf = Unpooled.buffer();
        try {
            encode(clientVersion, packet, buf);
            byte[] result = new byte[buf.readableBytes()];
            buf.readBytes(result);
            return result;
        } finally {
            buf.release();
        }
    }

    public void encode(int clientVersion, BasePacket packet, ByteBuf buf) {
        int type = packet.getPacketType();
        PacketEncoder encoder = getEncoderDecoder(type);
        if (encoder == null) {
            encoder = getEncoderDecoder(type & 0xff);
        }
        if (encoder == null) {
            logger.error("not found encoder for type {}", packet.getPacketType());
            return;
        }
        ByteBuf bodyBuf = Unpooled.buffer();
        try {
            if (packet instanceof GenericPacket && clientVersion < 5){
                logger.error("client version {} too old, can't encode generic packet.", clientVersion);
                return;
            }
            encoder.encode(packet, bodyBuf);
            if (bodyBuf.readableBytes() < 1) {
                logger.error("encoder data is null for type {}, encoder is {}", packet.getPacketType(), encoder);
                return;
            }

            PacketHead head = packet.getPacketHead();
            if (head == null) {
                head = new PacketHead();
                head.setTempId(0);
                head.setAppId((short) 0);
            }
            head.setHead((byte) 0xb7);
            if (head.getVersion() == 0) {
                head.setVersion((byte) 0x03);
            }
            head.setPacketType((byte) packet.getPacketType());
            byte s = (byte) (head.getSecret() & 0x7F);
            if (head.isZip()) {
                s = (byte) (s | 0x80);
            }
            head.setSecret(s);
            head.setPacketSize(bodyBuf.readableBytes());
            if (head.getTimestamp() == 0) {
                head.setTimestamp(System.currentTimeMillis());
            }

            writePacketHead(head, buf);
            buf.writeBytes(bodyBuf);

        } finally {
            bodyBuf.release();
        }

    }

    private void writePacketHead(PacketHead head, ByteBuf buf) {
        buf.writeByte(head.getHead());
        buf.writeByte(head.getVersion());
        buf.writeByte(head.getPacketType());
        if (head.getPacketType() != GenericPacket.GENERIC_PACKET_TYPE) {
            buf.writeByte(head.getSecret());
            buf.writeShort((int) (head.getTimestamp() >> 32));
            buf.writeInt((int) head.getTimestamp());
            buf.writeShort(head.getTempId());
            buf.writeShort(head.getPacketSize());
            buf.writeShort(head.getAppId());
        }else{
            byte s = (byte) (head.getSecret() & 0x01);
            if (head.isZip()) {
                s = (byte) (s | 0x20);
            }
            buf.writeByte(s);
            buf.writeInt(head.getPacketSize());
            buf.writeShort(head.getHeadSize());
        }
    }

    public final PacketHead decodePacketHead(ByteBuf buf) {
        PacketHead head = new PacketHead();
        head.setHead(buf.readByte());
        head.setVersion(buf.readByte());
        head.setPacketType(buf.readByte());
        if (head.getPacketType() != GenericPacket.GENERIC_PACKET_TYPE) {
            byte s = buf.readByte();
            head.setZip((s & 0x80) != 0);
            head.setSecret((byte) (s & 0x7F));
            long timestampPre = buf.readUnsignedShort();
            long g = timestampPre << 32;
            head.setTimestamp(buf.readUnsignedInt() + g);
            head.setTempId(buf.readUnsignedShort());
            head.setPacketSize(buf.readUnsignedShort());
            head.setAppId(buf.readUnsignedShort());
        }else{
            byte s = buf.readByte();
            head.setZip((s & 0x02) != 0);
            head.setSecret((byte) (s & 0x01));
            head.setPacketSize((int) buf.readUnsignedInt());
            head.setHeadSize(buf.readUnsignedShort());
            head.setBodySize(head.getPacketSize() - head.getHeadSize());
        }
        return head;
    }

    public BasePacket decode(ByteBuf buf , PacketHead packetHead) {
        int packetType = packetHead.getPacketType();
        BasePacketDecodeAndEncoder decoderAndEncoder = getEncoderDecoder(packetType);
        BasePacket packet = null;
        if (decoderAndEncoder != null) {
            packet = decoderAndEncoder.decode(buf, packetHead);
        } else {
            logger.error("No decoder for packet type {}", packetType);
        }
        return packet;
    }

}
