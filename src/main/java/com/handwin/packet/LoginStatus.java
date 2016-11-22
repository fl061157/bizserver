package com.handwin.packet;

/*
#define LOGIN_STATUS_SUCCESS (0x01)
#define LOGIN_STATUS_FAIL (0x02)
#define LOGIN_STATUS_KICKOFF (0x03)
#define LOGIN_STATUS_REDIRECT (0x04)
#define LOGIN_STATUS_REFRESH_TCPSERVER (0x05)
 */
public enum LoginStatus {//TODO 确认
    SUCCESS(1),
    FAIL(2),
    KICKOFF(3),
    REFRESH_TCPSERVER(4),
    REDIRECT(5),
    UNKNOWN(99);

    private int status;

    LoginStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public static LoginStatus getInstance(int status) {
        switch (status) {
            case 1:
                return SUCCESS;
            case 2:
                return FAIL;
            case 3:
                return KICKOFF;
            case 4:
                return REFRESH_TCPSERVER;
            case 5:
                return REDIRECT;

            default:
                return REDIRECT;
        }
    }

}
