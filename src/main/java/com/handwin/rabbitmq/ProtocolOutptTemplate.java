package com.handwin.rabbitmq;

import com.handwin.exception.ServerException;
import com.handwin.mq.RabbitTemplate;

import java.util.Map;

/**
 * @author fangliang
 */
public interface ProtocolOutptTemplate {

    public void send(String region, String exchange, String routeKey, byte[] message) throws ServerException;

    public void send(String exchange, String routeKey, byte[] message) throws ServerException;

    public Map<String, RabbitTemplate> getTemplates();

    public String getDefaultCountryCode();

}
