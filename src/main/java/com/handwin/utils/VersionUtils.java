package com.handwin.utils;

import com.handwin.entity.UserBehaviouAttr;
import org.apache.commons.lang.StringUtils;

/**
 * Created by fangliang on 17/12/15.
 */
public class VersionUtils {


    static String IOS_VIDEO_SUPPORT_VERSION_STR = "3.1.52";
    static int[] IOS_VIDEO_SUPPORT_VERSION_ARRAY;
    static int IOS_VIDEO_VERSION_LENGTH;


    static String ANDROID_VIDEO_SUPPORT_VERSION_STR = "3.1.58";
    static int[] ANDROID_VIDEO_SUPPORT_VERSION_ARRAY;
    static int ANDROID_VIDEO_VERSION_LENGTH;


    static String IOS_LIKE_SUPPORT_VERSION_STR = "3.3";
    static int[] IOS_LIKE_SUPPORT_VERSION_ARRAY;
    static int IOS_LIKE_VERSION_LENGTH;


    static String ANDROID_LIKE_SUPPORT_VERSION_STR = "3.3";
    static int[] ANDROID_LIKE_SUPPORT_VERSION_ARRAY;
    static int ANDROID_LIKE_VERSION_LENGTH;


    static {
        String[] array = IOS_VIDEO_SUPPORT_VERSION_STR.split("\\.");
        IOS_VIDEO_VERSION_LENGTH = array.length;
        IOS_VIDEO_SUPPORT_VERSION_ARRAY = new int[IOS_VIDEO_VERSION_LENGTH];
        for (int i = 0; i < IOS_VIDEO_VERSION_LENGTH; i++) {
            IOS_VIDEO_SUPPORT_VERSION_ARRAY[i] = Integer.parseInt(array[i]);
        }


        array = ANDROID_VIDEO_SUPPORT_VERSION_STR.split("\\.");
        ANDROID_VIDEO_VERSION_LENGTH = array.length;
        ANDROID_VIDEO_SUPPORT_VERSION_ARRAY = new int[ANDROID_VIDEO_VERSION_LENGTH];
        for (int i = 0; i < ANDROID_VIDEO_VERSION_LENGTH; i++) {
            ANDROID_VIDEO_SUPPORT_VERSION_ARRAY[i] = Integer.parseInt(array[i]);
        }


        array = IOS_LIKE_SUPPORT_VERSION_STR.split("\\.");
        IOS_LIKE_VERSION_LENGTH = array.length;
        IOS_LIKE_SUPPORT_VERSION_ARRAY = new int[IOS_LIKE_VERSION_LENGTH];
        for (int i = 0; i < IOS_LIKE_VERSION_LENGTH; i++) {
            IOS_LIKE_SUPPORT_VERSION_ARRAY[i] = Integer.parseInt(array[i]);
        }


        array = ANDROID_LIKE_SUPPORT_VERSION_STR.split("\\.");
        ANDROID_LIKE_VERSION_LENGTH = array.length;
        ANDROID_LIKE_SUPPORT_VERSION_ARRAY = new int[ANDROID_LIKE_VERSION_LENGTH];
        for (int i = 0; i < ANDROID_LIKE_VERSION_LENGTH; i++) {
            ANDROID_LIKE_SUPPORT_VERSION_ARRAY[i] = Integer.parseInt(array[i]);
        }



    }


    private static boolean iosSupportVersion(String version) {
        if (StringUtils.isBlank(version)) return false;
        String[] vss = version.split("\\.");
        if (vss == null) return false;
        int vsl = vss.length;
        for (int i = 0; i < IOS_VIDEO_VERSION_LENGTH; i++) {
            String vs = (i < vsl) ? vss[i] : "0";
            Integer ivs = Integer.parseInt(vs);

            if (ivs > IOS_VIDEO_SUPPORT_VERSION_ARRAY[i]) {
                return true;
            } else if (ivs < IOS_VIDEO_SUPPORT_VERSION_ARRAY[i]) {
                return false;
            }
        }
        return true;
    }



    private static boolean iosLikeSupportVersion(String version) {
        if (StringUtils.isBlank(version)) return false;
        String[] vss = version.split("\\.");
        if (vss == null) return false;
        int vsl = vss.length;
        for (int i = 0; i < IOS_LIKE_VERSION_LENGTH; i++) {
            String vs = (i < vsl) ? vss[i] : "0";
            Integer ivs = Integer.parseInt(vs);

            if (ivs > IOS_LIKE_SUPPORT_VERSION_ARRAY[i]) {
                return true;
            } else if (ivs < IOS_LIKE_SUPPORT_VERSION_ARRAY[i]) {
                return false;
            }
        }
        return true;
    }


    private static boolean androidSupportVersion(String version) {
        if (StringUtils.isBlank(version)) return false;
        String[] vss = version.split("\\.");
        if (vss == null) return false;
        int vsl = vss.length;
        for (int i = 0; i < ANDROID_VIDEO_VERSION_LENGTH; i++) {
            String vs = (i < vsl) ? vss[i] : "0";

            Integer ivs = Integer.parseInt(vs);

            if (ivs > ANDROID_VIDEO_SUPPORT_VERSION_ARRAY[i]) {
                return true;
            } else if (ivs < ANDROID_VIDEO_SUPPORT_VERSION_ARRAY[i]) {
                return false;
            }
        }
        return true;
    }


    private static boolean androidLikeSupportVersion(String version) {
        if (StringUtils.isBlank(version)) return false;
        String[] vss = version.split("\\.");
        if (vss == null) return false;
        int vsl = vss.length;
        for (int i = 0; i < ANDROID_LIKE_VERSION_LENGTH; i++) {
            String vs = (i < vsl) ? vss[i] : "0";

            Integer ivs = Integer.parseInt(vs);

            if (ivs > ANDROID_LIKE_SUPPORT_VERSION_ARRAY[i]) {
                return true;
            } else if (ivs < ANDROID_LIKE_SUPPORT_VERSION_ARRAY[i]) {
                return false;
            }
        }
        return true;
    }


    public static boolean isSupportVersion(String version, boolean isIos) {

        if (StringUtils.isBlank(version)) return false;
        String s = version.substring(version.indexOf("chatgame-") + 9);
        try {
            if (isIos) {
                return iosSupportVersion(s);
            } else {
                return androidSupportVersion(s);
            }
        } catch (Exception e) {
        }

        return false;

    }



    public static boolean isLikeSupportVersion(String version, boolean isIos) {

        if (StringUtils.isBlank(version)) return false;
        String s = version.substring(version.indexOf("chatgame-") + 9);
        try {
            if (isIos) {
                return iosLikeSupportVersion(s);
            } else {
                return androidLikeSupportVersion(s);
            }
        } catch (Exception e) {
        }

        return false;

    }


    public static boolean isSupportVersion(UserBehaviouAttr uba) {

        if (uba == null) return false;

        String version = uba.getClientVersion();

        if (StringUtils.isBlank(version)) return false;

        String ua = uba.getUa();

        if (StringUtils.isBlank(ua)) return false;

        return isSupportVersion(version, ua.toLowerCase().contains("ios"));

    }



    public static boolean isLikeSupportVersion(UserBehaviouAttr uba) {

        if (uba == null) return false;

        String version = uba.getClientVersion();

        if (StringUtils.isBlank(version)) return false;

        String ua = uba.getUa();

        if (StringUtils.isBlank(ua)) return false;

        return isLikeSupportVersion(version, ua.toLowerCase().contains("ios"));

    }


}
