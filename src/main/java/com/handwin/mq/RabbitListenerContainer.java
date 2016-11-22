package com.handwin.mq;

import com.handwin.listener.CommonMessageListener;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by fangliang on 3/12/14.
 */
public class RabbitListenerContainer implements InitializingBean {

    private Map<String, MessageListener> listenerMap;

    private RabbitAdmin rabbitAdmin;

    private final static boolean DEFAULT_AUTO_ACK = false;


    @Value("#{configproperties['rabbit.qos.count']}")
    private int rabbitQosCount;

    private final static int DEFAULT_BLOCK_SIZE = 1000;

    private final static int DEFAULT_MAX_TRY_TIMES = 100;

    private static final Logger logger = LoggerFactory.getLogger(RabbitListenerContainer.class);

    private Map<Channel, String> consumerChannels = new ConcurrentHashMap<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        //openQueueConsumer();

        ListenerContainerInitialize.CONTAINER_SET.add(this);

    }

    public void openQueueConsumer() throws Exception {
        logger.info("open queue consumer.");
        List<Connection> subConnectionList = rabbitAdmin.getSubConnecionList();
        if (subConnectionList != null && subConnectionList.size() > 0) {
            subConnectionList.stream().forEach(conn -> {
                try {
                    openQueueConsumer(conn);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    throw new RuntimeException(e);
                }
            });
        }
    }

    public void openQueueConsumer(Connection connection) throws Exception {
        if (listenerMap == null || listenerMap.size() == 0) {
            logger.error("MessageListener is empty !");
            throw new RuntimeException("MessageListener is empty !");
        }

        listenerMap.entrySet().stream().forEach(entry -> {
            try {
                for (int i = 0; i < rabbitAdmin.getRabbitConnChannelCount(); i++) {
                    tryChannelConsume(connection, entry.getKey(), (MessageListener)entry.getValue());
                }
            } catch (IOException e) {
                logger.error("Create Channel Error , Shutdown Complete ! ", e);
                throw new RuntimeException("Create Channel Error , Shutdown Complete ! ", e);
            }
        });
    }


    private void tryChannelConsume(Connection connection, String queue, MessageListener messageListener) throws IOException {
        IOException exception = null;
        for (int i = 0; i < DEFAULT_MAX_TRY_TIMES; i++) {
            exception = null;
            try {
                logger.info("consume queue {} at {}:{}", queue, connection.getAddress(), connection.getPort());
                channelConsume(connection, queue, messageListener);
                break;
            } catch (IOException e) {
                logger.error("Channel Consume Error!", e);
                exception = e;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e1) {
                }
            }
        }
        if (exception != null) {
            throw exception;
        }
    }


    private void channelConsume(Connection connection, String queue, MessageListener messageListener) throws IOException {
        Channel channel = null;
        try {
            channel = connection.createChannel();
            if (rabbitQosCount <= 0) {
                rabbitQosCount = DEFAULT_BLOCK_SIZE;
            }
            channel.basicQos(DEFAULT_BLOCK_SIZE, false);
            String consumerTag = channel.basicConsume(queue, DEFAULT_AUTO_ACK, new InternalConsumer(channel, messageListener));
            consumerChannels.put(channel, consumerTag);
        } catch (IOException e) {
            if (channel != null) {
                try {
                    channel.close();
                } catch (IOException e1) {
                    logger.error("Close Channel Error ! ", e1);
                }
            }
            throw e;
        }

    }

    public void restoreChannelConsumer() {
        if (null != consumerChannels && consumerChannels.size() > 0) {
            consumerChannels.entrySet().stream().forEach((entry) -> {
                try {
                    entry.getKey().close();
                } catch (IOException e) {
                    logger.debug("fails to close channle.", e);
                }
            });
        }

        try {
            //openQueueConsumer();
        } catch (Exception e) {
            logger.error("fails to restore channel consumer.", e);
        }

    }

    public Map<String, MessageListener> getListenerMap() {
        return listenerMap;
    }

    public void setListenerMap(Map<String, MessageListener> listenerMap) {
        this.listenerMap = listenerMap;
    }

    public RabbitAdmin getRabbitAdmin() {
        return rabbitAdmin;
    }

    public void setRabbitAdmin(RabbitAdmin rabbitAdmin) {
        this.rabbitAdmin = rabbitAdmin;
    }

    public Map<Channel, String> getConsumerChannels() {
        return consumerChannels;
    }
}
