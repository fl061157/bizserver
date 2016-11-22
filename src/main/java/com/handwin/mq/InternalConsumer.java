package com.handwin.mq;

import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by fangliang on 3/12/14.
 */
public class InternalConsumer extends DefaultConsumer {

    private MessageListener messageListener;

    private static final Logger logger = LoggerFactory.getLogger(InternalConsumer.class);

    public InternalConsumer(Channel channel, MessageListener messageListener) {
        super(channel);
        this.messageListener = messageListener;
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        messageListener.onMessage(this.getChannel(), envelope, body);
    }

    @Override
    public void handleCancel(String consumerTag) throws IOException {
        if (logger.isInfoEnabled()) {
            logger.info("Channel handleCancel consumerTag:{}", consumerTag);
        }
    }

    @Override
    public void handleCancelOk(String consumerTag) {
        if (logger.isInfoEnabled()) {
            logger.info("Channel handleCancelOk consumerTag:{}", consumerTag);
        }
    }


    @Override
    public void handleConsumeOk(String consumerTag) {
        if (logger.isInfoEnabled()) {
            logger.info("Channel handleConsumeOk consumerTag:{}", consumerTag);
        }
    }

    @Override
    public void handleRecoverOk(String consumerTag) {
        if (logger.isInfoEnabled()) {
            logger.info("Channel handleRecoverOk consumerTag:{}", consumerTag);
        }
    }


    @Override
    public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
        if (logger.isInfoEnabled()) {
            logger.info("Channel handleShutdownSignal consumerTag:{}, ShutdownSignalException:{}", consumerTag, sig);
        }
    }

}
