package com.handwin.service;

import com.handwin.entity.Conversation;
import com.handwin.exception.ServerException;

/**
 * Created by fangliang on 10/11/14.
 */
public interface ConversationService {

    public boolean isInBlackSheet(String toUserID, String fromUserID, Integer appId) throws ServerException;

    public boolean isInGreySheet(String toUserID, String fromUserID, Integer appId) throws ServerException;

    public boolean isInBlackOrInGreySheet(String toUserID, String fromUserID, Integer appId) throws ServerException;

    public boolean isNoDisturbForGroup(String userId, String groupId, Integer appId) throws ServerException;

    public Conversation findConversationByEntityId(String userId, String entityId , Integer appId) throws ServerException;

    public void save(String userId, String entityId, int type, Integer appId) throws ServerException;

    public void add(String userId, String entityId, Integer type, Integer appId) throws ServerException;

    public void remove(String userId, String entityId, Integer type, Integer appId) throws ServerException;

}
