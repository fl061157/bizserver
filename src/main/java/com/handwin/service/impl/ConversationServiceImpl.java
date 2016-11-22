package com.handwin.service.impl;

import com.handwin.entity.Conversation;
import com.handwin.entity.ConversationKey;
import com.handwin.exception.ServerException;
import com.handwin.service.ConversationService;
import com.handwin.utils.SystemConstant;
import com.handwin.utils.UserUtils;
import info.archinnov.achilles.persistence.PersistenceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.inject.Inject;


@Service
public class ConversationServiceImpl implements ConversationService {

    private static final Logger logger = LoggerFactory.getLogger(ConversationServiceImpl.class);

    @Autowired
    @Qualifier("basicEntityManager")
    private PersistenceManager manager;

    @Inject
    @Qualifier("opManager")
    private PersistenceManager opManager;

    /**
     * 黑名单
     *
     * @param toUserID
     * @param fromUserID
     * @return
     */
    public boolean isInBlackSheet(String toUserID, String fromUserID, Integer appId) throws ServerException {
        Conversation converSation = findConversationByEntityId(toUserID, fromUserID, appId);
        return converSation != null && UserUtils.isInBlackList((byte) converSation.getType());
    }
    
    /**
     * 灰名单
     *
     * @param toUserID
     * @param fromUserID
     * @return
     */
    public boolean isInGreySheet(String toUserID, String fromUserID, Integer appId) throws ServerException {
        Conversation converSation = findConversationByEntityId(toUserID, fromUserID, appId);
        return converSation != null && UserUtils.isInGreyList((byte) converSation.getType());
    }

    public boolean isInBlackOrInGreySheet(String toUserID, String fromUserID, Integer appId) throws ServerException {
        Conversation converSation = findConversationByEntityId(toUserID, fromUserID, appId);
        return converSation != null
                && (UserUtils.isInBlackList((byte) converSation.getType())
                || UserUtils.isInGreyList((byte) converSation.getType()));
    }

    @Override
    public boolean isNoDisturbForGroup(String userId, String groupId , Integer appId) throws ServerException {
        boolean result = false;
        Conversation converSation = findConversationByEntityId(userId, groupId, appId );
        if (null != converSation) {
            if (((byte) converSation.getType() & SystemConstant.GROUP_CONVERSION_NO_DISTURB_FLAG) == SystemConstant.GROUP_CONVERSION_NO_DISTURB_FLAG) {
                result = true;
            }
        }

        return result;
    }


    public Conversation findConversationByEntityId(String userId, String entityId, Integer appId) throws ServerException {
        PersistenceManager realManager = getRealManager(appId);
        try {
            Conversation conversation = realManager.find(Conversation.class, new ConversationKey(userId, entityId));
            return conversation;
        } catch (Exception e) {
            throw new ServerException(ServerException.ErrorCode.CanNotHandleIoError, e);
        }
    }

    public void save(String userId, String entityId, int type, Integer appId) throws ServerException {
        PersistenceManager realManager = getRealManager(appId);
        Conversation c = new Conversation();
        c.setId(new ConversationKey(userId, entityId));
        c.setType(type);
        c.setCreateTime(System.currentTimeMillis());
        try {
            realManager.insert(c);
        } catch (Exception e) {
            throw new ServerException(ServerException.ErrorCode.CanNotHandleIoError, e);
        }
    }

    public void add(String userId, String entityId, Integer type, Integer appId) throws ServerException {
        Conversation conversation = findConversationByEntityId(userId, entityId, appId);
        if (conversation != null) {
            type = conversation.getType() | type;
        }
        save(userId, entityId, type, appId);
    }

    public void remove(String userId, String entityId, Integer type, Integer appId) {
        PersistenceManager realManager = getRealManager(appId);
        Conversation conversation = findConversationByEntityId(userId, entityId, appId);
        if (conversation != null) {
            int result = conversation.getType() & (~type);
            if (result == 0) {
                try {
                    realManager.deleteById(Conversation.class, conversation.getId());
                } catch (Exception e) {
                    throw new ServerException(ServerException.ErrorCode.CanNotHandleIoError, e);
                }
            } else {
                save(userId, entityId, result, appId);
            }
        }
    }

    public PersistenceManager getRealManager(Integer appId) {
        return appId != null && appId > SystemConstant.CG_MAX_APP_ID ? opManager : manager;
    }


}
