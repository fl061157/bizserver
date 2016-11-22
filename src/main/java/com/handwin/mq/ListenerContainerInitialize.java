package com.handwin.mq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by fangliang on 24/11/15.
 */

@Service
public class ListenerContainerInitialize implements ApplicationListener<ContextRefreshedEvent> {


    public static final Set<RabbitListenerContainer> CONTAINER_SET = new HashSet<>();

    private static final Logger logger = LoggerFactory.getLogger(ListenerContainerInitialize.class);


    private AtomicBoolean start = new AtomicBoolean(false);

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {

        if (start.compareAndSet(false, true)) {

            CONTAINER_SET.stream().forEach(container -> {
                if( logger.isInfoEnabled() ) {
                    logger.info( "[ListenerContainerInitialize open consumer ] rabbitProperties:{} " , container.getRabbitAdmin().getSubRegionProperties() );
                }
                try {
                    container.openQueueConsumer();
                } catch (Exception e) {
                    logger.error("Open Consumer Error ", e);
                }
            });

        }

    }
}
