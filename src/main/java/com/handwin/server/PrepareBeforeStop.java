package com.handwin.server;

import cn.v5.rpc.cluster.MRClusterConnectionManagerSpring;
import com.handwin.mq.MessageListener;
import com.handwin.mq.RabbitListenerContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by piguangtao on 2014/12/8.
 */
@ManagedResource(objectName = "mq.consumer.operaton:name=openOrCloseConsumer", description = "manage queue consumer")
@Service
public class PrepareBeforeStop {
    private static final Logger logger = LoggerFactory.getLogger(PrepareBeforeStop.class);

    private List<RabbitListenerContainer> listenerContainers = new ArrayList();


    @Autowired
    private MRClusterConnectionManagerSpring mrConnectionManager;

    private long liveTime;

    private static final long LIVE_TIME_AFTER_SHUTDOWN = 2000;

    public void clean() {

        try {
            if (logger.isInfoEnabled()) {
                logger.info("MRConnectionManager Shutdown ====> ");
            }
            mrConnectionManager.shutdown();
        } catch (Exception e) {
            logger.error("", e);
        }

        try {
            Thread.sleep(liveTime);
        } catch (InterruptedException e) {
            logger.warn("fails to sleep.", e);
        }

        //取消每一个mq的consumer
        closeQueueConsumer();

        Startup.isShutdown().set(true);

        if (liveTime == 0) liveTime = LIVE_TIME_AFTER_SHUTDOWN;

        try {
            Thread.sleep(liveTime);
        } catch (InterruptedException e) {
            logger.warn("fails to sleep.", e);
        }

        //等待每一个consumer的消息处理完成
        Long isTaskHandling = MessageListener.getIsTaskHanding().get();

        while (isTaskHandling > 0) {
            logger.info("consumer no over.num:{}", isTaskHandling);
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                logger.warn("fails to sleep.", e);
            }
            isTaskHandling = MessageListener.getIsTaskHanding().get();
        }
    }


    public void setListenerContainers(List<RabbitListenerContainer> listenerContainers) {
        this.listenerContainers = listenerContainers;
    }

    public void setLiveTime(long liveTime) {
        this.liveTime = liveTime;
    }


    @ManagedOperation(description = "close queue consumer")
    public void closeQueueConsumer() {
        listenerContainers.stream().forEach((container) ->
                container.getConsumerChannels().entrySet().stream().forEach(entry -> {
                    logger.debug("begin to cancel consumer. channel:{},consumer:{}", entry.getKey(), entry.getValue());
                    try {
                        entry.getKey().basicCancel(entry.getValue());
                    } catch (IOException e) {
                        logger.warn("fails to concel channel consumer. channel:{},consumer tag:{}", entry.getKey(), entry.getValue(), e);
                    }
                }));
    }

    @ManagedOperation(description = "open queue consumer")
    public void openQueueConsumer() {
        listenerContainers.stream().forEach((container) -> {
                    container.restoreChannelConsumer();
                }
        );
    }
}
