package com.handwin.service.impl;

import com.datastax.driver.core.ConsistencyLevel;
import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.handwin.entity.*;
import com.handwin.exception.ServerException;
import com.handwin.exception.ServerException.ErrorCode;
import com.handwin.persist.StatusStore;
import com.handwin.service.UserService;
import com.handwin.utils.StringUtil;
import com.handwin.utils.SystemConstant;
import com.handwin.utils.UserUtils;
import info.archinnov.achilles.persistence.PersistenceManager;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static com.datastax.driver.core.querybuilder.QueryBuilder.in;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;


@Service
public class UserServiceImpl implements UserService, InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Inject
    @Qualifier("basicEntityManager")
    private PersistenceManager basicManager;

    @Inject
    @Qualifier("opManager")
    private PersistenceManager opManager;

    @Value("#{configproperties['base.url']}")
    private String baseUrl;

    @Value("${cn.cdn.url}")
    private String cnCDNUrl;

    @Value("${us.cdn.url}")
    private String usCDNUrl;

    @Value("#{configproperties['country.codes']}")
    private String countryCodes;

    @Value("#{configproperties['default.country.code']}")
    private String defaultCountryCode;

    @Value("#{configproperties['idc.country.codes']}")
    private String idcCountryCodes;


    @Value("#{configproperties['dudu.user.id']}")
    private String duduUID;

    @Autowired
    @Qualifier("statusClusterStoreImpl")
    private StatusStore statusStore;

    private Map<String, String> localCountryMaps = new HashMap<String, String>();

    private Map<String, String> idcCountryCodeMap = new HashMap<String, String>();

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }

    @Override
    public void afterPropertiesSet() {
        User.baseUrl = baseUrl;
        logger.debug("country codes:{},idc country codes:{}", countryCodes, idcCountryCodes);
        if (null != countryCodes) {
            String[] allCountryCodes = countryCodes.trim().split(",");
            for (String countryCode : allCountryCodes) {
                localCountryMaps.put(countryCode, countryCode);
            }
        } else {
            logger.error("country codes should not be null");
        }

        if (null != idcCountryCodes) {
            String[] allIdcCountryCodes = idcCountryCodes.trim().split(",");
            for (String idcCountryCode : allIdcCountryCodes) {
                String[] idcAndCountryCode = idcCountryCode.split("_");
                if (idcAndCountryCode.length == 2) {
                    idcCountryCodeMap.put(idcAndCountryCode[0], idcAndCountryCode[1]);
                }
            }
        } else {
            logger.error("idc country codes should not be null");
        }
    }

    private static int[] TIMES = new int[]{4, 2, 3};


    private LoadingCache<String, Optional<User>> userCache = CacheBuilder.newBuilder().maximumSize(50000).expireAfterWrite(60 * 60, TimeUnit.SECONDS).build(

            new CacheLoader<String, Optional<User>>() {
                @Override
                public Optional<User> load(String key) throws Exception {

                    String[] saa = org.apache.commons.lang3.StringUtils.split(key, "|");
                    String sessionID = saa[0];
                    Integer appID = Integer.valueOf(saa[1]);
                    User u;
                    try {
                        u = authorizeIt(sessionID, appID);
                    } catch (Exception e) {
                        throw e;
                    }
                    return Optional.fromNullable(u);
                }
            }

    );

    public User authorizeIt(String sessionId, Integer appId) {
        PersistenceManager realManager = getRealManager(appId);

        if (StringUtils.isBlank(sessionId)) {
            return null;
        }
        UserSession session;
        try {
            session = realManager.find(UserSession.class, sessionId);
        } catch (Throwable e) {
            throw new ServerException(ErrorCode.CanNotHandleIoError, e);
        }
        if (session == null) {
            return null;
        }
        User user;
        try {
            user = realManager.find(User.class, session.getUserId());
        } catch (Throwable e) {
            throw new ServerException(ErrorCode.CanNotHandleIoError, e);
        }
        return user;
    }


    @Override
    public User authorize(String sessionId, Integer appId) {

        try {
            return userCache.get(String.format("%s|%d", sessionId, appId)).orNull();
        } catch (ExecutionException e) {
            logger.error("authorize error sessionid: " + sessionId);
            return null;
        }
    }

    @Override
    public void updateUserLocalTimezone(User user, String locale, String timeZone) {

        boolean modified = false;
        if (timeZone != null && !timeZone.equals(user.getTimezone())) {
            modified = true;
            user.setTimezone(timeZone);
        }

        if (locale != null && !locale.equals(user.getLanguage())) {
            modified = true;
            user.setLanguage(locale);
        }
        PersistenceManager realManager = getRealManager(user.getAppId());

        if (modified) realManager.update(user);
    }

    private final static String USER_LATEST_LOGIN_KEY_PREFIX = "u_l_l_";

    @Override
    public void login(User user, int appId, String channelId,
                      String nodeId, String ip, String locale, String timeZone) {
        Throwable throwable = null;
        try {
            updateUserLocalTimezone(user, locale, timeZone);
        } catch (Exception e) {
            throwable = e;
        }
        if (throwable != null) {
            throw new ServerException(ErrorCode.CanHandleIoError, throwable);
        }
    }

    @Override
    public User findById(String id, Integer appId) {
        return loadById(id, appId);
    }


    @Override
    public User loadById(String id, Integer appId) {
        try {
            if (id.equals(duduUID)) {
                appId = 0;
            }
            PersistenceManager realManager = getRealManager(appId);
            User user = realManager.find(User.class, id);
            if (user != null && user.getAppId() != 0 &&
                    (user.getAppId() != appId) && user.getId().equals(duduUID)) {
                logger.error("[LoadById Error] user.id:{} , user.appID:{} , appID:{}  ", id, user.getAppId(), appId);
                return null;
            }
            return user;
        } catch (Throwable e) {
            throw new ServerException(ServerException.ErrorCode.CanHandleIoError, e);
        }
    }

    @Override
    public UserToken getTokenInfo(String user_id, Integer app_id) {
        PersistenceManager realManager = getRealManager(app_id);
        return realManager.find(UserToken.class, new TokenKey(user_id, app_id == null ? 0 : app_id));
    }

    /**
     * 根据手机号码返回用户
     *
     * @param mobile
     * @return
     */
    @Override
    public MobileIndex findMobileIndex(String mobile, String countrycode) {
        return basicManager.find(MobileIndex.class, new MobileKey(mobile, countrycode));
    }


    /**
     * 优先级顺序，备注名称、通讯录名称、发送方的昵称
     *
     * @param user
     * @param friendId
     * @param appId
     * @return
     * @throws ServerException
     */
    @Override
    public String getFriendNickname(User user, String friendId, int appId) throws ServerException {
        PersistenceManager realManager = getRealManager(appId);

        String userID = user.getId();
        Friend friend = null;
        try {
            friend = realManager.find(Friend.class, new UserKey(friendId, appId, userID));
        } catch (Exception e) {
            logger.error("fails to get friend.", e);
        }

        if (friend != null && StringUtil.isSet(friend.getContactName())) {
            logger.debug("friend remark name. userId:{},otherUserId:{},remark:{}", friendId, user.getId(), friend.getContactName());
            return friend.getContactName();
        }

        PhoneBook phoneBook = null;
        try {
            phoneBook = realManager.find(PhoneBook.class,
                    new PhoneKey(user.getCountrycode(), user.getMobile(), friendId));
        } catch (Exception e) {
            logger.error("fails to get phonebook.", e);
        }
        if (phoneBook != null && StringUtil.isSet(phoneBook.getName())) {
            return phoneBook.getName();
        }

        String nickName = user.getNickname();
        if (null != nickName && StringUtil.isSet(nickName)) {
            return nickName;
        }

        return "Unknown".intern();
    }


    @Override
    public boolean isLocalUser(String countryCode) {
        if (StringUtils.isEmpty(countryCode)) {
            return localCountryMaps.containsKey(defaultCountryCode);
        }
        if (idcCountryCodeMap.containsKey(countryCode)) {
            return localCountryMaps.containsKey(countryCode);
        } else {
            return localCountryMaps.containsKey(defaultCountryCode);
        }
    }

    @Override
    public boolean isLocal(String tcpNode) {
        String rc = "0001";
        String nodeID = tcpNode;
        if (nodeID.contains("c_cn")) {
            rc = "0086";
        }
        return isLocalUser(rc);
    }

    @Override
    public boolean isDuduUID(String userID) {
        //return duduUID.equals(userID);
        return userID == null ? false : userID.startsWith(duduUID);
    }

    @Override
    public boolean isSystemAccount(User user) {
        return null != user.getUserType() && user.getUserType() == SystemConstant.USER_TYPE_SYSTEMACCOUNT;
    }

    @Override
    public List<User> findByIdList(List<String> idList) {
        return new ArrayList<>(basicManager.typedQuery(User.class, select().from("users").where(in("id", idList.toArray()))
                .setConsistencyLevel(ConsistencyLevel.LOCAL_ONE)).get());
    }

    public List<User> findByIdList(List<String> idList, Integer appID) {
        if (isOuter(appID)) {
            return new ArrayList<>(getRealManager(appID).typedQuery(User.class, select().from("users").where(in("id", idList.toArray()))
                    .setConsistencyLevel(ConsistencyLevel.QUORUM)).get());
        } else {
            return new ArrayList<>(getRealManager(appID).typedQuery(User.class, select().from("users").where(in("id", idList.toArray()))
                    .setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM)).get());
        }
    }


    @Override
    public UserBehaviouAttr getUserBehaviouAttr(String userId, Integer appId) {
        return basicManager.find(UserBehaviouAttr.class, new UserBehaviouAttr.UserAppIdKey(userId, appId));
    }


    public String[] getLocalIdcCountryCode() {
        return localCountryMaps.keySet().toArray(new String[]{});
    }

    public UserSessionIndex findUserSessionIndexByKey(String user_id, Integer app_id) {
        return basicManager.find(UserSessionIndex.class, new UserSessionIndexKey(user_id, app_id));
    }

    public PersistenceManager getRealManager(Integer appId) {
        return appId != null && appId > SystemConstant.CG_MAX_APP_ID ? opManager : basicManager;
    }

    public boolean isOuter(Integer appId) {
        return appId != null && appId > SystemConstant.CG_MAX_APP_ID;
    }


    public static void main(String[] args) {
        String sss = "abc|def";
        String[] ss = org.apache.commons.lang3.StringUtils.split(sss, "|");
        System.out.println(ss[0] + " : " + ss[1]);
    }


}


