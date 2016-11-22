package com.handwin.packet.v5;

import com.handwin.packet.VoiceMessagePacket;

/**
 * Created by piguangtao on 16/3/21.
 */
public class V5VoiceMessagePacket extends VoiceMessagePacket implements V5SimpleMessagepacket {
    /**
     * 群组特定的接受者
     */
    private String[] groupSepUsers;

    /**
     * 消息的来源区域
     */
    private String sourceRegion;


    @Override
    public void setGroupSpeUsers(String[] userIds) {
        this.groupSepUsers = userIds;
    }

    @Override
    public String[] getGroupSpeUsers() {
        return this.groupSepUsers;
    }

    @Override
    public void setMessageSourceRegion(String sourceRegion) {
        this.sourceRegion = sourceRegion;
    }

    @Override
    public String getMessageSourceRegion() {
        return this.sourceRegion;
    }
}
