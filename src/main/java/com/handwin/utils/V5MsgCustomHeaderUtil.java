package com.handwin.utils;

import com.handwin.protocal.v5.codec.V5PacketHead;
import org.springframework.stereotype.Service;

/**
 * Created by piguangtao on 16/3/21.
 */
@Service
public class V5MsgCustomHeaderUtil {
    public String getGroupId(V5PacketHead v5PacketHead) {
        return null != v5PacketHead && null != v5PacketHead.getHead("x-group-id") ? (String) v5PacketHead.getHead("x-group-id") : "";
    }

    public V5PacketHead setGroupId(V5PacketHead v5PacketHead, String groupId) {
        if (null != v5PacketHead) {
            v5PacketHead.addHead("x-group-id", groupId);
        }
        return v5PacketHead;
    }


    public String getGroupRegion(V5PacketHead v5PacketHead) {
        return null != v5PacketHead && null != v5PacketHead.getHead("x-group-region") ? (String) v5PacketHead.getHead("x-group-region") : "";
    }

    public V5PacketHead setGroupRegion(V5PacketHead v5PacketHead, String groupRegion) {
        if (null != v5PacketHead) {
            v5PacketHead.addHead("x-group-region", groupRegion);
        }
        return v5PacketHead;
    }

    public Integer getMsgType(V5PacketHead v5PacketHead) {
        return null != v5PacketHead && null != v5PacketHead.getHead("x-msg-type") ? (Integer) v5PacketHead.getHead("x-msg-type") : null;
    }

    public V5PacketHead setMsgType(V5PacketHead v5PacketHead, Integer msgType) {
        if (null != v5PacketHead) {
            v5PacketHead.addHead("x-msg-type", msgType);
        }
        return v5PacketHead;
    }

    public String getMsgSourceRegion(V5PacketHead v5PacketHead) {
        return null != v5PacketHead && null != v5PacketHead.getHead("x-source-region") ? (String) v5PacketHead.getHead("x-source-region") : null;
    }

    public V5PacketHead setMsgSourceRegion(V5PacketHead v5PacketHead, String sourceRegion) {
        if (null != v5PacketHead) {
            v5PacketHead.addHead("x-source-region", sourceRegion);
        }
        return v5PacketHead;
    }
}
