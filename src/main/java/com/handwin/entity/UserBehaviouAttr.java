package com.handwin.entity;

import info.archinnov.achilles.annotations.*;


/**
 * 用户表
 */

@Entity(table = "user_behaviou_attr", keyspace = "faceshow", comment = "用户行为属性如软件版本等")
public class UserBehaviouAttr {
    @CompoundPrimaryKey
    private UserAppIdKey userAppIdkey;

    @Column(name = "client_version")
    private String clientVersion;

    @Column(name = "ua")
    private String ua ;

    public UserAppIdKey getUserAppIdkey() {
        return userAppIdkey;
    }

    public void setUserAppIdkey(UserAppIdKey userAppIdkey) {
        this.userAppIdkey = userAppIdkey;
    }

    public String getClientVersion() {
        return clientVersion;
    }

    public void setClientVersion(String clientVersion) {
        this.clientVersion = clientVersion;
    }


    public String getUa() {
        return ua;
    }

    public void setUa(String ua) {
        this.ua = ua;
    }

    public static class UserAppIdKey {
        @PartitionKey
        @Order(1)
        private String userId;

        @PartitionKey
        @Column(name = "app_id")
        @Order(2)
        private Integer appId = 0;

        public UserAppIdKey() {
        }

        public UserAppIdKey(String userId) {
            this.userId = userId;
        }

        public UserAppIdKey(String userId, Integer appId) {
            this.userId = userId;
            this.appId = appId;
        }

        public Integer getAppId() {
            return appId;
        }

        public void setAppId(Integer appId) {
            this.appId = appId;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("UserAppIdKey{");
            sb.append("userId='").append(userId).append('\'');
            sb.append(", appId=").append(appId);
            sb.append('}');
            return sb.toString();
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UserBehaviouAttr{");
        sb.append("userAppIdkey=").append(userAppIdkey);
        sb.append(", clientVersion='").append(clientVersion).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
