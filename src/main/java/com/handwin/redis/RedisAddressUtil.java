package com.handwin.redis;

import org.apache.commons.lang.StringUtils;
import redis.clients.jedis.HostAndPort;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by fangliang on 5/1/15.
 */
public class RedisAddressUtil {

    public static Set<HostAndPort> parse(String hostStr) {

        if (StringUtils.isBlank(hostStr)) {
            return null;
        }
        String[] hostAndPortStrArray = StringUtils.split(hostStr, ",; \t");
        if (hostAndPortStrArray == null) {
            return null;
        }
        Set<HostAndPort> set = new HashSet<>();
        for (String hostAndPortStr : hostAndPortStrArray) {
            String[] hpStr = StringUtils.split(hostAndPortStr, ":") ;
            HostAndPort hp = new HostAndPort(hpStr[0], Integer.parseInt(hpStr[1]));
            set.add(hp);
        }
        return set;
    }


}
