package com.handwin.entity;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.Order;

/**
 * Created by hi on 14-4-12.
 */
public class MobileKey {

    @Column(name = "mobile")
    @Order(1)
    private String mobile;

    @Column(name = "countrycode")
    @Order(2)
    private String countrycode;

    public MobileKey(){}

    public MobileKey(String mobile,String countrycode) {
        this.countrycode = countrycode;
        this.mobile = mobile;
    }

    public String getCountrycode() {
        return countrycode;
    }

    public void setCountrycode(String countrycode) {
        this.countrycode = countrycode;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }
}
