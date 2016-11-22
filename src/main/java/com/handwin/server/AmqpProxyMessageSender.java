package com.handwin.server;

import com.handwin.exception.ServerException;
import com.handwin.packet.BasePacket;
import com.handwin.rabbitmq.ProtocolOutptTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author fangliang
 */
//@Service
public class AmqpProxyMessageSender implements ProxyMessageSender, InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(AmqpProxyMessageSender.class);

    @Autowired
    private ProtocolOutptTemplate protocolOutptTemplate;

    @Autowired
    private AmqpQueueConfig amqpQueueConfig;

    protected BlockingQueue<Prm> repeatQueue = new LinkedBlockingQueue<>();

    protected volatile boolean status = true;

    private Thread repeatThread = new Thread(() -> {

        while (status && !Thread.currentThread().isInterrupted()) {
            try {
                Prm prm = repeatQueue.take();
                try {
                    protocolOutptTemplate.send(prm.region, prm.exchange, prm.queue, prm.messageBytes);
                } catch (ServerException e) {
                    repeatQueue.offer(prm);
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                status = false;
            }
        }
    });


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
    public void afterPropertiesSet() throws Exception {
        repeatThread.start();
    }

    @Override
    public void write(String region, byte[] messageBytes) {
        try {
            protocolOutptTemplate.send(region,
                    amqpQueueConfig.getBiz2bizExchange(), amqpQueueConfig.getBiz2bizQueueName(), messageBytes);
        } catch (ServerException e) {
            repeatQueue.offer(new Prm(region, amqpQueueConfig.getBiz2bizExchange(), amqpQueueConfig.getBiz2bizQueueName(), messageBytes));
        }
    }

    @Override
    public void write(String region, BasePacket basePacket) {
        try {
            if (null != basePacket.getSrcMsgBytes()) {
                protocolOutptTemplate.send(region,
                        amqpQueueConfig.getBiz2bizExchange(), amqpQueueConfig.getBiz2bizQueueName(), basePacket.getSrcMsgBytes());
            }
        } catch (ServerException e) {
            repeatQueue.offer(new Prm(region, amqpQueueConfig.getBiz2bizExchange(), amqpQueueConfig.getBiz2bizQueueName(), basePacket.getSrcMsgBytes()));
        }
    }

    @Override
    public void writeStatus(String region, BasePacket basePacket) {
        try {
            protocolOutptTemplate.send(region,
                    amqpQueueConfig.getBiz2bizExchange(),
                    amqpQueueConfig.getBiz2bizStatusQueueName(),
                    basePacket.getSrcMsgBytes());
        } catch (ServerException e) {
            repeatQueue.offer(new Prm(region, amqpQueueConfig.getBiz2bizExchange(),
                    amqpQueueConfig.getBiz2bizStatusQueueName(),
                    basePacket.getSrcMsgBytes()));
        }
    }

    @Override
    public void writeV5Protocol(String region, byte[] messageBytes) {
        try {
            protocolOutptTemplate.send(region,
                    amqpQueueConfig.getBiz2bizExchange(), amqpQueueConfig.getV5ProtocolQueue(), messageBytes);
        } catch (ServerException e) {
            repeatQueue.offer(new Prm(region, amqpQueueConfig.getBiz2bizExchange(), amqpQueueConfig.getBiz2bizQueueName(), messageBytes));
        }
    }
}
