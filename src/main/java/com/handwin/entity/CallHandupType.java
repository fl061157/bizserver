package com.handwin.entity;

/**
 * Created by piguangtao on 2014/6/19.
 */
public enum CallHandupType {
    /**
     * 对方未接通挂断
     * 呼叫方在对方未接通时，超时挂断
     */
    OPPOSITE_UN_ACCECPT_OVERTIME((byte) 0x01),

    OPPOSITE_ACCECPTED((byte) 0x02),

    /**
     * 对方未接通挂断
     * 呼叫方在对方未接通时，未等到超时主动挂断（客户端做提示用）
     */
    OPPOSITE_UN_ACCECPT_ACTIVE((byte) 0x03);

    private byte value;

    CallHandupType(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }

}
