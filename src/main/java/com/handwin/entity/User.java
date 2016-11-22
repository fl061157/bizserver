package com.handwin.entity;


import com.handwin.utils.LocaleUtils;
import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.Entity;
import info.archinnov.achilles.annotations.Id;
import info.archinnov.achilles.annotations.TypeTransformer;
import info.archinnov.achilles.internal.utils.UUIDGen;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


/**
 * 用户表
 */

@Entity(table = "users", keyspace = "faceshow", comment = "用户表")
public class User {
    public static String baseUrl = "";
    @Id
    private String id;

    @Column
    private String nickname;

    @Column(name = "mobile")
    private String mobile;

    @Column
    private Integer sex;

    @JsonIgnore
    @Column
    private String avatar;

    //@Column(name = "avatar_url")
    private String avatar_url;

    @JsonIgnore
    @Column(name = "reg_source")
    private String regSource;

    @JsonIgnore
    @Column
    private String language;

    /**
     * 默认为0   系统用户为1
     */
    @Column(name = "user_type")
    private Integer userType;


    @Column(name = "hide_time")
    private String hideTime;


    @Column
    private String timezone;

    @Column
    private String countrycode;

    /**
     * 默认为0 未校验 校验后为1
     */
    @Column(name = "mobile_verify")
    private Integer mobileVerify;

    @JsonIgnore
    @Column(name = "create_time")
    private Date createTime;

    /**
     * 最近一次登陆时间
     */
    @JsonIgnore
    @Column(name = "last_login_time")
    @TypeTransformer(valueCodecClass = TimestampToString.class)
    private Timestamp lastLoginTime;

    /**
     * 最近更新时间
     */
    @JsonIgnore
    @Column(name = "last_update_time")
    private Long lastUpdateTime;

    @JsonIgnore
    @Column(name = "app_id")
    private int appId = 0;


    @Column(name = "public_key")
    private String publicKey;

    @Column(name = "account")
    private String account;

    private Integer conversation;

    @JsonIgnore
    @Column(name = "touch")
    private Integer touch;

    @JsonIgnore
    @Column(name = "mobile_plaintext")
    private String mobilePlaintext;

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public Integer getMobileVerify() {
        return mobileVerify;
    }

    public void setMobileVerify(Integer mobileVerify) {
        this.mobileVerify = mobileVerify;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getHideTime() {
        return hideTime;
    }

    public void setHideTime(String hideTime) {
        this.hideTime = hideTime;
    }

    private String[] tcpServer;
    private String[] fileServer;

    public String getAvatar_url() {

        if (StringUtils.isNotBlank(avatar_url)) return avatar_url;

        if (StringUtils.isNotBlank(avatar)) {

            if (avatar.toLowerCase().startsWith("http")) {
                return avatar;
            }
            return baseUrl + "/api/avatar/" + avatar;
        }
        return null;
    }

    public void setAvatar_url(String avatar_url) {
        this.avatar_url = avatar_url;
    }

    private String sessionId;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String[] getTcpServer() {
        return tcpServer;
    }

    public void setTcpServer(String[] tcpServer) {
        this.tcpServer = tcpServer;
    }

    public String[] getFileServer() {
        return fileServer;
    }

    public void setFileServer(String[] fileServer) {
        this.fileServer = fileServer;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }


    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public static String createUUID() {
        return UUIDGen.getTimeUUID().toString().replaceAll("\\-", "");
    }

    public Integer getSex() {
        return sex;
    }

    public void setSex(Integer sex) {
        this.sex = sex;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getRegSource() {
        return regSource;
    }

    public void setRegSource(String regSource) {
        this.regSource = regSource;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Integer getUserType() {
        return userType;
    }

    public void setUserType(Integer userType) {
        this.userType = userType;
    }

    public String getCountrycode() {
        return countrycode;
    }

    public void setCountrycode(String countrycode) {
        this.countrycode = countrycode;
    }

    public Timestamp getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(Timestamp lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public Long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public static String genPassword(String password, String salt) {
        return DigestUtils.md5Hex(password + salt);
    }

    public static String genSalt() {
        return DigestUtils.sha256Hex(Long.toHexString(System.currentTimeMillis()));
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public int getAppId() {
        return appId;
    }

    public void setAppId(int appId) {
        this.appId = appId;
    }

    public boolean equals(Object o) {
        if (!(o instanceof User)) return false;
        User u = (User) o;

        return this.id.equals(u.getId());
    }

    public Integer getConversation() {
        return conversation;
    }

    public void setConversation(Integer conversation) {
        this.conversation = conversation;
    }

    public Locale getLocale() {
        Locale result = new Locale("");
        if (null != language) {
            result = LocaleUtils.parseLocaleString(language);
        }
        return result;
    }

    public Integer getTouch() {
        return touch;
    }

    public void setTouch(Integer touch) {
        this.touch = touch;
    }

    public static boolean isAllowSend(String time) {
        boolean result = false;   //可以发LocaleUtils
        if (time == null || time.length() != 11) {
            return result;
        }

        if (time.length() != 11) {
            return true;
        }

        time = time.replaceAll(":", "");
        int len = time.indexOf("-");

        DateFormat format = new SimpleDateFormat("HHmm");
        Integer nowTime = Integer.parseInt(format.format(new Date()));
        Integer startTime = Integer.parseInt(time.substring(0, len));
        Integer endTime = Integer.parseInt(time.substring(len + 1));

        //开始和结束时间再同一天
        if (startTime < endTime) {
            //在限制区间内 不发
            if (startTime < nowTime && nowTime < endTime) {
                result = true;
            }

        } else {

            //在限制区间内 不发
            if ((startTime < nowTime && nowTime < 2400) || nowTime < endTime) {
                result = true;
            }

        }


        return result;

    }

    public User() {
    }

    public String getMobilePlaintext() {
        return mobilePlaintext;
    }

    public void setMobilePlaintext(String mobilePlaintext) {
        this.mobilePlaintext = mobilePlaintext;
    }
}

