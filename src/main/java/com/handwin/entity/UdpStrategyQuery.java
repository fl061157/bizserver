package com.handwin.entity;

/**
 * Created by piguangtao on 14-3-7.
 */
public class UdpStrategyQuery {

    /**
     * 呼叫发起方
     */
    private String ip1;

    private String ip2;

    private String net1;

    private String net2;

    private Integer appId;


    public String getIp1() {
        return ip1;
    }

    public void setIp1(String ip1) {
        this.ip1 = ip1;
    }

    public String getIp2() {
        return ip2;
    }

    public void setIp2(String ip2) {
        this.ip2 = ip2;
    }

    public String getNet2() {
        return net2;
    }

    public void setNet2(String net2) {
        this.net2 = net2;
    }

    public String getNet1() {
        return net1;
    }

    public void setNet1(String net1) {
        this.net1 = net1;
    }

    public Integer getAppId() {
        return appId;
    }

    public void setAppId(Integer appId) {
        this.appId = appId;
    }

    @Override
    public String toString() {
        return "UdpStrategyQuery{" +
                "ip1='" + ip1 + '\'' +
                ", ip2='" + ip2 + '\'' +
                ", net1='" + net1 + '\'' +
                ", net2='" + net2 + '\'' +
                '}';
    }
}
