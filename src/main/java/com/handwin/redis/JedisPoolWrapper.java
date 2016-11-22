package com.handwin.redis;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPool;

/**
 * Created by Danny on 2015-01-11.
 */
public class JedisPoolWrapper extends JedisPool {

    private static final Logger logger = LoggerFactory.getLogger(JedisTemplate.class);

    private JedisPoolWrapper next;

    public JedisPoolWrapper(GenericObjectPoolConfig poolConfig, String host, int port) {
        super(poolConfig, host, port);
    }

    public JedisPoolWrapper getNext() {
        return next;
    }

    public void setNext(JedisPoolWrapper next) {
        this.next = next;
    }
}
