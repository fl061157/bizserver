package com.handwin.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;

/**
 * Created by Danny on 2015-01-10.
 */
public interface JedisCallback<T> {
    T execute(Jedis jedis) throws JedisException;
}
