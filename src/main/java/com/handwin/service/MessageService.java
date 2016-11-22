package com.handwin.service;

import com.handwin.entity.PushMsgMqBean;
import com.handwin.entity.User;
import com.handwin.entity.UserToken;
import com.handwin.entity.wrong.SimpleWrongMessage;
import com.handwin.exception.ServerException;
import com.handwin.localentity.Message;
import com.handwin.localentity.UserLocalMsgCounter;
import com.handwin.message.bean.MessageStatus;
import com.handwin.packet.*;
import com.handwin.protocal.v5.codec.V5GenericPacket;
import com.handwin.protocal.v5.codec.V5PacketHead;


public interface MessageService {


    public Long newMessageUID();

    public Message getMessage(String userId, long messageId) throws ServerException;

    /**
     * @param sender
     * @param receiver
     * @param message
     * @param content
     * @TODO 维护用户离线消息数的count, 忽略系统消息类型
     */
    public Message createMessage(String sender, String receiver,
                                 Message message, MessageStatus messageStatus, byte[] content) throws ServerException;


    /**
     * @param userID
     * @param messageID
     * @param messageStatus
     * @throws ServerException
     */
    public void updateMessage(String userID, long messageID, MessageStatus messageStatus) throws ServerException;


    /**
     * 跟进消息ID删除消息,userId为rowkey
     *
     * @param userId
     * @param messageId
     * @return
     */
    public boolean removeMessage(String userId, Long messageId) throws ServerException;

    /**
     * 更新本机未读消息
     *
     * @param userId
     * @param isAddOne
     */
    public UserLocalMsgCounter updateUnreadLocalCount(String userId, boolean isAddOne);


    /**
     * @param fromUserID
     * @param callPacket
     * @param messageStatus
     * @param callStatus
     * @param createTime
     * @return
     */
    public Message createCallMessage(String fromUserID, CallPacket callPacket, MessageStatus messageStatus,
                                     CallStatus callStatus, Long createTime);

    /**
     * @param notifyPackage
     * @param toUser
     * @param device
     * @param traceId
     */
    public void pushText(final SystemNotifyPacket notifyPackage, User toUser,
                         UserToken device, final String traceId);


    public void pushText(final SystemNotifyPacket notifyPackage, User toUser,
                         UserToken device, final String traceId, boolean noDisturb);


    /**
     * @param packetHead
     * @param genericPacket
     * @param toUser
     * @param device
     * @param traceId
     */
    public void pushText(final V5PacketHead packetHead, V5GenericPacket genericPacket, User toUser,
                         UserToken device, final String traceId);


    /**
     * @param fromUser
     * @param toUser
     * @param toUserToken
     * @param nickName
     * @param simpleMessagePacket
     * @param traceID
     */
    public void pushText(final User fromUser, final User toUser, final UserToken toUserToken,
                         final String nickName, CommonMsgPackage simpleMessagePacket, String traceID);


    public void pushText(User fromUser, User toUser, UserToken toUserToken, String text,
                         String traceID, PushMsgMqBean.NoticeType noticeType, String tipType, Boolean isMsgIncrementByOne, Long timeToLiveInMillsec);


    public void pushText(User fromUser, User toUser, UserToken toUserToken,
                         String nickName, SimpleMessagePacket packet,
                         String traceID);

    /**
     * @param simpleWrongMessage
     * @param message
     * @param fromUser
     * @param toUser
     * @param userToken
     * @param nickName
     * @throws ServerException
     */
    public void pushMessage(SimpleWrongMessage simpleWrongMessage, Message message, User fromUser,
                            User toUser, UserToken userToken, String nickName);

    /**
     * @param fromUser
     * @param toUser
     * @param toUserToken
     * @param callPacket
     * @param unReadCount
     * @param traceId
     */
    public void pushCall(User fromUser, User toUser, UserToken toUserToken, CallPacket callPacket,
                         Integer unReadCount, final String traceId, String nickName);

    /**
     * @param cmsgId
     * @param msgflag
     * @return
     */
    public Long isServerReceived(final String cmsgId, final byte msgflag, String userId);

    /**
     * @param cmsgId
     * @param messageId
     * @param ttl
     */
    public void addServerReceivedMessage(String cmsgId, Long messageId, final int ttl, String userId);


    public void sendPushCallReissueTip(Message message, Integer appId);

    /**
     * 获取消息内容的格式
     *
     * @param simpleMessagePacket
     * @return
     */
    public Integer getMessageEntityType(SimpleMessagePacket simpleMessagePacket);

}
