package com.handwin.service;

import com.handwin.entity.*;
import info.archinnov.achilles.persistence.PersistenceManager;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.util.Locale;


/**
 * Created by piguangtao on 15/9/7.
 */
@Service
public class UserCallSmSendService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserCallSmSendService.class);

    @Inject
    @Qualifier("basicEntityManager")
    private PersistenceManager manager;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private SmsService smsService;

    @Autowired
    private UserService userService;

    /**
     * @param receiverId 呼叫的接受方
     * @param fromId     呼叫的发送方
     */
    public void incUserSmCount(String receiverId, String fromId) {
        try {
            LOGGER.debug("[UserCallSm]receiverId:{},fromId:{} count increase by 1");
            UserCallSmSendKey key = new UserCallSmSendKey();
            key.setReceiverId(receiverId);
            key.setFromId(fromId);

            //先查询，再增加1, 客户端不会并发调用此接口，不需要考虑并发情况
            UserCallSmSend smSend = manager.find(UserCallSmSend.class, key);
            if (null == smSend) {
                smSend = new UserCallSmSend();
                smSend.setKey(key);
                smSend.setCount(1);
            } else {
                smSend.setCount(smSend.getCount() + 1);
            }
            smSend.setUpdateTime(new Timestamp(System.currentTimeMillis()));
            manager.insertOrUpdate(smSend);
        } catch (Exception e) {
            LOGGER.error(String.format("fails to increase user call sm send.receiverId:%s,fromId:%s", receiverId, fromId), e);
        }
    }

    public void sendCallSm(User toUser, User fromUser, String type) {
        if (null == toUser || null == fromUser) {
            return;
        }
        if (StringUtils.isBlank(toUser.getMobilePlaintext())) {
            LOGGER.debug("[sm]no send. userId:{} has no plain text mobile", toUser);
            return;
        }
//        String fromUserMobile = fromUser.getMobile();
//        String fromCountryCode = fromUser.getCountrycode();
//        String toUserId = toUser.getId();

//        PhoneBook phoneBook = manager.find(PhoneBook.class, new PhoneKey(fromCountryCode, fromUserMobile, toUserId));
//
//        if (null == phoneBook) {
//            LOGGER.debug("[send call sm] fromCountryCode:{},fromUserMobile:{} not in toUserId:{} phone book. no send sm", fromCountryCode, fromUserMobile, toUserId);
//            return;
//        }



        UserCallSmSendKey key = new UserCallSmSendKey();
        key.setFromId(fromUser.getId());
        key.setReceiverId(toUser.getId());

        UserCallSmSend smSend = manager.find(UserCallSmSend.class, key);
        if (null == smSend) {
            //表示第一次发送短，需要发送短信提示语
            //发送方是否在接受方的本地通讯录中
            Locale locale = toUser.getLocale();
            String resourceName = null;
            switch (type) {
                case "call": {
                    resourceName = "calling.sm";
                    break;
                }
                case "missed": {
                    resourceName = "missed.call.sm";
                    break;
                }
            }
            if (StringUtils.isBlank(resourceName)) {
                LOGGER.warn("[send call sm]. type:{} no support", type);
                return;
            }
            String sendUserName = userService.getFriendNickname(fromUser, toUser.getId(), toUser.getAppId());
            String msgContent = messageSource.getMessage(resourceName, new Object[]{sendUserName}, locale);
            smsService.sendSmsExcludeAuth(toUser.getMobilePlaintext(), toUser.getCountrycode(), msgContent);
            LOGGER.debug("[user call sm send] first time. send sm. toUserId:{},fromUserId:{},type:{},content:{}", toUser.getId(), fromUser.getId(), type, msgContent);
        } else {
            LOGGER.debug("[user call sm send]. not first time, no send sm. receiverId:{},fromId:{}", toUser.getId(), fromUser.getId());
        }
        incUserSmCount(toUser.getId(), fromUser.getId());

    }
}
