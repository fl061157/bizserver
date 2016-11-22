package com.handwin.service.impl;


import com.handwin.packet.CallPacket;
import com.handwin.persist.StatusStore;
import com.handwin.service.CallStatusService;
import com.handwin.utils.SystemConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

import static java.lang.String.format;
import static java.lang.String.valueOf;


/**
 * @author fangliang
 */
@Service
public class CallStatusServiceImpl implements CallStatusService {


    private static final Logger logger = LoggerFactory.getLogger(CallStatusServiceImpl.class);

    @Value("${push.waitack.msg.time.delay.millisecond}")
    protected int pushMessageWaitAckDelay;

    @Value("${call.offline.msg.time.delay}")
    protected int callOfflineMsgDelay;

    @Autowired
    @Qualifier(value = "statusClusterStoreImpl")
    private StatusStore statusStore;


    private static final int ROOM_INVALID_SECONDS = 2 * 24 * 60 * 60;

    private static final int ROOM_DELAY_CLEAN_SECONDS = 5 * 60;


    @Override
    public boolean hasCallReceive(String roomID, String fromUserID,
                                  String toUserID) {
        try {
            byte[] receiveBytes = statusStore.get(getCallReceiverReceivedKey(roomID).getBytes());
//            if (receiveBytes == null || receiveBytes.length == 0) {
//                receiveBytes = statusStore.get(getCallReceiverReceivedKey(fromUserID, toUserID).getBytes());
//            }
            return receiveBytes != null && receiveBytes.length > 0;
        } catch (Exception e) {
            logger.error(String.format("roomID:%s hasCallReceive", roomID), e);
            return true;
        }
    }


    @Override
    public void setCallReceiverReceivedStatus(String callReqReceiveUserId, String callReqFromUserId,
                                              CallPacket callPacket) {

        try {
            if (null != callPacket.getRoomId()) {
                //向redis添加终止任务定时执行的逻辑
                statusStore.set(getCallReceiverReceivedKey(callPacket.getRoomId()).getBytes(),
                        valueOf(callPacket.getCallStatus()).getBytes(), ROOM_INVALID_SECONDS);
            }
//            else {
//                //toUserId表示呼叫请求的发起方del
//                statusStore.set(getCallReceiverReceivedKey(callReqFromUserId, callReqReceiveUserId).getBytes(),
//                        valueOf(callPacket.getCallStatus()).getBytes(), 6 * pushMessageWaitAckDelay / 1000);
//            }
        } catch (Exception e) {
            logger.error(String.format("roomID:%s setCallReceiverReceivedStatus", callPacket.getRoomId()), e);
        }
    }


    @Override
    public boolean hasHangupUnTalked(String roomID, String fromUserID,
                                     String toUserID) {
        try {
            byte[] hangupUntalkedBytes = statusStore.get(getCallFromHangupUnTalkedKey(roomID).getBytes());
            return hangupUntalkedBytes != null && hangupUntalkedBytes.length > 0;
        } catch (Exception e) {
            logger.error(String.format("roomID:%s hasHangupUnTalked error", roomID), e);
            return true;
        }
    }


    @Override
    public void setCallFromHangupUnTalkedStatus(String roomID,
                                                String fromUserID, String toUserID, String callStatus) {
        try {
//            statusStore.set(getCallFromHangupUnTalkedKey(fromUserID, toUserID).getBytes(),
//                    callStatus.getBytes(), callOfflineMsgDelay);
            statusStore.set(getCallFromHangupUnTalkedKey(roomID).getBytes(), callStatus.getBytes(), ROOM_INVALID_SECONDS);
        } catch (Exception e) {
            logger.error(String.format("roomID:%s setCallFromHangupUnTalkedStatus error", roomID), e);
        }
    }


    @Override
    public byte[] getCallStatus(String roomID) {
        try {
            return statusStore.get(getPushMissedCallStatusKey(roomID).getBytes());
        } catch (Exception e) {
            logger.error("roomID:{} , error", roomID, e);
            return null;
        }
    }

    @Override
    public void setCallStatus(String roomID, String callStatus) {
        try {
            statusStore.set(getPushMissedCallStatusKey(roomID).getBytes(), callStatus.getBytes(), ROOM_INVALID_SECONDS);
        } catch (Exception e) {
            logger.error("roomID:{} error", roomID, e);
        }
    }

    @Override
    public void setCallStartTime(String roomId, Long startTime) {
        if (null == roomId) return;
        String startTimeStr = null != startTime ? String.valueOf(startTime) : String.valueOf(System.currentTimeMillis());
        try {
            statusStore.set(getCallStartTimeKey(roomId).getBytes(SystemConstant.CHARSET_UTF8), startTimeStr.getBytes(SystemConstant.CHARSET_UTF8), ROOM_INVALID_SECONDS);
        } catch (Throwable e) {
            logger.error("setCallStartTime Error roomId:{} , startTime:{} ", roomId, startTime, e);
        }
    }

