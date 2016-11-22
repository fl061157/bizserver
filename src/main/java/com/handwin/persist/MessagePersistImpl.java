package com.handwin.persist;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;
import com.handwin.exception.ServerException;
import com.handwin.exception.ServerException.ErrorCode;
import com.handwin.localentity.Message;
import com.handwin.localentity.MessageIndex;
import com.handwin.localentity.MessageIndexKey;
import com.handwin.localentity.UserLocalMsgCounter;
import com.handwin.message.bean.MessageStatus;
import info.archinnov.achilles.persistence.PersistenceManager;
import info.archinnov.achilles.type.CounterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("cassendraMessagePersist")
public class MessagePersistImpl implements MessagePersist, InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(MessagePersistImpl.class);

    @Autowired
    @Qualifier("bizEntityManager")
    private PersistenceManager manager;
    private Session session;
    private PreparedStatement statement;
    private PreparedStatement removeStatement;


    @Override
    public void afterPropertiesSet() throws Exception {
        session = manager.getNativeSession();
        statement = session.prepare("INSERT INTO user_messages(user_id,message_id,group_id,content,message) values(?,?,?,?,?)");
        removeStatement = session.prepare("DELETE FROM user_messages WHERE user_id = ? and message_id = ?");
    }

    @Override
    public Message getMessage(String userId, long messageId) throws ServerException {
        MessageIndex mi;
        try {
            mi = manager.find(MessageIndex.class, new MessageIndexKey(userId, messageId));
        } catch (Exception e) {
            throw new ServerException(ErrorCode.CanNotHandleIoError, e);
        }
        Message message = null;
        if (mi != null) {
            message = mi.getMessage();
            if (message != null) {
                message.setContent(mi.getContent());
            }
        }
        return message;
    }

    @Override
    public Message createMessage(String sender, String receiver, Message message, MessageStatus messageStatus,
                                 byte[] content) throws ServerException {
        long messageId = message.getId();
        message.setId(messageId);
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setCreateTime(System.currentTimeMillis());
        MessageIndex index = new MessageIndex();
        index.setId(new MessageIndexKey(receiver, messageId));
        index.setContent(content);
        index.setMessage(message);
        try {
            manager.insert(index);
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            throw new ServerException(ErrorCode.CanNotHandleIoError, e);
        }
        return message;
    }

    @Override
    public boolean removeMessage(String userId, Long messageId) throws ServerException {
        BoundStatement boundStatement = new BoundStatement(removeStatement);
        try {
            session.executeAsync(boundStatement.bind(userId, messageId));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ServerException(ErrorCode.CanNotHandleIoError, e);
        }
        return true;
    }


    @Override
    public void updateMessageStatus(String userID, long messageID, MessageStatus messageStatus) throws ServerException {

    }

    @Override
    public UserLocalMsgCounter updateUnreadLocalCount(String userId,
                                                      boolean isAddOne) throws ServerException {
        UserLocalMsgCounter localMsgCount;
        try {
            localMsgCount = manager.find(UserLocalMsgCounter.class, userId);
            if (null == localMsgCount) {
                localMsgCount = new UserLocalMsgCounter();
                localMsgCount.setUserId(userId);
                localMsgCount.setCounter(CounterBuilder.incr(0));
                localMsgCount = manager.insert(localMsgCount);
            }
            if (isAddOne) {
                localMsgCount.getCounter().incr(1);
                manager.update(localMsgCount);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ServerException(ErrorCode.CanNotHandleIoError, e);
        }
        return localMsgCount;
    }

}
