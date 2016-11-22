package com.handwin.codec;

import com.handwin.packet.BasePacket;

public abstract class BasePacketDecodeAndEncoder<E extends BasePacket> implements PacketDecoder<E> , PacketEncoder<E> {
	
	protected static final int SIZE_SHORT = 2 ;
	protected static final int SIZE_INT = 4 ;
	protected static final int SIZE_BYTE = 1 ;
	protected static final int SIZE_LONG = 8 ;

	public abstract void register();

}
