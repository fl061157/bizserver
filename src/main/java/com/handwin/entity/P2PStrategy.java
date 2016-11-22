package com.handwin.entity;

/**
 * Created by piguangtao on 14-1-17.
 */
public enum P2PStrategy {
    ALL_NO_P2P((byte) 0X00),

    AUDIO_P2P((byte) 0x01),

    VIDEO_P2P((byte) 0x10),

    ALL_P2P((byte) 0x11);

    private byte value;


    P2PStrategy(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }
}
