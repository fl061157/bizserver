package com.handwin.mr;

import cn.v5.mr.MRClient;
import com.handwin.entity.TcpMessage;
import com.handwin.rabbitmq.MessageBuilder;
import com.handwin.service.TransportExecutor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * Created by fangliang on 28/12/15.
 */
public class MrChannel {


    private MRClient mrClient;

    private TransportExecutor transportExecutor;

    private MessageBuilder messageBuilder;

    protected static final Logger logger = LoggerFactory.getLogger(MrChannel.class);


    public MrChannel(MRClient mrClient, TransportExecutor transportExecutor, MessageBuilder messageBuilder) {
        this.mrClient = mrClient;
        this.transportExecutor = transportExecutor;
        this.messageBuilder = messageBuilder;
    }

    public MrChannel(MRClient mrClient) {
        this.mrClient = mrClient;
    }


    public void write(TcpMessage tcpMessage) {

        if (mrClient == null) {
            if (logger.isInfoEnabled()) {
                logger.info("MRClient Is Empty !");
            }
            transportExecutor.add(tcpMessage);
            return;
        }


        byte[] messageBody = tcpMessage.getMessageBody(messageBuilder);

        if (logger.isDebugEnabled()) {
            logger.debug("MrChannel Write Message NodeId:{} , TraceID:{} ", tcpMessage.getNodeID(), tcpMessage.getTraceID());
        }
        String topic = MessageBuilder.cutRouteKey(tcpMessage.getNodeID(), MessageBuilder.ROUTE_KEY_CUT_WORLDS_COUNT);
        final String traceID = MDC.get("TraceID");
        mrClient.pub(topic, messageBody, (status, messageID, message) -> {

            MDC.remove("TraceID");
            if (StringUtils.isNotBlank(traceID)) {
                MDC.put("TraceID", traceID);
            }
            if (status != 0) {
                logger.error("MessageCallback Error MessageID:{} , status:{}  , traceID:{} ", messageID, status, traceID);
            }

        }, 1);

    }


    public static interface FaultCallBack {

        public void call(String topic, byte[] content);

    }

    public void write(String topic, byte[] content, FaultCallBack call) {

        if (mrClient == null) {
            if (logger.isWarnEnabled()) {
                logger.warn("MRClient Is Empty Topic:{} ", topic);
            }
            return;
        }

        mrClient.pub(topic, content, (status, messageID, message) -> {

            MDC.remove("TraceID");

            if (logger.isDebugEnabled()) {
                logger.debug("FaultCallBack messageID:{} , status:{} ", messageID, status);
            }

            if (status != 0) {

                logger.error("MessageCallback Error id:{} , status:{}", messageID, status);
                call.call(topic, content);
            }

        }, 1);


    }


}
