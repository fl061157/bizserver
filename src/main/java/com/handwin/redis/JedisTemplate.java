package com.handwin.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.RedisConnectionFailureException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;

/**
 * Created by Danny on 2015-01-10.
 */
public class JedisTemplate {

    private static final Logger logger = LoggerFactory.getLogger(JedisTemplate.class);

    private RedisCluster jedisCluster;
    private JedisPoolWrapper jedisPoolWrapper;
    private Jedis jedis;

    public JedisTemplate(RedisCluster jedisCluster, JedisPoolWrapper jedisPoolWrapper, Jedis resource) {
        this.jedisCluster = jedisCluster;
        this.jedisPoolWrapper = jedisPoolWrapper;
        this.jedis = resource;
    }

    public <T> T execute(JedisCallback<T> action) throws DataAccessException{
        T result = null;
        try {
            result = action.execute(jedis);
        }catch (JedisConnectionException e) {
            logger.error("try another redis; error:" + e.getMessage(), e);
            JedisTemplate jedisTemplate = jedisCluster.getJedisTemplate(jedisPoolWrapper.getNext(), 0);
            result = jedisTemplate.execute(action);
        } catch (Throwable e){
            logger.error(e.getMessage(), e);
            throw new RedisConnectionFailureException(e.getMessage(), e);
        } finally {
            if (jedis.getClient().isBroken()) {
                logger.warn("redis connection broken.");
                jedisPoolWrapper.returnBrokenResource(jedis);
            } else {
                jedisPoolWrapper.returnResource(jedis);
            }
        }
        return result;
    }

    public JedisPoolWrapper getJedisPoolWrapper() {
        return jedisPoolWrapper;
    }

    public void setJedisPoolWrapper(JedisPoolWrapper jedisPoolWrapper) {
        this.jedisPoolWrapper = jedisPoolWrapper;
    }

    public Jedis getJedis() {
        return jedis;
    }

    public void setJedis(Jedis jedis) {
        this.jedis = jedis;
    }

}
