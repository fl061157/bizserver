package com.handwin.packet.v5;

/**
 * Created by piguangtao on 16/3/21.
 */
public interface V5SimpleMessagepacket {


    void setGroupSpeUsers(String[] userIds);

    String[] getGroupSpeUsers();

    void setMessageSourceRegion(String sourceRegion);

    String getMessageSourceRegion();
}
