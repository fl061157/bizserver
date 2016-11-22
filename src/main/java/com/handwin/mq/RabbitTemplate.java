package com.handwin.mq;

import com.handwin.service.UserService;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by fangliang on 3/12/14.
 */
public class RabbitTemplate implements InitializingBean {

    private String region;

    private RabbitAdmin rabbitAdmin;

    @Autowired
    private UserService userService;

    private static final Logger logger = LoggerFactory.getLogger(RabbitTemplate.class);

    Map<String, List<Connection>> connectionMap = new HashMap<>();

    Map<String, List<Channel>> channelMap = new HashMap<>();

    List<Channel> bindThreadChannelList = new ArrayList<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        List<ConnectionWrapper> connections = rabbitAdmin.pubConnection(region);
        if (connections == null || connections.size() == 0) {
            logger.error("PubConnections Empty !");
            throw new RuntimeException("PubConnections Empty !");
        }
        for (ConnectionWrapper cw : connections) {
            List<Connection> cL = connectionMap.get(cw.getAddress());
            if (cL == null) {
                cL = new ArrayList<>();
                connectionMap.put(cw.getAddress(), cL);
            }
            cL.add(cw.getConnection());
        }

        for (Map.Entry<String, List<Connection>> entry : connectionMap.entrySet()) {
            List<Channel> channels = initChannel(entry.getValue(), false);
            if (channels != null && channels.size() > 0) {
                channelMap.put(entry.getKey(), channels);
            }
        }


        //TODO 本区不需要
        if (!userService.isLocalUser(region)) {
            for (Map.Entry<String, List<Connection>> entry : connectionMap.entrySet()) {
                List<Channel> channels = initChannel(entry.getValue(), true);
                if (channels != null && channels.size() > 0) {
                    bindThreadChannelList.addAll(channels);
                }
            }
        }

    }


    private List<Channel> initChannel(List<Connection> publicConnections, boolean confirm) {
        //每个连接均建立发布通道
        if (null == publicConnections || publicConnections.size() < 1) {
            logger.error("no public connection.");
            return null;
        }
        List<Channel> pubChannels = new ArrayList<>();

        publicConnections.stream().forEach((connection) -> {
            int count = getPubChannelCountPerConnection();
            for (int i = 0; i < count; i++) {
                try {
                    Channel channel = connection.createChannel();
                    if (confirm) {
                        channel.confirmSelect();
                    }
                    pubChannels.add(channel);
                } catch (IOException e) {
                    logger.error("fails to create channel.connection:{}", connection.getAddress(), e);
                    continue;
                }
            }
        });
        return pubChannels;
    }

//    private List<Channel> initChannel(List<Connection> publicConnections) {
//        //每个连接均建立发布通道
//        if (null == publicConnections || publicConnections.size() < 1) {
//            logger.error("no public connection.");
//            return null;
//        }
//        List<Channel> pubChannels = new ArrayList<>();
//
//        publicConnections.stream().forEach((connection) -> {
//            int count = getPubChannelCountPerConnection();
//            for (int i = 0; i < count; i++) {
//                try {
//                    Channel channel = connection.createChannel();
//                    pubChannels.add(channel);
//                } catch (IOException e) {
//                    logger.error("fails to create channel.connection:{}", connection.getAddress(), e);
//                    continue;
//                }
//            }
//        });
//        return pubChannels;
//    }

    private int getPubChannelCountPerConnection() {
        int count = rabbitAdmin.getRabbitConnChannelCount() / 4;
        return count > 0 ? count : 1;
    }

    private Channel getRandomPubChannel(List<Channel> pubChannels) {
        int randomNum = ThreadLocalRandom.current().nextInt(0, pubChannels.size());

        Channel channel = null;
        try {
            channel = pubChannels.get(randomNum);
            if (null == channel || !channel.isOpen()) {
                for (int i = 0; i < pubChannels.size(); i++) {
                    channel = pubChannels.get(i);
                    if (channel.isOpen()) {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("fails to get pub channel", e);
        }

        if (null == channel) {
            logger.error("no open pub channel.");
        }

        return channel;
    }

    public void write(String exchange, String routeKey, byte[] message) throws IOException {
        Throwable ioException = null;
        List<Map.Entry<String, List<Channel>>> list = new ArrayList<>(channelMap.entrySet());
        Collections.shuffle(list);
        for (Map.Entry<String, List<Channel>> entry : list) {
            Channel channel = getRandomPubChannel(entry.getValue());
            if (exchange == null) {
                exchange = StringUtils.EMPTY;
            }
            try {
                ioException = null;
                channel.basicPublish(exchange, routeKey, null, message);
                break;
            } catch (Throwable e) {
                logger.error("Channel.Write Address:{} Error ", entry.getKey(), e);
                ioException = e;
            }
        }
        if (ioException != null) {
            throw new IOException(ioException);
        }

    }

    protected String key(String exchange, String routeKey) {
        return String.format("E_%s_R_%s_I_%d", exchange, routeKey, ThreadLocalRandom.current().nextInt(0, getPubChannelCountPerConnection()));
    }


    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public RabbitAdmin getRabbitAdmin() {
        return rabbitAdmin;
    }

    public void setRabbitAdmin(RabbitAdmin rabbitAdmin) {
        this.rabbitAdmin = rabbitAdmin;
    }

    public List<Channel> getBindThreadChannelList() {
        return bindThreadChannelList;
    }



}
