package com.handwin.utils;


import com.handwin.packet.PacketHead;
import io.netty.buffer.ByteBuf;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created with IntelliJ IDEA.
 * User: sunhao
 * Date: 13-9-16
 * Time: 下午1:56
 */
public class UserUtils {
    private static Logger logger = LoggerFactory.getLogger(UserUtils.class);

    private static final String USERID_APPID_SPBLIT = "_";

    public static String getAppUserId(String userId, String appId) {
        return getAppUserId(userId, Integer.valueOf(appId));
    }

    public static String getUserId(String appUserId) {
        Assert.notNull(appUserId);
        return appUserId.split(USERID_APPID_SPBLIT)[0];
    }

    public static String getAppUserId(String userId, int appId) {
        return userId + USERID_APPID_SPBLIT + appId;
    }

    public static String getAppId(String appUserId) {
        Assert.notNull(appUserId);
        Assert.state(appUserId.contains(USERID_APPID_SPBLIT), "appUserId:" + appUserId + " not contain appId");
        return appUserId.split(USERID_APPID_SPBLIT)[1];
    }

    public static String formatLanguage(String language) {
        String result = "en";
        if (null != language) {
            language = language.toLowerCase();
            if (language.contains("zh")) {
                result = "zh";
            }
        }
        return result;
    }

    public static boolean isUserHideMessage(String hideTime, String timeZone) {
        boolean result = false;
        if (null != hideTime && !"".equals(hideTime)) {
            if (null != timeZone && !"".equals(timeZone)) {
                try {
                    String prefix = timeZone.substring(0, 1);
                    String offsetStr = timeZone.substring(1);
                    String[] offsetArray = offsetStr.split(":");
                    int hourOffset = Integer.parseInt(offsetArray[0]);
                    int minoffset = 0;
                    if (offsetArray.length == 2) {
                        minoffset = Integer.parseInt(offsetArray[1]);
                    }
                    if ("-".equals(prefix)) {
                        hourOffset = 0 - hourOffset;
                        minoffset = minoffset != 0 ? 0 - minoffset : minoffset;
                    }
                    result = isHideTimeValid(hideTime, hourOffset, minoffset);
                } catch (Exception e) {
                    //解析出错时，默认按照无免打扰
                    logger.error(e.getMessage(), e);
                }
            }
        }
        return result;
    }

    private static boolean isHideTimeValid(String hideTime, int hourOffset, int minoffset) {
        boolean result = false;   //可以发
        if (hideTime == null || hideTime.length() != 11) {
            return result;
        }
        if (hideTime.length() != 11) {
            return true;
        }
        hideTime = hideTime.replaceAll(":", "");
        int len = hideTime.indexOf("-");

        DateFormat format = new SimpleDateFormat("HHmm");
        Calendar calendar = Calendar.getInstance();
        //获取时间偏移量
        int zoneOffset = calendar.get(Calendar.ZONE_OFFSET);
        //获取夏令时差
        int dstOffset = calendar.get(Calendar.DST_OFFSET);
        calendar.add(Calendar.MILLISECOND, -(zoneOffset + dstOffset));

        calendar.add(Calendar.HOUR_OF_DAY, hourOffset);
        if (0 != minoffset) {
            calendar.add(Calendar.MINUTE, minoffset);
        }

        Integer nowTime = Integer.parseInt(format.format(calendar.getTime()));
        Integer startTime = Integer.parseInt(hideTime.substring(0, len));
        Integer endTime = Integer.parseInt(hideTime.substring(len + 1));

        //开始和结束时间再同一天
        if (startTime < endTime) {
            //在限制区间内 不发
            if (startTime < nowTime && nowTime < endTime) {
                result = true;
            }
        } else {
            //在限制区间内 不发
            if ((startTime < nowTime && nowTime < 2400) || nowTime < endTime) {
                result = true;
            }
        }
        return result;
    }


    public static boolean isInBlackList(byte flag) {
        return (flag & SystemConstant.USER_CONVERSION_BLACK_FLAG) == SystemConstant.USER_CONVERSION_BLACK_FLAG;
    }

    public static boolean isInGreyList(byte flag) {
        return (flag & SystemConstant.USER_CONVERSION_GREY_FLAG) == SystemConstant.USER_CONVERSION_GREY_FLAG;
    }

    public static boolean isThiradApp(Integer appID) {
        if (appID != null && appID > 65535) return true;
        return false;
    }

    public static String attachThirdUserID(String userID, Integer appID) {
        if (StringUtils.isBlank(userID)) return userID;
        if (!isThiradApp(appID)) return userID;
        return String.format("%s@{%d}", userID, appID);
    }

    public static String cutGroupID(String groupID, Integer appID) {
        if (appID <= 65535) return groupID;
        return cutThirdUserID(groupID);
    }

    public static String cutGroupID(String groupID ) {
        return cutThirdUserID(groupID);
    }


    public static String cutThirdUserID(String userID) {
        if (StringUtils.isNotBlank(userID)) {
            try {
                int index = userID.lastIndexOf("@{");
                if (index <= 0) {
                    return userID;
                } else {
                    userID = userID.substring(0, index);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return userID;
    }


    public static String chatRoomId(String chatRoomId) {
        if (StringUtils.isNotBlank(chatRoomId)) {
            int index = chatRoomId.indexOf("@{");
            if (index >= 0) {
                chatRoomId = chatRoomId.substring(0, index);
            }
        }
        return chatRoomId;
    }


    public static void writeThirdUser(String userID, PacketHead packetHead, ByteBuf buf) {
        if (packetHead == null || packetHead.getAppId() < 65535 || !userID.contains("@{")) {
            ByteBufUtils.writeUTF8String(buf, userID);
        } else {
            int index = userID.lastIndexOf("@{");
            if (index <= 0) {
                logger.error("[UserUtils] UserID:{} , index :{}", userID, index);
            } else {
                userID = userID.substring(0, index);
            }


            ByteBufUtils.writeUTFStringFixedLength(buf, userID, 32);
        }
    }


    public static String outThirdUserID(String userID, Integer appID) {
        if (appID < 65535 || !userID.contains("@{")) {
            return userID;
        } else {
            int index = userID.lastIndexOf("@{");
            if (index <= 0) {
                logger.error("[UserUtils] UserID:{} , index :{}", userID, index);
            } else {
                userID = userID.substring(0, index);
            }
            return userID;
        }
    }


}

