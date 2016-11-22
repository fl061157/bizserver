package com.handwin.packet;

public class LoginResponsePacket extends LoginPacket {
    private static final long serialVersionUID = -4058189449413487256L;
    public final static int COMMAND_LOGIN_RESPONSE_PACKET_TYPE = COMMAND_LOGIN_RESPONSE_TYPE * 256 + COMMAND_PACKET_TYPE;

    private String tcpServersJson;

    private String clientIP;

    public LoginResponsePacket() {
        this.setPacketType(COMMAND_LOGIN_RESPONSE_PACKET_TYPE);
    }

    public LoginResponsePacket(LoginStatus loginStatus) {
        this();
        this.setLoginStatus(loginStatus);
    }

    public String getTcpServersJson() {
        return tcpServersJson;
    }

    public String getClientIP() {
        return clientIP;
    }

    public void setClientIP(String clientIP) {
        this.clientIP = clientIP;
    }

    public void setTcpServersJson(String tcpServersJson) {
        this.tcpServersJson = tcpServersJson;
    }
}
