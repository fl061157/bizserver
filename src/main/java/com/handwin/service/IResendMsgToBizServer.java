package com.handwin.service;

/**
 * Created by piguangtao on 2014/11/26.
 */
public interface IResendMsgToBizServer {
    public boolean saveSingleMsgForResend(String fromUserId, String cmsgId, String countryCode,String toUserId,byte[] msgBody);

    public boolean saveGroupMsgForResend(String fromUserId, String cmsgId, String countryCode,String groupId,byte[] msgBody);

    public boolean deleteResendMsg(String fromUserId, String cmsgId,String toRegion);

}
