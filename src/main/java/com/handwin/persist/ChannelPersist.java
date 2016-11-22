package com.handwin.persist;

import com.handwin.exception.ServerException;
import com.handwin.localentity.ChannelInfoBean;
import com.handwin.packet.ChannelMode;

import java.util.List;

/**
 * Created by fangliang on 5/1/15.
 */
public interface ChannelPersist {


    public void insert(ChannelInfoBean channelInfoBean, int expire) throws ServerException;

    public ChannelInfoBean get(String userID, int appID, String channelUUID) throws ServerException;

    public void delete(String userID, int appID, String channelUUID) throws ServerException;

    public void delete(String userID, int appID) throws ServerException;

    public List<ChannelInfoBean> find(String userID, int appID) throws ServerException;

    public void updateChannelMode(String userID, int appID, String channelUUID, ChannelMode channelMode, int expire) throws ServerException;

    public void expire(String userID, int appID, String channelUUID, int expire) throws ServerException;

    public void expire(ChannelInfoBean channelInfoBean, int expire) throws ServerException ;


}
