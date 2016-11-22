package com.handwin.service;

import com.handwin.entity.*;

import java.util.List;

/**
 * @author fangliang
 */
public interface UserService {

    public User authorize(String sessionId, Integer appId);

    public void updateUserLocalTimezone(User user, String locale, String timeZone);

    public void login(User user, int appId, String channelId, String nodeId, String ip, String locale, String timeZone);

    public User findById(String id, Integer appId);

    public User loadById(String id, Integer appId);

    public List<User> findByIdList(List<String> idList);

    public List<User> findByIdList(List<String> idList, Integer appID);

    public UserToken getTokenInfo(String user_id, Integer app_id);

    public MobileIndex findMobileIndex(String mobile, String countrycode);

    public String getFriendNickname(User user, String friendId, int appId);

    public boolean isSystemAccount(User user);

    public boolean isLocalUser(String countryCode);

    public boolean isLocal(String tcpNode);

    public UserBehaviouAttr getUserBehaviouAttr(String userId, Integer appId);

    public UserSessionIndex findUserSessionIndexByKey(String user_id, Integer app_id);

    public String[] getLocalIdcCountryCode();


    public boolean isDuduUID(String userID) ;

}
