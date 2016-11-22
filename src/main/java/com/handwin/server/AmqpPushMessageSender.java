package com.handwin.server;

import com.handwin.entity.PushCallBean;
import com.handwin.entity.PushMsgMqBean;
import com.handwin.entity.PushTextBean;
import com.handwin.rabbitmq.MessageBuilder;
import com.handwin.rabbitmq.ProtocolOutptTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author fangliang
 */
@Service
public class AmqpPushMessageSender implements PushMessageSender {

    private static final Logger logger = LoggerFactory.getLogger(AmqpPushMessageSender.class);

    @Autowired
    private MessageBuilder messageBuilder;

    @Autowired
    private AmqpQueueConfig amqpQueueConfig;

    private final static String DEFAULT_EXCHANGE = "";

    @Autowired
    private ProtocolOutptTemplate protocolOutptTemplate;

    @Override
    public void write(String toRegion,PushMsgMqBean pushMsgMqBean) {
        byte[] pushMessageBody = messageBuilder.buildPushMsgMqBean(pushMsgMqBean);
        write(toRegion,pushMessageBody);
    }

    @Override
    public void write(String toRegion,PushTextBean pushTextBean) {
        final PushMsgMqBean mqBean = messageBuilder.buildPushTextBean(pushTextBean);
        write(toRegion,mqBean);
    }

    @Override
    public void write(String toRegion,PushCallBean pushCallBean) {
        final PushMsgMqBean mqBean = messageBuilder.buildPushMsgMqBean(pushCallBean);
        write(toRegion,mqBean);
    }


    @Override
    public void write(String toRegion,byte[] pushMessageBody) {
        if (pushMessageBody != null && pushMessageBody.length > 0) {
            protocolOutptTemplate.send(toRegion,DEFAULT_EXCHANGE, amqpQueueConfig.getPushQueueName(), pushMessageBody);
        }
    }
}
