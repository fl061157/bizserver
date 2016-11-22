package com.handwin.service.impl;

import com.handwin.database.bean.RetrySendMessage;
import com.handwin.message.bean.ChannelInformation;
import com.handwin.message.bean.Status;
import com.handwin.message.service.RetryMessageService;
import com.handwin.service.IResendMsgToBizServer;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Created by piguangtao on 2014/11/27.
 */
@Service
public class ResendMsgToBizServerImpl implements IResendMsgToBizServer {
    @Autowired
    private RetryMessageService retryMessageService;

    @Value("#{configproperties['biz_server.exchange']}")
    private String biz2bizExchange;

    @Value("#{configproperties['timer2biz.queue']}")
    private String timer2bizQueueName;

    @Override
    public boolean saveSingleMsgForResend(String fromUserId, String cmsgId, String countryCode, String toUserId, byte[] msgBody) {

        //老版本没有cmsgId，不入库进行重发
        if(StringUtils.isBlank(cmsgId)) return true;

        RetrySendMessage retrySendMessage = new RetrySendMessage();
        retrySendMessage.setFromUserID(fromUserId);
        retrySendMessage.setRetryMessageID(cmsgId);
        retrySendMessage.setRetryTimess(0);
        retrySendMessage.setStatus(Status.UnDeal.getStatus());
        retrySendMessage.setToUserID(toUserId);
        retrySendMessage.setToRegion(countryCode);

        ChannelInformation channelInfor = new ChannelInformation();
        channelInfor.setRegion(countryCode);
        channelInfor.setExchange(biz2bizExchange);
        channelInfor.setRouteKey(timer2bizQueueName);

        return retryMessageService.createMessage(retrySendMessage, channelInfor, msgBody);
    }

    @Override
    public boolean saveGroupMsgForResend(String fromUserId, String cmsgId, String countryCode, String groupId, byte[] msgBody) {
        return saveSingleMsgForResend(fromUserId, cmsgId, countryCode, groupId, msgBody);
    }

    @Override
    public boolean deleteResendMsg(String fromUserId, String cmsgId,String toRegion) {
        retryMessageService.deleteMessage(cmsgId, fromUserId,toRegion);
        return true;
    }
}
