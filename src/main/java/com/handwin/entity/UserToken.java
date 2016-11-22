package com.handwin.entity;

import info.archinnov.achilles.annotations.*;

import static info.archinnov.achilles.type.ConsistencyLevel.QUORUM;

@Entity(table = "user_token",keyspace = "faceshow", comment = "table for save mobile push token")
public class UserToken {

    @EmbeddedId
    private TokenKey id;

    @Column(name = "token_code")
    private String token;

    @Column(name = "device_type")
    private Integer deviceType;

    @Column(name = "provider")
    private String provider;


    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public TokenKey getId() {
        return id;
    }

    public void setId(TokenKey id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Integer getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(Integer deviceType) {
        this.deviceType = deviceType;
    }
    public UserToken() {

    }
}

