package com.handwin.bean;

import org.apache.commons.collections.map.HashedMap;

import java.util.Map;

/**
 * Created by fangliang on 16/8/2.
 */
public enum Platform {

    Mobile("mobile", 1),
    Web("web", 5),
    Proxy("proxy", 1);

    private String platform;

    private int count;

    Platform(String platform, int count) {
        this.platform = platform;
        this.count = count;
    }

    public String getPlatform() {
        return platform;
    }

    public int getCount() {
        return count;
    }

    public final static String PLATFROM = "platform";

    static Map<String, Platform> MAP = new HashedMap();

    static {

        for (Platform p : Platform.values()) {
            MAP.put(p.getPlatform().toLowerCase(), p);
        }

    }


    public static Platform getPlatform(String platform) {
        platform = platform.toLowerCase();
        Platform p = MAP.get(platform);
        return p != null ? p : Mobile;
    }


}
