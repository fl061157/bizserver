package com.handwin.server;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AmqpQueueConfig {

    @Value("#{configproperties['tcp_server.exchange']}")
    private String tcpExchange;

    @Value("#{configproperties['push.msg.queue']}")
    private String pushQueueName;

    @Value("#{configproperties['biz2robot.queue']}")
    private String bizRobotQueueName;

    @Value("#{configproperties['biz_server.exchange']}")
    private String biz2bizExchange;

    @Value("#{configproperties['biz2biz.queue']}")
    private String biz2bizQueueName;

    @Value("#{configproperties['biz2biz.status.queue']}")
    private String biz2bizStatusQueueName;

    @Value("#{configproperties['default.country.code']}")
    private String defaultCountryCode;

    @Value("#{configproperties['country.codes']}")
    private String countryCode;

    @Value("#{configproperties['serverheart.queue']}")
    private String serverHeartBeatQueue;

    @Value("#{configproperties['v5protocol.queue']}")
    private String v5ProtocolQueue;

    public String getBiz2bizExchange() {
        return biz2bizExchange;
    }

    public String getBiz2bizQueueName() {
        return biz2bizQueueName;
    }

    public String getBiz2bizStatusQueueName() {
        return biz2bizStatusQueueName;
    }

    public String getBizRobotQueueName() {
        return bizRobotQueueName;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public String getDefaultCountryCode() {
        return defaultCountryCode;
    }

    public String getPushQueueName() {
        return pushQueueName;
    }

    public String getServerHeartBeatQueue() {
        return serverHeartBeatQueue;
    }

    public String getTcpExchange() {
        return tcpExchange;
    }

    public String getV5ProtocolQueue() {
        return v5ProtocolQueue;
    }

    public void setV5ProtocolQueue(String v5ProtocolQueue) {
        this.v5ProtocolQueue = v5ProtocolQueue;
    }
}
