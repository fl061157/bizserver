package com.handwin.packet;

import com.handwin.utils.UserUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Created by fangliang on 16/6/1.
 */
public class UdpRoutePacket extends CommandPacket {

    public final static int COMMAND_UDP_ROUTE_TYPE = 0x2C;
    public final static int COMMAND_UDP_ROUTE_PACKET_TYPE = COMMAND_UDP_ROUTE_TYPE * 256 + COMMAND_PACKET_TYPE;


    public UdpRoutePacket() {
        this.setPacketType(COMMAND_UDP_ROUTE_PACKET_TYPE);
    }

    private int commonad;

    private String userID;

    private String detectID;

    private String extraData;

    @Override
    public void attachThirdUserId(Integer appID) {
        if (StringUtils.isNotBlank(userID)) {
            userID = UserUtils.attachThirdUserID(userID, appID);
        }
    }

    public int getCommonad() {
        return commonad;
    }

    public void setCommonad(int commonad) {
        this.commonad = commonad;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getDetectID() {
        return detectID;
    }

    public void setDetectID(String detectID) {
        this.detectID = detectID;
    }

    public String getExtraData() {
        return extraData;
    }

    public void setExtraData(String extraData) {
        this.extraData = extraData;
    }
}
