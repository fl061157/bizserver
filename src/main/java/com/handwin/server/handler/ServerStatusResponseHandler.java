package com.handwin.server.handler;

import com.handwin.packet.MessageResponsePacket;
import com.handwin.server.Channel;
import com.handwin.service.IResendMsgToBizServer;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by piguangtao on 2014/11/26.
 */
@Service
public class ServerStatusResponseHandler extends AbstractHandler<MessageResponsePacket> implements InitializingBean {

    private static Logger logger = LoggerFactory.getLogger(ServerStatusResponseHandler.class);

    public void afterPropertiesSet() throws Exception {
        register(MessageResponsePacket.class);
    }

    @Autowired
    private IResendMsgToBizServer resendMsgToBizServer;

    @Override
    public void handle(Channel channel, MessageResponsePacket packet) {
        final String from = channel.getChannelInfo().getUserId();

        logger.debug("[receive msg from other bizServer].traceId:{},from:{},packet:{}",from, channel.getTraceId(), packet);
        switch (packet.getMessageStatus()){
            case SERVER_RECEIVED:
                String fromIdcCountryCode = packet.getFromIdcCountryCode();
                if(!StringUtils.isBlank(fromIdcCountryCode)){
                    //resendMsgToBizServer.deleteResendMsg(from,packet.getCmsgid(),fromIdcCountryCode);
                }
                else{
                    logger.warn("country code is null, no delete resend msg.from:{},packet:{}",from,packet);
                }
                break;
            default:
                logger.warn("[receive msg from other bizServer].no support. traceId:{},from:{},packet:{}",from, channel.getTraceId(), packet);
        }
    }
}
