package com.handwin.codec;

import io.netty.buffer.ByteBuf;

import com.handwin.packet.BasePacket;
import com.handwin.packet.PacketHead;

public interface PacketDecoder<E extends BasePacket> {
    E decode(ByteBuf buf, PacketHead head);
}
