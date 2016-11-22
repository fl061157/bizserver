package com.handwin.entity;

import org.apache.commons.lang.StringUtils;

/**
 * Created by piguangtao on 15/4/15.
 */
public class TraversingServerQuery {
    private String userId;
    private String countryCode;
    private String mobile;
    private String ip;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TraversingServerQuery{");
        sb.append("userId='").append(userId).append('\'');
        sb.append(", countryCode='").append(countryCode).append('\'');
        sb.append(", mobile='").append(mobile).append('\'');
        sb.append(", ip='").append(ip).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public boolean isValid() {
        return StringUtils.isNotBlank(userId) && StringUtils.isNotBlank(countryCode) && StringUtils.isNotBlank(mobile) && StringUtils.isNotBlank(ip);
    }
}
