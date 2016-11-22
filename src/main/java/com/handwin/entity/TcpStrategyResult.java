package com.handwin.entity;

/**
 * Created by piguangtao on 15/3/20.
 */
public class TcpStrategyResult {
    private String tcpServers;

    public String getTcpServers() {
        return tcpServers;
    }

    public void setTcpServers(String tcpServers) {
        this.tcpServers = tcpServers;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TcpStrategyResult{");
        sb.append("tcpServers='").append(tcpServers).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
