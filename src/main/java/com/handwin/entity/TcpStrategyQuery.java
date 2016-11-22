package com.handwin.entity;

/**
 * Created by piguangtao on 15/3/20.
 */
public class TcpStrategyQuery {
    private String ip;
    private String contryCode;

    public TcpStrategyQuery buildIp(String ip) {
        this.ip = ip;
        return this;
    }

    public TcpStrategyQuery buildContryCode(String contryCode) {
        this.contryCode = contryCode;
        return this;
    }

    public String getIp() {
        return ip;
    }

    public String getContryCode() {
        return contryCode;
    }

    public boolean isValid() {
        return null != this.ip && !"".equals(this.ip) && null != this.contryCode && !"".equals(this.contryCode);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TcpStrategyQuery{");
        sb.append("ip='").append(ip).append('\'');
        sb.append(", contryCode='").append(contryCode).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
