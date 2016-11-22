package com.handwin.mq;

import com.handwin.rabbitmq.RpcListener;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by fangliang on 22/1/15.
 */
public class RabbitRpcContainer implements InitializingBean {

    private RabbitAdmin rabbitAdmin;

    private Map<String, RpcListener> rpcListenerMap;

    private static final Logger logger = LoggerFactory.getLogger(RabbitRpcContainer.class);


    @Override
    public void afterPropertiesSet() throws Exception {
        openRpcServer();
    }


    public void openRpcServer() throws Exception {
        logger.info("open queue rpcServer.");
        List<Connection> subConnectionList = rabbitAdmin.getSubConnecionList();
        if (subConnectionList != null && subConnectionList.size() > 0) {
            subConnectionList.stream().forEach(conn -> {
                try {
                    openRpcServer(conn);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            });
        }
    }

    public void openRpcServer(Connection connection) {
        if (rpcListenerMap == null || rpcListenerMap.size() == 0) {
            logger.error("rpcListenerMap is empty !");
            return;
        }
        rpcListenerMap.entrySet().stream().forEach(entry -> {
            try {
                Channel channel = connection.createChannel();
                entry.getValue().init(entry.getKey(), channel);
            } catch (IOException e) {
                logger.error("Create Channel Error ! ", e);
            }
        });


    }


    public Map<String, RpcListener> getRpcListenerMap() {
        return rpcListenerMap;
    }

    public void setRpcListenerMap(Map<String, RpcListener> rpcListenerMap) {
        this.rpcListenerMap = rpcListenerMap;
    }

    public RabbitAdmin getRabbitAdmin() {
        return rabbitAdmin;
    }

    public void setRabbitAdmin(RabbitAdmin rabbitAdmin) {
        this.rabbitAdmin = rabbitAdmin;
    }
}
