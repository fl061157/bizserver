package com.handwin.packet;

public enum MessageStatus {
    SERVER_UNRECEIVED(0),
    SERVER_RECEIVED(1),
    PEER_RECEIVED(2),
    PEER_READ(3),
    ERROR(4),
    UNKNOWN(7),

    REPEAT_SEND(8) ;


    public static final String STATUS = "Status";

    private int id;

    public int id() {
        return this.id;
    }

    MessageStatus(int id) {
        this.id = id;
    }

    public static MessageStatus getInstance(int id) {
        switch (id) {
            case 0:
                return SERVER_UNRECEIVED;
            case 1:
                return SERVER_RECEIVED;
            case 2:
                return PEER_RECEIVED;
            case 3:
                return PEER_READ;
            case 4:
                return ERROR;
            default:
                return UNKNOWN;
        }
    }
}
