package com.handwin.mq;

import com.rabbitmq.client.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

/**
 * Created by fangliang on 3/12/14.
 */

public class RabbitAdmin implements InitializingBean {

    @Autowired
    private ConnectionFactory connectionFactory;

    private RabbitProperties subRegionProperties;

    @Value("#{configproperties['rabbit.sub.conn.size']}")
    private int subConnectionSize;

    private Map<String, List<RabbitProperties>> pubRegionPropertiesMap;

    //private Connection subConnecion;

    private List<Connection> subConnecionList;

    private ConcurrentHashMap<String, List<ConnectionWrapper>> pubConnectionHolder;

    @Value("#{configproperties['rabbit.conn.thread']}")
    private int rabbitConnThreadCount;

    @Value("#{configproperties['rabbit.conn.max.thread']}")
    private int rabbitConnMaxThreadCount;

    @Value("#{configproperties['rabbit.conn.channel.count']}")
    private int rabbitConnChannelCount;

    @Value("#{configproperties['rabbit.conn.thread.queue.pool.size']}")
    private int rabbitConnThreadQueuePoolSize;

    private final static int DEFAULT_QUEUE_POOL_SIZE = 32;

    private final static int MAX_TRY_CONNECT_TIMES = 100;

    private ExecutorService connExecutor;

    private static final Logger logger = LoggerFactory.getLogger(RabbitAdmin.class);

    @Override
    public void afterPropertiesSet() throws Exception {
        if (subRegionProperties != null) {
            subConnecionList = new ArrayList<>(subConnectionSize);
            connExecutor = new BlockingThreadPoolExecutor(rabbitConnThreadCount, rabbitConnMaxThreadCount, 0L,
                    rabbitConnThreadQueuePoolSize <= 0 ? DEFAULT_QUEUE_POOL_SIZE : rabbitConnThreadQueuePoolSize);
            ConnectionFactory.Params params = ConnectionFactory.Params.build(subRegionProperties.getUserName()
                    , subRegionProperties.getPassword())
                    .buildVirtualHost(ConnectionFactory.DEFAULT_VIRTUAL_HOST)
                    .buildExecutor(connExecutor);
            for (int i = 0; i < subConnectionSize; i++) {
                Connection connection = createConnection(params, subRegionProperties.getAddresses());
                if (connection == null) {
                    logger.error("Create Connection Fatal Error !!!!!!!!!!!!!!!");
                    throw new RuntimeException("Create Connection Fatal Error !!!!!!!!!!!!!!!");
                }
                subConnecionList.add(connection);
            }
        }
        if (pubRegionPropertiesMap != null && pubRegionPropertiesMap.size() > 0) {
            pubConnectionHolder = new ConcurrentHashMap<>();
            pubRegionPropertiesMap.entrySet().stream().forEach(entry -> {

                entry.getValue().stream().forEach(rabbitProperties -> {
                    ConnectionFactory.Params params = ConnectionFactory.Params.build(rabbitProperties.getUserName()
                            , rabbitProperties.getPassword()).buildVirtualHost(ConnectionFactory.DEFAULT_VIRTUAL_HOST);
                    logger.info("connect to mq {} for pub", rabbitProperties.getAddresses());
                    Connection connection = createConnection(params, rabbitProperties.getAddresses());
                    if (connection == null) {
                        logger.error("Create Connection Fatal Error !!!!!!!!!!!!!!!");
                        throw new RuntimeException("Create Connection Fatal Error !!!!!!!!!!!!!!!");
                    }
                    pubConnectionHolder.putIfAbsent(entry.getKey(), new ArrayList<>());
                    pubConnectionHolder.get(entry.getKey()).add(new ConnectionWrapper(rabbitProperties.getAddresses(), connection));
                });
            });
        }
    }


    private Connection createConnection(ConnectionFactory.Params params, String address) {
        Connection connection = null;
        try {
            connection = connectionFactory.createConnection(params, address);
        } catch (IOException e) {
            logger.error("Create Pub Connection Error ! ", e);
            for (int i = 0; i < MAX_TRY_CONNECT_TIMES; i++) {
                try {
                    if (connection != null) {
                        try {
                            connection.close();
                        } catch (IOException e1) {
                            logger.error("Close error: ", e1);
                        }
                    }
                    connection = connectionFactory.createConnection(params, address);
                    break;
                } catch (IOException e1) {
                    logger.error("Create Pub Connection Error ! ", e1);
                    if (connection != null) {
                        try {
                            connection.close();
                        } catch (IOException e2) {
                        }
                        connection = null;
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e2) {
                    }
                }
            }
        }
        return connection;
    }


    public List<ConnectionWrapper> pubConnection(String region) {
        return pubConnectionHolder.get(region);
    }

    public List<Connection> getSubConnecionList() {
        return subConnecionList;
    }

    public Map<String, List<RabbitProperties>> getPubRegionPropertiesMap() {
        return pubRegionPropertiesMap;
    }

    public void setPubRegionPropertiesMap(Map<String, List<RabbitProperties>> pubRegionPropertiesMap) {
        this.pubRegionPropertiesMap = pubRegionPropertiesMap;
    }


    public RabbitProperties getSubRegionProperties() {
        return subRegionProperties;
    }

    public void setSubRegionProperties(RabbitProperties subRegionProperties) {
        this.subRegionProperties = subRegionProperties;
    }

    public int getRabbitConnThreadQueuePoolSize() {
        return rabbitConnThreadQueuePoolSize;
    }

    public int getRabbitConnChannelCount() {
        return rabbitConnChannelCount;
    }

}
