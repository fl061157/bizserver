package com.handwin.localentity;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.Order;
import info.archinnov.achilles.annotations.PartitionKey;

/**
 * Created by fangliang on 5/1/15.
 */
public class ChannelInfoKey {

    @Column(name = "user_id")
    @Order(1)
    private String userID;

    @Column(name = "app_id")
    @Order(2)
    private int appID;

    @Column(name = "channel_uuid")
    @Order(3)
    private String channelUUID;


    public ChannelInfoKey() {

    }


    public ChannelInfoKey(String userID, int appID, String channelUUID) {
        this.userID = userID;
        this.appID = appID;
        this.channelUUID = channelUUID;
    }

    public String getUserID() {
        return userID;
    }

    public int getAppID() {
        return appID;
    }

    public String getChannelUUID() {
        return channelUUID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public void setChannelUUID(String channelUUID) {
        this.channelUUID = channelUUID;
    }

    public void setAppID(int appID) {
        this.appID = appID;
    }
}
