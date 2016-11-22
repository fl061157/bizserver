package com.handwin.service;

import com.handwin.packet.CallPacket;
import com.handwin.packet.CallStatus;

/**
 * Created by wyang on 2014/8/19.
 */
public abstract class AbstractCallService {
    public abstract CallPacket buildCallResponsePacket(String peerName, CallStatus callStatus,
                                                       Integer status, CallPacket callPacket,
                                                       boolean includeUserData);

}
