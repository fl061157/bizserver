package com.handwin.service;

import com.handwin.rabbitmq.MessageBuilder;
import com.handwin.rabbitmq.ProtocolOutptTemplate;
import com.handwin.rabbitmq.RabbitMqExecutor;
import com.handwin.utils.RoundRobinList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by fangliang on 18/5/15.
 */

@Service
public class TransportManager implements InitializingBean {

    @Autowired
    private ProtocolOutptTemplate protocolOutptTemplate;

    @Autowired
    private MessageBuilder messageBuilder;


    @Autowired
    private FailingService failingService;


    //执行 发送的  RabbitExecutor 线程数
    @Value("${rabbit.send.executor.count}")
    private int rabbitExecutorCount;

    @Value("${country.codes}")
    private String contryCodes;

    private RoundRobinList<TransportExecutor> roundRobinList;

    private ExecutorService exec = Executors.newFixedThreadPool(16); //TODO 单独配置

    private AtomicBoolean init = new AtomicBoolean(false);

    private static final Logger logger = LoggerFactory.getLogger(TransportManager.class);

    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            init();
        } catch (Exception e) {
            logger.error("Init Error ", e);
        }

    }

    protected void init() {
        if (init.compareAndSet(false, true)) {
            // 默认 RabbitMqExecutor 没有下一级 Executor 且 RabbitMqExecutor 为最后默认存在的 Executor
            List<TransportExecutor> executorList = new ArrayList<>();
            if (rabbitExecutorCount == 0) {
                rabbitExecutorCount = 8;
            }
            for (int i = 0; i < rabbitExecutorCount; i++) {
                RabbitMqExecutor rabbitMqExecutor = new RabbitMqExecutor(protocolOutptTemplate, messageBuilder, null, failingService);
                rabbitMqExecutor.start();
                executorList.add(rabbitMqExecutor);
            }
            roundRobinList = new RoundRobinList<>(executorList);
        }

    }

    public RabbitMqExecutor getRabbitMqExecutor() {
        try {
            return (RabbitMqExecutor) roundRobinList.get();
        } catch (Exception e) {
            logger.error("Get RabbitMqExecutor Null !");
            return null;
        }
    }


    private boolean isLocalMrMq(String nodeID) {

        if (contryCodes.equals("0086")) {
            return nodeID.contains("c_cn_tcp");
        } else if (contryCodes.equals("0001")) {
            return nodeID.contains("c_us_tcp") || !nodeID.contains("c_cn_tcp");
        }
        return true;
    }


}
