package com.handwin.server.handler;

import com.chatgame.protobuf.TcpBiz;
import com.google.protobuf.Descriptors;
import com.google.protobuf.GeneratedMessage;
import com.handwin.rabbitmq.AmqpProtocolOutputTemplate;
import com.handwin.server.proto.ChannelAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.Callable;

/**
 * Created by fangliang on 7/4/15.
 */
@Service
public class BroadcastRefresh implements Callable<Void>, InitializingBean {

    @Value("${localidc.country.code}")
    private String localIDC;

    @Value("${tcp_server.brocast.heart.exchange}")
    private String tcpServerBrocast;


    @Value("${enable.tcp.channel.status.broadcast}")
    private String enableTcpBroadcast;

    @Autowired
    private AmqpProtocolOutputTemplate amqpProtocolOutputTemplate;

    private static final Logger logger = LoggerFactory.getLogger(BroadcastRefresh.class);

    @Override
    public Void call() throws Exception {
        if (logger.isInfoEnabled()) {
            logger.info("Brodcast tcpserver refresh heartbeat  exchange: {} , region:{} ", tcpServerBrocast, localIDC);
        }
        try {
            TcpBiz.Biz2TcpReps.Builder builder = TcpBiz.Biz2TcpReps.getDefaultInstance().newBuilderForType();
            setProtoField(builder, TcpBiz.Biz2TcpReps.ACTIONS_FIELD_NUMBER, Arrays.asList(ChannelAction.BROCAST_HEART.getAction()));
            setProtoField(builder, TcpBiz.Biz2TcpReps.TCPCHANNELUUID_FIELD_NUMBER, UUID.randomUUID().toString());
            amqpProtocolOutputTemplate.send(localIDC, tcpServerBrocast, "", builder.build().toByteArray());
        } catch (Exception e) {
            logger.error("Brodcat message error: ", e);
        }
        return null;
    }


    protected static <T extends GeneratedMessage.Builder> T setProtoField(T protoObjectBuilder,
                                                                          int fieldNumber, Object value) {
        if (value != null) {
            Descriptors.Descriptor descriptor = protoObjectBuilder.getDescriptorForType();
            Descriptors.FieldDescriptor fieldDescriptor = descriptor.findFieldByNumber(fieldNumber);
            protoObjectBuilder.setField(fieldDescriptor, value);
        }
        return protoObjectBuilder;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if("yes".equalsIgnoreCase(enableTcpBroadcast)){
            Thread thread = new Thread() {
                public void run() {
                    try {
                        call();
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            };
            thread.start();
        }
    }
}
