package com.handwin.service;

import com.handwin.bean.LiveResponse;
import com.handwin.persist.StatusStore;
import com.handwin.server.controller.livechat.LiveChatJoinController;
import com.handwin.server.proto.ChannelAction;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by fangliang on 16/7/6.
 */
@Service
public class LiveChatService {

    @Inject
    @Qualifier(value = "statusClusterStoreImpl")
    private StatusStore statusStore;


    @Autowired
    @Qualifier("newOnlineSessionService")
    private OnlineStatusService newOnlineStatusService;


    static final String HOSTESS_PREFIX = "hostess_";

    static final String ROOM_TCP_MAPPER_PREFIX = "room_tcp_";

    static final String ROOM_USER_MAPPER_PREFIX = "room_user_";

    static final String TCP_USER_MAPPER_PREFIX = "tcp_user_";

    static final String ROOM_USER_COUNT_PREFIX = "room_user_count_";

    static final String ROOM_USER_INC_COUNT_PREFIX = "room_user_inc_count_";


    private static final Logger logger = LoggerFactory.getLogger(LiveChatJoinController.class);


    public String getHostessID(String roomID) {
        try {
            return statusStore.get(hostnessKey(roomID));
        } catch (Exception e) {
            logger.error("[LiveChatService] getHostessID error roomID:{}", roomID, e);
            return null;
        }
    }

    public boolean addTcpNodeToRoom(String roomID, String tcpNode) throws Exception {
        return statusStore.sAdd(roomTcpKey(roomID), tcpNode);
    }


    public boolean joinRoom(String roomID, String tcpNode, String userID) { //TODO 事务

        boolean addTcp = true;
        try {
            addTcpNodeToRoom(roomID, tcpNode);
        } catch (Exception e) {
            addTcp = false;
            logger.error("[LiveChatService] addTcpNodeToRoom error roomID:{} , tcpNode:{}", roomID, tcpNode);
        }

        if (!addTcp) {
            logger.error("[LiveChat] join  addTcpMapper error from:{} , roomID:{} ", userID, roomID);
            return false;
        }

        try {
            addUserToTcp(tcpNode, userID);
        } catch (Exception e) {
            logger.error("[LiveChatService] addUserToTcp error  tcpNode:{} , userID:{} ", tcpNode, userID);
        }

        try {
            addUserToRoom(roomID, userID);
        } catch (Exception e) {
            logger.error("[LiveChatService] addUserToTcp error  tcpNode:{} , userID:{} ", tcpNode, userID);
        }
        return true;
    }


    public void leaveRoom(String roomID, String tcpNode, String userID) { //TODO 事务

        try {
            deleteUserFromRoom(roomID, userID);
        } catch (Exception e) {
            logger.error("[LiveChatService] leaveRoom deleteUserFromRoom error roomID:{} , userID:{}", roomID, userID);
            return;
        }

        try {
            deleteUserFromTcp(tcpNode, userID);
        } catch (Exception e) {
            logger.error("[LiveChatService] deleteUserFromTcp error tcpNode:{} , userID:{}", tcpNode, userID);
            return;
        }

        int members;

        try {
            members = membersOfTcp(tcpNode);
        } catch (Exception e) {
            logger.error("[LiveChatService] membersOfTcp error tcpNode:{} ", tcpNode);
            return;
        }

        if (members == 0) {
            try {
                deleteTcpNodeFromRoom(roomID, tcpNode);
            } catch (Exception e) {
                logger.error("[LiveChatService] deleteTcpNodeFromRoom error roomID:{} , tcpNode:{}", roomID, tcpNode);
            }
        }

    }


    public void leaveSecondRoom(String roomID, String tcpNode, String userID) { //TODO 事务

        try {
            deleteUserFromTcp(tcpNode, userID);
        } catch (Exception e) {
            logger.error("[LiveChatService] leaveSecondRoom  deleteUserFromTcp error tcpNode:{} , userID:{}", tcpNode, userID);
            return;
        }

        int members;

        try {
            members = membersOfTcp(tcpNode);
        } catch (Exception e) {
            logger.error("[LiveChatService] leaveSecondRoom membersOfTcp error tcpNode:{} ", tcpNode);
            return;
        }

        if (members == 0) {
            try {
                deleteTcpNodeFromRoom(roomID, tcpNode);
            } catch (Exception e) {
                logger.error("[LiveChatService] leaveSecondRoom deleteTcpNodeFromRoom error roomID:{} , tcpNode:{}", roomID, tcpNode);
            }
        }

    }


    protected void deleteTcpNodeFromRoom(String roomID, String tcpNode) throws Exception {
        statusStore.sRem(roomTcpKey(roomID), tcpNode);
    }


    @Autowired
    private UserService userService;

    public Set<String> findTcpNode(String roomID) {
        try {

            Set<String> tcpNodeSet = statusStore.smembers(roomTcpKey(roomID));
            if (CollectionUtils.isNotEmpty(tcpNodeSet)) {
                return tcpNodeSet.stream().filter(tcpNode -> {
                    boolean r = true;
                    try {
                        if (!userService.isLocal(tcpNode)) {
                            r = true;
                        } else {
                            r = newOnlineStatusService.isServerOnline(tcpNode);
                        }
                    } catch (Exception e) {
                    }
                    if (!r) {
                        try {
                            statusStore.sRem(roomTcpKey(roomID), tcpNode);
                        } catch (Exception e) {
                        }
                    }
                    return r;
                }).collect(Collectors.toSet());
            }
            return tcpNodeSet;

        } catch (Exception e) {
            logger.error("[LiveChatService] findTcpNode error roomID:{} ", roomID);
            return null;
        }
    }


    public boolean addUserToTcp(String tcpNode, String userID) throws Exception {
        return statusStore.sAdd(tcpUserKey(tcpNode), userID);
    }

    protected void deleteUserFromTcp(String tcpNode, String userID) throws Exception {
        statusStore.sRem(tcpUserKey(tcpNode), userID);
    }

    protected int membersOfTcp(String tcpNode) throws Exception {
        Set<String> members = statusStore.smembers(tcpUserKey(tcpNode));
        return CollectionUtils.isEmpty(members) ? 0 : members.size();
    }


    protected void addUserToRoom(String roomID, String userID) throws Exception {
        statusStore.sAdd(roomUserKey(roomID), userID);
        statusStore.incr(roomUserCountKey(roomID));
        statusStore.incr( roomUserIncCountKey( roomID ) );
    }


    protected void deleteUserFromRoom(String roomID, String userID) throws Exception {
        if (statusStore.sRem(roomUserKey(roomID), userID)) {
            statusStore.decr(roomUserCountKey(roomID));
        }
    }


    private String roomUserKey(String roomID) {
        return String.format("%s%s", ROOM_USER_MAPPER_PREFIX, roomID);
    }

    private String roomTcpKey(String roomID) {
        return String.format("%s%s", ROOM_TCP_MAPPER_PREFIX, roomID);
    }

    private String tcpUserKey(String tcpNode) {
        return String.format("%s%s", TCP_USER_MAPPER_PREFIX, tcpNode);
    }


    private String hostnessKey(String roomID) {
        return String.format("%s%s", HOSTESS_PREFIX, roomID);
    }


    private String roomUserCountKey(String roomID) {
        return String.format("%s%s", ROOM_USER_COUNT_PREFIX, roomID);
    }

    private String roomUserIncCountKey(String roomID) {
        return String.format("%s%s", ROOM_USER_INC_COUNT_PREFIX, roomID);
    }


}
