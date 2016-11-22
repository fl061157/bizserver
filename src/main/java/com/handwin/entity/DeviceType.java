package com.handwin.entity;

/**
 * Created by piguangtao on 14-1-20.
 */
public enum DeviceType {

    IOS(1),

    ANDRIOD(2);

    int value;

    DeviceType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
