package com.handwin.entity;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.Order;

public class PhoneKey {
    @Column()
    @Order(1)
    private String mobile;

    @Column(name = "country_code")
    @Order(2)
    private String countryCode;

    @Column
    @Order(3)
    private String userId;

    public PhoneKey() {

    }

    public PhoneKey(String countryCode, String mobile, String userId) {
        this.countryCode = countryCode;
        this.mobile = mobile;
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }
}

