package com.handwin.codec;

import com.handwin.api.sysmsg.bean.SimpleMessage;
import com.handwin.packet.SimpleMessagePacket;
import com.handwin.packet.TextMessagePacket;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;

/**
 * Created by fangliang on 16/6/28.
 */

@Service
public class SimpleEncoder {

    private Charset UTF8 = Charset.forName("UTF-8");

    private static final Logger logger = LoggerFactory.getLogger(SimpleEncoder.class);

    public SimpleMessagePacket encodePacket(SimpleMessage simpleMessage) {

        SimpleMessagePacket simpleMessagePacket = new SimpleMessagePacket();
        simpleMessagePacket.setMessageType(simpleMessage.getMsgType());
        byte msgServerType = simpleMessage.getMsgServerType();
        byte messageServiceType = (byte) (msgServerType & 0x0F);
        byte entityType = (byte) (msgServerType & 0xF0);
        simpleMessagePacket.setMessageServiceType(messageServiceType);
        simpleMessagePacket.setEntityType(entityType);
        simpleMessagePacket.setMsgFlag(simpleMessage.getMsgFlag());
        simpleMessagePacket.setContent(StringUtils.isNotBlank(simpleMessage.getContent()) ?  simpleMessage.getContent().getBytes(UTF8) : null );
        simpleMessagePacket.setFrom(simpleMessage.getFrom());
        if (TextMessagePacket.TO_USER == simpleMessagePacket.getMessageServiceType()) {
            simpleMessagePacket.setToUser(simpleMessage.getTo());
        } else if (TextMessagePacket.TO_GROUP == simpleMessagePacket.getMessageServiceType()) {
            simpleMessagePacket.setFromGroup(simpleMessage.getTo());
            simpleMessagePacket.setToGroup(simpleMessage.getTo());
        } else {
            logger.error("unknown message service type {}", messageServiceType);
            return null;
        }
        simpleMessagePacket.setCmsgid(simpleMessage.getCmsgID());

        simpleMessagePacket.setTempId(0);

        return simpleMessagePacket;

    }

}
