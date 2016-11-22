package com.handwin.service;

import com.handwin.entity.Group;
import com.handwin.entity.GroupMember;
import com.handwin.entity.User;
import com.handwin.exception.ServerException;

import java.util.List;

public interface GroupService {

    public List<User> findUser(String groupID, Integer appId);

    public List<User> findUserExcludeById(String userID, String groupID, Integer appId) throws ServerException;

    public Group findGroupInfo(String groupId);

    public Group findGroupInfo(String groupId, Integer appID);

    public GroupMember findGroupMemberByKey(String groupId, String userId);

}
