package com.handwin.rabbitmq;


import com.rabbitmq.client.Channel;

/**
 * Created by fangliang on 22/1/15.
 */
public interface RpcListener {

    public void init(String queueName, Channel channel);

}
