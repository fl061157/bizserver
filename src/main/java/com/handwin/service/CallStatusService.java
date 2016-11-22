package com.handwin.service;

import com.handwin.packet.CallPacket;

/**
 * @author fangliang
 */
public interface CallStatusService {

    /**
     * 呼叫请求接受方已经接受到的前缀(roomId)
     */
    String CALL_RECEIVER_RECEIVED_PREFIX_ROOM = "call_receiver_received_roomId";

    /**
     * 呼叫请求接受方已经接受到的前缀(userId)
     */
    String CALL_STATUS_PREFIX_USER = "call_receiver_received_userId";

    /**
     * 呼叫请求方挂断前缀(roomId)
     */
    String CALL_FROM_HANGUP_UNTALKED_PREFIX_ROOM = "call_from_hangup_roomId";

    /**
     * 呼叫请求方挂断前缀(user)
     */
    String CALL_FROM_HANGUP_UNTALKED_PREFIX_USER = "call_from_hangup_user";

    String PUSH_MISSED_CALL_STATUS_PREFIX = "push_missed_call_type_status_roomId";

    String CALL_START_TIME_PREFIX = "call_start_time_roomId_";

    String CALL_FROM_USER_PREFIX = "call_from_user_roomId_";

    String CALL_INFO_PREFIX = "call_roomId_info_";


    public boolean hasCallReceive(String roomID, String fromUserID, String toUserID);

    public void setCallReceiverReceivedStatus(String callReqReceiveUserId, String callReqFromUserId,
                                              CallPacket callPacket);

    public boolean hasHangupUnTalked(String roomID, String fromUserID, String toUserID);

    public void setCallFromHangupUnTalkedStatus(String roomID, String fromUserID,
                                                String toUserID, String callStatus);

    public byte[] getCallStatus(String roomID);

    public void setCallStatus(String roomID, String callStatus);

    public void setCallStartTime(String roomId, Long startTime);

    public Long getCallStartTime(String roomId);

    public void setCallFromUser(String roomId, String fromUserId);

    public String getCallFromUser(String roomId);

    public void cleanCallRoom(String roomId);

    public void setCallInfo(String roomId, String fromUserCountryCode, String fromUserId, String toUserId);

}