    @Override
    public Long getCallStartTime(String roomId) {
        if (null == roomId) return null;
        try {
            String startTimeStr = statusStore.get(getCallStartTimeKey(roomId));
            return null == startTimeStr ? null : Long.valueOf(startTimeStr);
        } catch (Throwable e) {
            logger.error("getCallStartTime Error roomId:{} ", roomId, e);
            return null;
        }
    }

    @Override
    public void setCallFromUser(String roomId, String fromUserId) {
        if (null == roomId || null == fromUserId) return;
        try {
            statusStore.set(getCallFromKey(roomId).getBytes(SystemConstant.CHARSET_UTF8), fromUserId.getBytes(SystemConstant.CHARSET_UTF8), ROOM_INVALID_SECONDS);
        } catch (Throwable e) {
            logger.error("setCallStartTime Error roomId:{} , from user:{} ", roomId, fromUserId, e);
        }
    }

    @Override
    public String getCallFromUser(String roomId) {
        if (null == roomId) return null;
        try {
            String fromUserId = statusStore.get(getCallFromKey(roomId));
            return fromUserId;
        } catch (Throwable e) {
            logger.error("getCallStartTime Error roomId:{} ", roomId, e);
            return null;
        }
    }

    @Override
    public void cleanCallRoom(String roomId) {
        logger.debug("[clean room]roomId:{}", roomId);

        //设置过期时间
        try {
            statusStore.expire(getCallFromKey(roomId).getBytes(StandardCharsets.UTF_8), ROOM_DELAY_CLEAN_SECONDS);
            statusStore.expire(getCallStartTimeKey(roomId).getBytes(SystemConstant.CHARSET_UTF8), ROOM_DELAY_CLEAN_SECONDS);
            statusStore.expire(getPushMissedCallStatusKey(roomId).getBytes(SystemConstant.CHARSET_UTF8), ROOM_DELAY_CLEAN_SECONDS);
            statusStore.expire(getCallFromHangupUnTalkedKey(roomId).getBytes(SystemConstant.CHARSET_UTF8), ROOM_DELAY_CLEAN_SECONDS);
            statusStore.expire(getCallReceiverReceivedKey(roomId).getBytes(SystemConstant.CHARSET_UTF8), ROOM_DELAY_CLEAN_SECONDS);
            statusStore.expire(getCallRoomInfoKey(roomId).getBytes(StandardCharsets.UTF_8), ROOM_DELAY_CLEAN_SECONDS);
        } catch (Exception e) {
            logger.error("fails to clean room cache", e);
        }

    }

    @Override
    public void setCallInfo(String roomId, String fromUserCountryCode, String fromUserId, String toUserId) {
        try {
            boolean result = statusStore.set(getCallRoomInfoKey(roomId).getBytes(StandardCharsets.UTF_8), String.format("%s_%s_%s", fromUserCountryCode, fromUserId, toUserId).getBytes(StandardCharsets.UTF_8), ROOM_INVALID_SECONDS);
            logger.debug("[call info].set call info. key:{} roomId:{}.fromUserCountryCode:{},fromUserId:{}, toUserId:{},result:{}", getCallRoomInfoKey(roomId), roomId, fromUserCountryCode, fromUserId, toUserId, result);
        } catch (Exception e) {
            logger.error("fails to set call room info", e);
        }
    }


    private String getCallReceiverReceivedKey(String roomId) {
        return format("%s_%s", CALL_RECEIVER_RECEIVED_PREFIX_ROOM, roomId);
    }

//    private String getCallReceiverReceivedKey(String callFrom, String callTo) {
//        return format("%s_%s_%s", CALL_STATUS_PREFIX_USER, callFrom, callTo);
//    }

    private String getCallFromHangupUnTalkedKey(String roomId) {
        return format("%s_%s", CALL_FROM_HANGUP_UNTALKED_PREFIX_ROOM, roomId);
    }

//    private String getCallFromHangupUnTalkedKey(String callFrom, String callTo) {
//        return format("%s_%s_%s", CALL_FROM_HANGUP_UNTALKED_PREFIX_USER, callFrom, callTo);
//    }


    private String getPushMissedCallStatusKey(String roomId) {
        return format("%s_%s", PUSH_MISSED_CALL_STATUS_PREFIX, roomId);
    }

    private String getCallStartTimeKey(String roomId) {
        return format("%s_%s", CALL_START_TIME_PREFIX, roomId);
    }

    private String getCallFromKey(String roomId) {
        return format("%s_%s", CALL_FROM_USER_PREFIX, roomId);
    }

    private String getCallRoomInfoKey(String roomId) {
        return format("%s%s", CALL_INFO_PREFIX, roomId);
    }


}
