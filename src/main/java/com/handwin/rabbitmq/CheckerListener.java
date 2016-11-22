package com.handwin.rabbitmq;


import com.rabbitmq.client.Channel;
import com.rabbitmq.client.StringRpcServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by fangliang on 21/1/15.
 */

public class CheckerListener implements RpcListener {

    private static final Logger logger = LoggerFactory.getLogger(CheckerListener.class);

    @Override
    public void init(String queueName, Channel channel) {
        try {
            channel.queueDeclare(queueName, true, false, false, null);
        } catch (IOException e) {
            logger.error("declare queue error or already exists  ", e);
        }
        try {
            StringRpcServer server = new StringRpcServer(channel, queueName) {
                @Override
                public String handleStringCall(String request) {
                    return String.format("ECHO:%s", request);
                }
            };
            Thread thread = new Thread(() -> {
                try {
                    server.mainloop();
                } catch (IOException e) {
                    logger.error("StringRpcServer mainLoop error", e);
                }
            });
            thread.start();
        } catch (IOException e) {
            logger.error("StringRpcServer init error ", e);
        }

    }
}
