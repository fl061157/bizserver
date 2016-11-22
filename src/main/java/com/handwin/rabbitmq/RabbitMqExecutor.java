package com.handwin.rabbitmq;

import com.handwin.entity.BizOutputMessage;
import com.handwin.entity.TcpMessage;
import com.handwin.exception.ServerException;
import com.handwin.service.FailingService;
import com.handwin.service.TransportExecutor;
import com.handwin.utils.RoundRobinList;

/**
 * Created by fangliang on 16/5/15.
 */
public class RabbitMqExecutor extends TransportExecutor {


    private ProtocolOutptTemplate protocolOutptTemplate;

    private MessageBuilder messageBuilder;

    private TransportExecutor transportExecutor;


    public RabbitMqExecutor(ProtocolOutptTemplate protocolOutptTemplate, MessageBuilder messageBuilder,
                            RoundRobinList<TransportExecutor> transportExecutorRound, FailingService failingService) {
        super(messageBuilder, transportExecutorRound, failingService);
        this.messageBuilder = messageBuilder;
        this.protocolOutptTemplate = protocolOutptTemplate;
    }


    @Override
    public void doStart() {

    }

    @Override
    protected void doStop() {

    }

    @Override
    protected boolean request(BizOutputMessage bizOutputMessage) {
        try {
            protocolOutptTemplate.send(bizOutputMessage.getTcpZoneCode(),
                    bizOutputMessage.getExchange(), bizOutputMessage.getRouteKey(), bizOutputMessage.getMessageBody());
        } catch (ServerException e) {
            logger.error("mq send error : " + e.getMessage(), e);
        }
        return true;
    }


    @Override
    protected boolean reQueue(TcpMessage tcpMessage) {
        this.queue.add(tcpMessage);
        return true;
    }

    @Override
    protected boolean heartBeat(byte[] heartBeatFrame) {
        return true;
    }
}
