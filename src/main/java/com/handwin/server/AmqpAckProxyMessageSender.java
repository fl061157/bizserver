package com.handwin.server;

import com.handwin.mq.RabbitTemplate;
import com.handwin.packet.BasePacket;
import com.handwin.rabbitmq.ProtocolOutptTemplate;
import com.handwin.rabbitmq.TcpMessageHandler;
import com.handwin.service.UserService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by fangliang on 25/11/15.
 */
@Service
public class AmqpAckProxyMessageSender implements ProxyMessageSender, InitializingBean {


    @Autowired
    private UserService userService;

    @Autowired
    private ProtocolOutptTemplate protocolOutptTemplate;

    @Autowired
    private AmqpQueueConfig amqpQueueConfig;

    private static final Logger logger = LoggerFactory.getLogger(AmqpAckProxyMessageSender.class);

    protected Map<String, BlockingQueue<Prm>> repeatQueueMap = new HashMap<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        Map<String, RabbitTemplate> templates = protocolOutptTemplate.getTemplates();
        if (templates == null || templates.size() == 0) {
            throw new RuntimeException("Template Empty Exception !");
        }
        for (Map.Entry<String, RabbitTemplate> entry : templates.entrySet()) {
            String region = entry.getKey();
            if (!userService.isLocalUser(region)) {
                BlockingQueue<Prm> queue = new LinkedBlockingQueue<>();
                repeatQueueMap.put(region, queue);

                RabbitTemplate rabbitTemplate = entry.getValue();
                List<com.rabbitmq.client.Channel> channelList = rabbitTemplate.getBindThreadChannelList();
                for (com.rabbitmq.client.Channel channel : channelList) {
                    Sender sender = new Sender(region, queue, channel);
                    Thread thread = new Thread(sender, "ProxyThread_" + region + "_" + channel.getChannelNumber());
                    thread.start();
                }
            }

        }

    }

    class Sender implements Runnable {

        private String region;

        private BlockingQueue<Prm> queue;

        private com.rabbitmq.client.Channel channel;

        private volatile boolean run = true;

        public Sender(String region, BlockingQueue<Prm> queue, com.rabbitmq.client.Channel channel) {
            this.region = region;
            this.queue = queue;
            this.channel = channel;
        }

        @Override
        public void run() {
            while (run) {
                try {
                    Prm prm = queue.take();
                    try {
                        String exchange = prm.exchange;
                        if (StringUtils.isBlank(exchange)) {
                            exchange = StringUtils.EMPTY;
                        }

                        if (logger.isDebugEnabled()) {
                            logger.debug("Proxy Send Region:{}  Bytes:{}", this.getRegion(), null != prm.messageBytes ? TcpMessageHandler.formatPacket(prm.messageBytes) : null);
                        }

                        channel.basicPublish(exchange, prm.queue, null, prm.messageBytes);
                        boolean r = channel.waitForConfirms(5000l);
                        if (!r) {
                            throw new Exception("there was message unack!");
                        }
                    } catch (Throwable e) {
                        logger.error("[Proxy Send Error]", e);
                        queue.offer(prm);
                    }

                } catch (InterruptedException e) {
                    run = false;
                }
            }
        }

        public void stop() {
            run = false;
            Thread.currentThread().interrupt();
        }

        public String getRegion() {
            return region;
        }
    }

    protected static class Prm {

        protected String region;

        protected String exchange;

        protected String queue;

        protected byte[] messageBytes;

        public Prm(String region, String exchange, String queue, byte[] messageBytes) {

            this.region = region;
            this.exchange = exchange;
            this.queue = queue;
            this.messageBytes = messageBytes;

        }

    }


    @Override
    public void write(String region, BasePacket basePacket) {

        Prm prm = new Prm(region, amqpQueueConfig.getBiz2bizExchange(),
                amqpQueueConfig.getBiz2bizQueueName(),
                basePacket.getSrcMsgBytes());
        BlockingQueue<Prm> queue = repeatQueueMap.get(region);

        if (queue == null) {

            region = protocolOutptTemplate.getDefaultCountryCode();
            queue = repeatQueueMap.get(region);
            if (queue == null) {
                logger.error("Region {} has no proxy handle", region);
                return;
            }
        }

        try {
            queue.offer(prm);
        } catch (Exception e) {
            logger.error("[Write Proxy Error]", e);
        }

    }

    @Override
    public void write(String region, byte[] messageBytes) {

        Prm prm = new Prm(region, amqpQueueConfig.getBiz2bizExchange(), amqpQueueConfig.getBiz2bizQueueName(), messageBytes);
        BlockingQueue<Prm> queue = repeatQueueMap.get(region);

        if (queue == null) {

            region = protocolOutptTemplate.getDefaultCountryCode();
            queue = repeatQueueMap.get(region);
            if (queue == null) {
                logger.error("Region {} has no proxy handle", region);
                return;
            }
        }

        try {
            queue.offer(prm);
        } catch (Exception e) {
            logger.error("[Write Proxy Error]", e);
        }
    }

    @Override
    public void writeStatus(String region, BasePacket basePacket) {

        Prm prm = new Prm(region, amqpQueueConfig.getBiz2bizExchange(), amqpQueueConfig.getBiz2bizStatusQueueName(), basePacket.getSrcMsgBytes());
        BlockingQueue<Prm> queue = repeatQueueMap.get(region);

        if (queue == null) {

            region = protocolOutptTemplate.getDefaultCountryCode();
            queue = repeatQueueMap.get(region);
            if (queue == null) {
                logger.error("Region {} has no proxy handle", region);
                return;
            }
        }

        try {
            queue.offer(prm);
        } catch (Exception e) {
            logger.error("[Write Proxy Error]", e);
        }

    }

    @Override
    public void writeV5Protocol(String region, byte[] messageBytes) {
        Prm prm = new Prm(region, amqpQueueConfig.getBiz2bizExchange(), amqpQueueConfig.getV5ProtocolQueue(), messageBytes);
        BlockingQueue<Prm> queue = repeatQueueMap.get(region);

        if (queue == null) {

            region = protocolOutptTemplate.getDefaultCountryCode();
            queue = repeatQueueMap.get(region);
            if (queue == null) {
                logger.error("Region {} has no proxy handle", region);
                return;
            }
        }

        try {
            queue.offer(prm);
        } catch (Exception e) {
            logger.error("[Write Proxy Error]", e);
        }
    }
}
