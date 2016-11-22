package com.handwin.persist;


import com.handwin.database.bean.Message;
import com.handwin.exception.ServerException;
import com.handwin.localentity.MessageType;
import com.handwin.localentity.UserLocalMsgCounter;
import com.handwin.message.MessageException;
import com.handwin.message.bean.MessageStatus;
import com.handwin.message.service.MessageService;
import info.archinnov.achilles.type.CounterBuilder;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

/**
 * Created by fangliang on 16/2/15.
 */
@Service("mysqlMessagePersist")
public class MysqlMessagePersist implements MessagePersist {
    private static final Logger LOGGER = LoggerFactory.getLogger(MysqlMessagePersist.class);

    @Autowired
    @Qualifier(value = "rpcMessageService")
    public MessageService rpcMessageService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final Logger logger = LoggerFactory.getLogger(MysqlMessagePersist.class);

    @Override
    public com.handwin.localentity.Message getMessage(String userId, long messageId) throws ServerException {
        try {
            Message mm = rpcMessageService.getMessage(userId, messageId);
            return mm != null ? parse(mm) : null;
        } catch (MessageException e) {
            throw new ServerException(ServerException.ErrorCode.CanNotHandleIoError, e);
        }
    }

    @Override
    public com.handwin.localentity.Message createMessage(String sender, String receiver, com.handwin.localentity.Message message, MessageStatus messageStatus, byte[] content) throws ServerException {
        Message m = parse(message);
        m.setSender(sender);
        m.setStatus(messageStatus != null ? messageStatus.getStatus() : MessageStatus.UNDEAL.getStatus());
        m.setContent(content);
        try {
            rpcMessageService.createMessage(receiver, m);
        } catch (MessageException e) {
            throw new ServerException(ServerException.ErrorCode.CanNotHandleIoError, e);
        }
        message.setId(m.getMessageID());
        message.setContent(content);
        return message;
    }

    @Override
    public void updateMessageStatus(String userID, long messageID, MessageStatus messageStatus) throws ServerException {
        try {
            rpcMessageService.updateMessageStatus(userID, messageID, messageStatus);
        } catch (MessageException e) {
            logger.error("updateMessageStatus error userID:{} , messageID:{} , messageStatus:{} ", userID, messageID, messageStatus.getStatus(), e);
            throw new ServerException(ServerException.ErrorCode.CanNotHandleIoError, e);
        }
    }

    @Override
    public boolean removeMessage(String userId, Long messageId) throws ServerException {
        try {
            rpcMessageService.onlineReceive(userId, messageId);
            return true;
        } catch (MessageException e) {
            logger.error("RemoveMessage error userId:{} , messageId:{}", userId, messageId, e);
        }
        return false;
    }

    @Override
    public UserLocalMsgCounter updateUnreadLocalCount(String userId, boolean isAddOne) throws ServerException {
        try {
            int unReadCount = rpcMessageService.findUnReadCount(userId);
            UserLocalMsgCounter userLocalMsgCounter = new UserLocalMsgCounter();
            userLocalMsgCounter.setCounter(new CounterBuilder().incr(unReadCount));
            return userLocalMsgCounter;
        } catch (MessageException e) {
            throw new ServerException(ServerException.ErrorCode.CanNotHandleIoError, e);
        }
    }


    protected com.handwin.localentity.Message parse(Message m) {
        com.handwin.localentity.Message message = new com.handwin.localentity.Message();
        message.setId(m.getMessageID());
        message.setContent(m.getContent());
        message.setConversationId(m.getConversationID());
        message.setReceiver(m.getUserID());
        message.setCreateTime(m.getCreateTime());
        message.setRoomId(m.getRoomID());
        message.setReceiverType(m.getReceiveType());
        message.setSecret(m.getSecrect());
        message.setSender(m.getSender());
        message.setType(MessageType.findByValue(m.getMessageType()).name());
        message.setIsCount(m.getIsCount());
        if (StringUtils.isNotBlank(m.getMeta())) {
            try {
                message.setMeta(objectMapper.readValue(m.getMeta(), new TypeReference<Map<String, Object>>() {
                }));
            } catch (IOException e) {
                LOGGER.error(String.format("fails to parse message meta. meta:%s", m.getMeta()), e);
            }
        }
        return message;
    }


    protected Message parse(com.handwin.localentity.Message message) {
        Message m = new Message();
        m.setMessageType(MessageType.getMessageType(message.getType()).getValue());
        m.setUserID(message.getReceiver());
        m.setReceiveType(message.getReceiverType());
        m.setMessageID(message.getId());
        m.setSender(message.getSender());
        m.setRoomID(message.getRoomId());
        m.setCreateTime(message.getCreateTime());
        m.setConversationID(message.getConversationId());
        m.setSecrect(message.getSecret());
        m.setIsCount(message.getIsCount());
        if (null != message.getMeta() && message.getMeta().size() > 0) {
            try {
                m.setMeta(objectMapper.writeValueAsString(message.getMeta()));
            } catch (Exception e) {
                LOGGER.error(String.format("fails to parse menssage meta. message: %s", message), e);
            }
        }
        return m;
    }


}
