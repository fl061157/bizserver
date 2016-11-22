package com.handwin.service;

import cn.v5.mr.MRClient;
import cn.v5.rpc.cluster.MRClusterMapClientManager;
import com.handwin.entity.ChannelInfo;
import com.handwin.entity.TcpMessage;
import com.handwin.mr.MrChannel;
import com.handwin.rabbitmq.MessageBuilder;
import com.handwin.server.ChannelStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by fangliang on 16/5/15.
 */

@Service
public class TcpMessageDispatcher {

    @Autowired
    private TransportManager transportManager;

    @Autowired
    private MessageBuilder messageBuilder;

    @Autowired
    private MRClusterMapClientManager mrClusterMapClientManager;


    @Autowired
    private UserService userService;

    protected static final Logger logger = LoggerFactory.getLogger(TcpMessageDispatcher.class);


    public void write(ChannelStrategy channelStrategy, TcpMessage tcpMessage) {


        String rc = "0001";
        String nodeID = tcpMessage.getNodeID();
        if (nodeID.contains("c_cn")) {
            rc = "0086";
        }
        boolean isLocal = userService.isLocalUser(rc);


        if (logger.isInfoEnabled()) {
            ChannelInfo cInfo = tcpMessage.getChannelInfo();
            try {
                logger.info("[WRITE] to.user:{} , traceID:{} , strategy:{} , node:{} , channelUUID:{} , countryCode:{} ", cInfo.getUserId(),
                        tcpMessage.getTraceID(), channelStrategy.name(), tcpMessage.getNodeID(), cInfo.getUuid(), cInfo.getUserZoneCode());
            } catch (Exception e) {
            }
        }

        if (channelStrategy == ChannelStrategy.MrChannel && isLocal) {

            MRClient mrClient = mrClusterMapClientManager.find(tcpMessage.getNodeID());

            if (mrClient != null) {

                MrChannel mrChannel = new MrChannel(mrClient, transportManager.getRabbitMqExecutor(), messageBuilder);
                mrChannel.write(tcpMessage);
                return;

            } else {
                logger.error("MRClient List Is Empty NodeID:{} , TraceID:{} ", tcpMessage.getNodeID(), tcpMessage.getTraceID());
            }
        }

        TransportExecutor transportExecutor = transportManager.getRabbitMqExecutor();
        transportExecutor.add(tcpMessage);
    }


}
