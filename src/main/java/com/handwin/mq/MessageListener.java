package com.handwin.mq;

import com.codahale.metrics.annotation.Timed;
import com.handwin.metric.IMqMetricFilter;
import com.handwin.packet.BasePacket;
import com.handwin.packet.LoginPacket;
import com.handwin.server.Startup;
import com.handwin.server.handler.LoginHandler;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Envelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by fangliang on 3/12/14.
 */
public abstract class MessageListener {

    private static final Logger logger = LoggerFactory.getLogger(MessageListener.class);

    private static AtomicLong isTaskHanding = new AtomicLong(0);

    @Autowired
    private IMqMetricFilter metricFilter;

    @Timed(name = "mq-message-handle-timer", absolute = true)
    public void onMessage(Channel channel, Envelope envelope, byte[] message) {
        try {
            isTaskHanding.incrementAndGet();
            if (null != metricFilter) metricFilter.before(envelope);

            if (Startup.isShutdown().get()) {
                logger.info("System being shutdown. no handle message.");
                return;
            }

            if (message != null && message.length > 0) {
                onMessage(message);
            } else {
                logger.error("Message is empty !");
            }

        } catch (Throwable throwable) {
            logger.error("onMessage error ", throwable);
        } finally {
            try {
                channel.basicAck(envelope.getDeliveryTag(), false);
            } catch (IOException e) {
                //ignore
            }
            isTaskHanding.decrementAndGet();
            if (null != metricFilter) metricFilter.after(envelope);
        }
    }

    public abstract void onMessage(byte[] message) throws Throwable;

    public static AtomicLong getIsTaskHanding() {
        return isTaskHanding;
    }


    public static void attachThirdUser(BasePacket basePacket, com.handwin.server.Channel channel) {

        Integer appID = 0;
        if (basePacket instanceof LoginPacket) {
            appID = LoginHandler.getAppID((LoginPacket) basePacket);
        } else {
            appID = channel.getChannelInfo().getAppID();
        }

        basePacket.attachThirdUserId(appID);

    }


}
