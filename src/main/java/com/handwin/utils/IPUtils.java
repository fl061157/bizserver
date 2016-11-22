package com.handwin.utils;


public class IPUtils {
    public static int ip2int(String address) {
        String[] as = address.split("\\.");
        int out = 0;
        for (int i = 0; i < as.length; i++) {
            out += Integer.parseInt(as[i]) << (8 * i);
        }
        return out;
    }

    public static String int2ip(int aInt) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < 4; i++) {
            if (i != 0) {
                sb.append(".");
            }
            sb.append((aInt >> (8 * i)) & 0x000000FF);
        }
        return sb.toString();
    }
}
