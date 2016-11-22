package com.handwin.redis;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.RedisConnectionFailureException;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by fangliang on 4/1/15.
 */
public class RedisCluster implements DisposableBean, InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(RedisCluster.class);

    private List<JedisPoolWrapper> jedisPoolList = new ArrayList<>();

    private static ThreadLocal<JedisPoolWrapper> jedisPoolWrapperThreadLocal = new ThreadLocal<>();

    public RedisCluster(String hostStr, final JedisPoolConfig poolConfig) {
        logger.info("init redis cluster host info {}, config {}",
                hostStr, ToStringBuilder.reflectionToString(poolConfig, ToStringStyle.MULTI_LINE_STYLE));
        init(RedisAddressUtil.parse(hostStr), poolConfig);
    }

    private void init(Set<HostAndPort> jedisClusterNode, final JedisPoolConfig poolConfig) {
        if (jedisClusterNode == null || jedisClusterNode.size() == 0) {
            throw new JedisException("No redis node exception !");
        }

        for (HostAndPort hap : jedisClusterNode) {
            JedisPoolWrapper jedisPool = new JedisPoolWrapper(poolConfig, hap.getHost(), hap.getPort());
            jedisPoolList.add(jedisPool);
        }
    }

    public JedisTemplate getJedisTemplate() {

        JedisPoolWrapper jedisPoolWrapper = jedisPoolWrapperThreadLocal.get();
        if (jedisPoolWrapper == null || jedisPoolWrapper.isClosed()) {
            jedisPoolWrapper =
                    jedisPoolList.get((int) (Thread.currentThread().getId() % jedisPoolList.size()));
        }

        JedisTemplate jedisTemplate = getJedisTemplate(jedisPoolWrapper, 0);

        return jedisTemplate;
    }

    protected JedisTemplate getJedisTemplate(JedisPoolWrapper jedisPoolWrapper, int times) {
        if (times >= jedisPoolList.size()) {
            throw new RedisConnectionFailureException("all redis pools error.");
        }
        JedisTemplate jedisTemplate = null;
        try {
            jedisTemplate = new JedisTemplate(this, jedisPoolWrapper, jedisPoolWrapper.getResource());
            jedisPoolWrapperThreadLocal.set(jedisPoolWrapper);
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            jedisTemplate = getJedisTemplate(jedisPoolWrapper.getNext(), times + 1);
        }
        return jedisTemplate;
    }

    @Override
    public void destroy() throws Exception {
        logger.info("close redis pools.");
        for (JedisPoolWrapper p : jedisPoolList) {
            p.setNext(null);
            p.close();
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (jedisPoolList.size() < 1) {
            throw new Exception("redis connect pool size is zero.");
        }
        int size = jedisPoolList.size();
        for (int i = 0; i < size; i++) {
            JedisPoolWrapper p = jedisPoolList.get(i);
            if (i == size - 1) {
                p.setNext(jedisPoolList.get(0));
            } else {
                p.setNext(jedisPoolList.get(i + 1));
            }
        }
    }
}
