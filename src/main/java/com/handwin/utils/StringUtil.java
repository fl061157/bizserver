package com.handwin.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: chenxy
 * Date: 13-5-16
 * Time: 上午1:00
 */
public class StringUtil {

    public static String serverType2String(int serverType) {
        if (serverType == 1) {
            return "HTTP";
        } else if (serverType == 2) {
            return "TCP";
        }
        if (serverType == 3) {
            return "UDP";
        }
        if (serverType == 4) {
            return "Monitor";
        }
        if (serverType == 5) {
            return "UserManage";
        }
        if (serverType == 6) {
            return "Game";
        }
        return null;
    }

    private static Pattern phonePattern = Pattern.compile("^((13[0-9])|(147)|(15[^4,\\D])|(18[0-9]))\\d{8}$");

    public static boolean isMobileNO(String mobile) {
        Matcher m = phonePattern.matcher(mobile);
        return m.matches();
    }

    public static String clearMobileNo(String mobile) {
        return mobile.replaceAll("(\\s+|-|\\+86)", "");
    }

    public static boolean isSet(String value) {
        return value != null && value.trim().length() > 0;
    }

    public static void main(String[] args) {
        System.out.println(clearMobileNo("00861-395-506-8517"));
        System.out.println(isMobileNO("15813933298"));
        System.out.println(isMobileNO("10813933298"));

    }

}
