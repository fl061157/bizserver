package com.handwin.utils;

import java.nio.charset.Charset;

/**
 * Created by piguangtao on 14-3-11.
 */
public interface SystemConstant {

    String LANGUAGE_ENGLISH = "en";

    String LANGUAGE_CHINESE = "zh";

    String LANGUAGE_DEFAULT = "zh";

    int TO_SEND_MESSAGE_RECEIVE_TYPE_PERSON = 1;

    int TO_SEND_MESSAGE_RECEIVE_TYPE_GROUP = 2;

    byte PACKET_SECRET = 0x01;

    //用户黑名单设置标志
    byte USER_CONVERSION_BLACK_FLAG = 0x02;

    //用户灰名单设置标志(在线消息可以接受，离线消息不能接受)
    byte USER_CONVERSION_GREY_FLAG = 0x01;

    //用户设置的群组免打扰
    byte GROUP_CONVERSION_NO_DISTURB_FLAG = 0x10;

    Charset CHARSET_UTF8 = Charset.forName("utf-8");

    String IOS_PUSH_TEXT_SECRET_TEMPLATE = "ios.offline.push.text.secret.template";

    String IOS_PUSH_TEXT_TEMPLATE = "ios.offline.push.text.template";

    String IOS_PUSH_PIC_TEMPLATE = "ios.offline.push.pic.template";

    String IOS_PUSH_NAMECARD_TEMPLATE = "ios.offline.push.namecard.template";

    String IOS_PUSH_VOICE_TEMPLATE = "ios.offline.push.voice.template";

    String IOS_PUSH_VIDEO_TEMPLATE = "ios.offline.push.video.template";

    String IOS_PUSH_VIDEO_CALL = "ios.offline.push.video.call";

    String IOS_PUSH_AUDIO_CALL = "ios.offline.push.audio.call";


    String IOS_PUSH_GAME_VIDEO_CALL = "ios.offline.push.game.video.call";
    String IOS_PUSH_GAME_AUDIO_CALL = "ios.offline.push.game.audio.call";


    String IOS_PUSH_GAME_CALL = "ios.offline.push.game.call";

    String ANDROID_PUSH_TEXT_SECRET_TEMPLATE = "android.offline.push.text.secret.template";

    String ANDROID_PUSH_TEXT_TEMPLATE = "android.offline.push.text.template";

    String ANDROID_PUSH_PIC_TEMPLATE = "android.offline.push.pic.template";

    String ANDROID_PUSH_NAMECARD_TEMPLATE = "android.offline.push.namecard.template";

    String ANDROID_PUSH_VOICE_TEMPLATE = "android.offline.push.voice.template";

    String ANDROID_PUSH_VIDEO_TEMPLATE = "android.offline.push.video.template";


    String ANDROID_PUSH_GAME_VIDEO_CALL = "android.offline.push.game.video.call";
    String ANDROID_PUSH_GAME_AUDIO_CALL = "android.offline.push.game.audio.call";


    String ANDROID_PUSH_VIDEO_CALL = "android.offline.push.video.call";

    String ANDROID_PUSH_AUDIO_CALL = "android.offline.push.audio.call";

    String OFFLINE_VIDEO_CALL_REISSUE_TIP = "offline.video.call.reissue.tip";

    String OFFLINE_AUDIO_CALL_REISSUE_TIP = "offline.audio.call.reissue.tip";

    int DEVICE_TYPE_IOS = 1;

    int DEVICE_TYPE_ANDRIOD = 2;

    byte MSGFLAG_RESENT = 0x01;


    int USER_TYPE_SYSTEMACCOUNT = 1;

    Integer NODE_ID_DEFAULT = 100;

    String NODE_IP_DEFAULT = "127.0.0.1";

    String CHANNEL_NETWORK_TYPE = "net_type";

    String LOGIN_APP_KEY = "app_key";

    int USER_TOKEN_DEVICE_TYPE_IOS = 1;

    int USER_TOKEN_DEVICE_TYPE_ANDRIOD = 2;

    String DEVICE_TOKEN_PROVIDE_XIAOMI = "7";

    int CG_MAX_APP_ID = 65534;

    //push推送时，不进入到回话界面
    String PUSH_TIPTYPE_DONOT_ENTER_INTERFACE = "1";

    byte MESSAGE_TYPE_RICH_MEDIA = 0x10;

    /**
     * 普通文本消息格式
     */
    Integer MESSAGE_ENTITY_PLAIN = 0;

    /**
     * 富媒体消息格式
     */
    Integer MESSAGE_ENTITY_RICH_MEDIA = 1;

    Integer MESSAGE_ENTITY_SYSTEM_NOTIRY = 2;

    String SYSTEM_NOTIFY_EXTRA_KEY_TYPE = "type";

    String SYSTEM_NOTIFY_EXTRAT_TYPE_GROUP = "group";

    String SYSTEM_NOTIFY_EXTRA_KEY_GROUP_ID = "group-id";

    /**
     * 点击push栏是否进去会话界面
     */
    String SYSTEM_NOTIFY_EXTRA_PUSH_ENTER_CONVERSATION = "enter-conversation";

    String SYSTEM_SECRETERY_SESSION_ID = "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";


}
