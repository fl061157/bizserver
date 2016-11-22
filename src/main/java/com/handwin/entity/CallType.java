package com.handwin.entity;

/**
 * Created by piguangtao on 14-1-17.
 */
public enum CallType {
    AUDIO((byte) 0x01),

    VIDEO((byte) 0x02);

    private byte value;

    CallType(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }
}
