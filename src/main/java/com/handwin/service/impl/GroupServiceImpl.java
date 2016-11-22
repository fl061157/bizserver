package com.handwin.service.impl;

import com.handwin.entity.Group;
import com.handwin.entity.GroupKey;
import com.handwin.entity.GroupMember;
import com.handwin.entity.User;
import com.handwin.exception.ServerException;
import com.handwin.service.GroupService;
import com.handwin.service.UserService;
import com.handwin.utils.SystemConstant;
import com.handwin.utils.UserUtils;
import info.archinnov.achilles.persistence.PersistenceManager;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by lb on 14/11/6.
 */
@Service
public class GroupServiceImpl implements GroupService {

    public static final int MAX_GROUP_MEMBERS_COUNT = 500;
    private static final Logger logger = LoggerFactory.getLogger(GroupServiceImpl.class);
    @Inject
    @Qualifier("basicEntityManager")
    private PersistenceManager basicManager;

    @Inject
    @Qualifier("opManager")
    private PersistenceManager opManager;


    @Inject
    private UserService userService;

    public List<GroupMember> findMembersByGroupId(String groupId, Integer appId) {
        PersistenceManager realManager = getRealManager(appId);
        String gID = UserUtils.cutGroupID(groupId, appId);
        List<GroupMember> result = realManager
                .sliceQuery(GroupMember.class)
                .forSelect()
                .withPartitionComponents(gID)
                .get(MAX_GROUP_MEMBERS_COUNT);

        return CollectionUtils.isNotEmpty(result) ? result :
                realManager.sliceQuery(GroupMember.class).forSelect().withPartitionComponents(String.format("%s@{%d}", gID, appId)).get(MAX_GROUP_MEMBERS_COUNT);

    }

    public List<User> findGroupUserList(String groupId, final String excludeUserId, final Integer appId) throws ServerException {
        try {
            groupId = UserUtils.cutGroupID(groupId, appId);
            List<GroupMember> members = findMembersByGroupId(groupId, appId);
            if (members == null || members.size() == 0) {
                return null;
            }

            List<String> memberIdList = members.stream()
                    .filter(groupMember -> !groupMember.getId().getUserId().equals(excludeUserId))
                    .map(groupMember -> groupMember.getId().getUserId())
                    .collect(Collectors.toList());

            if (memberIdList == null || memberIdList.size() == 0) {
                return null;
            }
            return userService.findByIdList(memberIdList, appId);
        } catch (Throwable e) {
            throw new ServerException(ServerException.ErrorCode.CanNotHandleIoError, e);
        }
    }


    @Override
    public List<User> findUser(String groupID, Integer appId) {
        return findGroupUserList(groupID, null, appId);
    }

    @Override
    public List<User> findUserExcludeById(String userID, String groupID, Integer appId) {
        return findGroupUserList(groupID, userID, appId);
    }

    public PersistenceManager getRealManager(Integer appId) {
        return appId != null && appId > SystemConstant.CG_MAX_APP_ID ? opManager : basicManager;
    }

    @Override
    public GroupMember findGroupMemberByKey(String groupId, String userId) {
        String gID = UserUtils.cutGroupID(groupId);
        GroupMember gm = basicManager.find(GroupMember.class, new GroupKey(gID, userId));
        return gm != null ? gm :
                basicManager.find(GroupMember.class, new GroupKey(groupId, userId));
    }


    @Override
    public Group findGroupInfo(String groupId) {
        String gID = UserUtils.cutGroupID(groupId);
        Group group = basicManager.find(Group.class, gID);
        return group != null ? group : basicManager.find(Group.class, groupId);
    }

    @Override
    public Group findGroupInfo(String groupId, Integer appID) {
        PersistenceManager manager = getRealManager(appID);
        groupId = UserUtils.cutGroupID(groupId, appID);
        Group group = manager.find(Group.class, groupId);
        return group != null ? group : manager.find(Group.class, String.format("%s@{%d}", groupId, appID));
    }
}
