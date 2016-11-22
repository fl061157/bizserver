package com.handwin.packet;

/*
 0x01 发起视频通话请求 0x02 接受 0x03 拒绝 0x04 挂断 0x05 对方不在线
0x06 正在通知对方 0x07 对方正忙 0x08 视频辅助呼叫 0x11 发起语音请求 0x18 语音辅助呼叫
 */
public enum CallStatus {
    VIDEO_REQUEST(1),
    VIDEO_ACCEPT(2),
    REJECT(3),
    HANGUP(4),
    PEER_OFFLINE(5),
    RECEIVED(6),
    BUSY(7),
    VIDEO_REQUEST_AGAIN(8),
    CALL_SERVER_RECEIVED(9),
    AUDIO_REQUEST(0x11),
    AUDIO_ACCEPT(0x12),
    AUDIO_REQUEST_AGAIN(0x18),
    ACCEPT_ACK(0x0a),
    GROUP_CALL_HEARTBEAT(0x20),
    UNKNOWN(0xff);

    private int id;

    public int id() {
        return this.id;
    }

    CallStatus(int id) {
        this.id = id;
    }

    public static CallStatus getInstance(int id) {
        switch (id) {
            case 1:
                return VIDEO_REQUEST;
            case 2:
                return VIDEO_ACCEPT;
            case 3:
                return REJECT;
            case 4:
                return HANGUP;
            case 5:
                return PEER_OFFLINE;
            case 6:
                return RECEIVED;
            case 7:
                return BUSY;
            case 8:
                return VIDEO_REQUEST_AGAIN;
            case 9:
                return CALL_SERVER_RECEIVED;
            case 10:
                return ACCEPT_ACK;
            case 0x11:
                return AUDIO_REQUEST;
            case 0x12:
                return AUDIO_ACCEPT;
            case 0x18:
                return AUDIO_REQUEST_AGAIN;
            case 0x20:
                return GROUP_CALL_HEARTBEAT;
            default:
                return UNKNOWN;
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CallStatus{");
        sb.append("id=").append(id);
        sb.append(super.toString());
        sb.append('}');
        return sb.toString();
    }
}
