package com.handwin.persist;

import com.handwin.exception.ServerException;
import com.handwin.localentity.Message;
import com.handwin.localentity.UserLocalMsgCounter;
import com.handwin.message.bean.MessageStatus;

/**
 * @author fangliang
 */
public interface MessagePersist {

    public Message getMessage(String userId, long messageId) throws ServerException;

    public void updateMessageStatus(String userID, long messageID, MessageStatus messageStatus) throws ServerException;

    public Message createMessage(String sender, String receiver, Message message, MessageStatus messageStatus, byte[] content) throws ServerException;

    public boolean removeMessage(String userId, Long messageId) throws ServerException;

    public UserLocalMsgCounter updateUnreadLocalCount(String userId, boolean isAddOne) throws ServerException;

}
