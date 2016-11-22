package com.handwin.bean;

/**
 * Created by piguangtao on 15/11/13.
 */
public class UDPServer {
    private String ip;
    private Integer port;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UDPServer{");
        sb.append("ip='").append(ip).append('\'');
        sb.append(", port=").append(port);
        sb.append('}');
        return sb.toString();
    }
}
