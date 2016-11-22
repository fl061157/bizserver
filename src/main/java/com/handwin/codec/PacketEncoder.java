package com.handwin.codec;

import io.netty.buffer.ByteBuf;

import com.handwin.packet.BasePacket;

public interface PacketEncoder<E extends BasePacket> {
    void encode(E msg, ByteBuf buf);
}
