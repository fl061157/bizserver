package com.handwin.localentity;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by piguangtao on 14-2-7.
 */
public enum MessageType {
    TEXT(1),
    CARDNAME(2),
    PICURL(3),
    VOICE_TYPE(5),
    VIDEO_TYPE(6),
    SYSTEM_NOTIFY(7),
    VIDEO_CALL(8),
    AUDIO_CALL(9),
    DEFAULT_TYPE(10),
    CMD(11),
    GAME_AUDIO_CALL(12),
    GAME_VIDEO_CALL(13),
    UNKOWN(-99);

    private final int value;

    private MessageType(int value) {
        this.value = value;
    }

    /**
     * Get the integer value of this enum value, as defined in the Thrift IDL.
     */
    public int getValue() {
        return value;
    }

    /**
     * Find a the enum type by its integer value, as defined in the Thrift IDL.
     *
     * @return null if the value is not found.
     */
    public static MessageType findByValue(int value) {
        switch (value) {
            case 1:
                return TEXT;
            case 2:
                return CARDNAME;
            case 3:
                return PICURL;
            case 5:
                return VOICE_TYPE;
            case 6:
                return VIDEO_TYPE;
            case 7:
                return SYSTEM_NOTIFY;
            case 8:
                return VIDEO_CALL;
            case 9:
                return AUDIO_CALL;
            case 11:
                return CMD;
            case 12:
                return GAME_AUDIO_CALL;
            case 13:
                return GAME_VIDEO_CALL;
            default:
                return UNKOWN;
        }
    }


    private static Map<String, MessageType> MAP = new HashMap<String, MessageType>();

    static {
        for (MessageType messageType : MessageType.values()) {
            MAP.put(messageType.name(), messageType);
        }
    }

    public static MessageType getMessageType(String messageTypeName) {
        return MAP.get(messageTypeName);
    }


}
