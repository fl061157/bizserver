package com.handwin.persist;

import com.handwin.redis.BinaryJedisCluster;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

/**
 * Created by fangliang on 5/1/15.
 */
@Service("statusClusterStoreImpl")
public class StatusClusterStoreImpl implements StatusStore {

    private final Logger logger = LoggerFactory.getLogger(StatusClusterStoreImpl.class);

    @Autowired
    private BinaryJedisCluster binaryJedisCluster;

    private final static String OK = "OK";

    @Override
    public byte[] get(byte[] key) throws Exception {
        try {
            return binaryJedisCluster.get(key);
        } catch (Throwable e) {
            logger.error("Redis Get Error key:{} ", new String(key), e);
            throw e;
        }
    }

    @Override
    public String get(String key) {
        try {
            return binaryJedisCluster.get(key);
        } catch (Throwable e) {
            logger.error("Redis Get Error key:{} ", key, e);
            throw e;
        }
    }

    @Override
    public boolean set(byte[] key, byte[] value, int seconds) {
        try {
            String r = binaryJedisCluster.setex(key, seconds, value);
            return StringUtils.isNotBlank(r) && r.toUpperCase().equals(OK);
        } catch (Throwable e) {
            logger.error("Redis Set Error key:{}", new String(key), e);
            throw e;
        }
    }

    @Override
    public boolean setEx(String key, int ttl, String value) {
        try {
            String r = binaryJedisCluster.setex(key, ttl, value);
            return StringUtils.isNotBlank(r) && r.toUpperCase().equals(OK);
        } catch (Throwable e) {
            logger.error("Redis Set Error key:{}", key, e);
            throw e;
        }
    }

    @Override
    public boolean setEx(byte[] key, int ttl, byte[] value) {
        try {
            String r = binaryJedisCluster.setex(key, ttl, value);
            return StringUtils.isNotBlank(r) && r.toUpperCase().equals(OK);
        } catch (Throwable e) {
            logger.error("Redis Set Error key:{}", new String(key), e);
            throw e;
        }
    }

    @Override
    public boolean set(String key, String value) {
        try {
            String r = binaryJedisCluster.set(key, value);
            return StringUtils.isNotBlank(r) && StringUtils.trim(r).toUpperCase().equals("OK");
        } catch (Throwable e) {
            logger.error("Redis Set Error key:{} ", key, e);
            throw e;
        }
    }

    @Override
    public boolean expire(byte[] key, int seconds) {
        try {
            return binaryJedisCluster.expire(key, seconds) == 1;
        } catch (Throwable e) {
            logger.error("Redis Expire Error key:{}", new String(key), e);
            throw e;
        }
    }


    @Override
    public boolean expire(String key, int seconds) throws Exception {
        try {
            return binaryJedisCluster.expire(key, seconds) == 1;
        } catch (Throwable e) {
            logger.error("Redis Expire Error key:{}", key, e);
            throw e;
        }
    }

    @Override
    public Set<byte[]> smembers(byte[] key) {
        try {
            return binaryJedisCluster.smembers(key);
        } catch (Throwable e) {
            logger.error("Redis Smembers Error key:{}", new String(key), e);
            throw e;
        }
    }


    @Override
    public Set<String> smembers(String key) throws Exception {
        try {
            return binaryJedisCluster.smembers(key);
        } catch (Throwable e) {
            logger.error("Redis Smembers Error key:{}", new String(key), e);
            throw e;
        }
    }

    @Override
    public boolean sAdd(byte[] key, byte[] value) {
        try {
            return binaryJedisCluster.sadd(key, value) == 1;
        } catch (Throwable e) {
            logger.error("Redis Sadd Error key:{} ", new String(key), e);
            throw e;
        }
    }

    @Override
    public boolean sAdd(String key, String value) {
        try {
            return binaryJedisCluster.sadd(key, value) == 1;
        } catch (Throwable e) {
            logger.error("Redis Sadd Error key:{} ", new String(key), e);
            throw e;
        }
    }


    @Override
    public boolean sRem(byte[] key, byte[] value) {
        try {
            return binaryJedisCluster.srem(key, value) == 1;
        } catch (Throwable e) {
            logger.error("Redis Srem Error key:{}", new String(key), e);
            throw e;
        }
    }

    @Override
    public boolean sRem(String key, String value) throws Exception {
        try {
            return binaryJedisCluster.srem(key, value) == 1;
        } catch (Throwable e) {
            logger.error("Redis Srem Error key:{}", new String(key), e);
            throw e;
        }
    }

    @Override
    public boolean del(byte[] key) {
        try {
            return binaryJedisCluster.del(key) > 0;
        } catch (Throwable e) {
            logger.error("Redis Del Error key:{}", new String(key), e);
            throw e;
        }
    }

    @Override
    public boolean del(String key) throws Exception {
        try {
            return binaryJedisCluster.del(key) > 0;
        } catch (Throwable e) {
            logger.error("Redis Del Error key:{}", key, e);
            throw e;
        }
    }

    @Override
    public boolean exists(byte[] key) {
        try {
            return binaryJedisCluster.exists(key);
        } catch (Throwable e) {
            logger.error("Redis Exists Error key:{}", new String(key), e);
            throw e;
        }
    }

    @Override
    public boolean zRem(String key, String value) {
        try {
            return binaryJedisCluster.zrem(key, value) == 1;
        } catch (Exception e) {
            logger.error("Redis Zrem Error key:{} ", new String(key), e);
            throw e;
        }
    }

    @Override
    public void zAdd(String key, double score, String value) throws Exception {
        try {
            binaryJedisCluster.zadd(key, score, value);
        } catch (Exception e) {
            logger.error("Redis zAdd Error key:{}, value:{}  ", new String(key), value, e);
            throw e;
        }
    }

    @Override
    public Map<String, String> hGetAll(String key) throws Exception {
        try {
            return binaryJedisCluster.hgetAll(key);
        } catch (Exception e) {
            logger.error("Redis hGetAll Error key:{} ", key, e);
            throw e;
        }
    }

    @Override
    public Map<byte[], byte[]> hGetAll(byte[] key) throws Exception {
        try {
            return binaryJedisCluster.hgetAll(key);
        } catch (Exception e) {
            logger.error("Redis hGetAll Error key:{} ", new String(key), e);
            throw e;
        }
    }

    @Override
    public String hGet(String key, String field) throws Exception {
        try {
            return binaryJedisCluster.hget(key, field);
        } catch (Exception e) {
            logger.error("Redis hGet Error key:{} , field:{} ", key, field, e);
            throw e;
        }
    }

    @Override
    public byte[] hGet(byte[] key, byte[] field) throws Exception {
        try {
            return binaryJedisCluster.hget(key, field);
        } catch (Exception e) {
            logger.error("Redis hGet Error key:{} , field:{} ", new String(key), new String(field), e);
            throw e;
        }
    }

    @Override
    public Long hSet(String key, String field, String value) throws Exception {
        try {
            return binaryJedisCluster.hset(key, field, value);
        } catch (Exception e) {
            logger.error("Redis hSet Error key:{} , field:{} , value:{}  ", key, field, value, e);
            throw e;
        }
    }

    @Override
    public Long hSet(byte[] key, byte[] field, byte[] value) throws Exception {

        try {
            return binaryJedisCluster.hset(key, field, value);
        } catch (Exception e) {
            logger.error("Redis hSet Error key:{} , field:{} , value:{}  ", new String(key), new String(field), new String(value), e);
            throw e;
        }

    }

    @Override
    public Long hDel(String key, String... field) throws Exception {
        try {
            return binaryJedisCluster.hdel(key, field);
        } catch (Exception e) {
            logger.error("Redis hDel Error key:{} ", key, e);
            throw e;
        }
    }

    @Override
    public Long hDel(byte[] key, byte[]... field) throws Exception {
        try {
            return binaryJedisCluster.hdel(key, field);
        } catch (Exception e) {
            logger.error("Redis hDel Error key:{} ", key, e);
            throw e;
        }
    }


    @Override
    public boolean hExists(String key, String field) throws Exception {
        try {
            return binaryJedisCluster.hexists(key, field);
        } catch (Exception e) {
            logger.error("Redis hExists Error key:{} , field:{} ", key, field, e);
            throw e;
        }
    }

    @Override
    public void incr(String key) throws Exception {
        try {
            binaryJedisCluster.incr(key);
        } catch (Exception e) {
            logger.error("Redis incr Error key:{}", key, e);
            throw e;
        }
    }

    @Override
    public void decr(String key) throws Exception {

        try {
            binaryJedisCluster.decr(key);
        } catch (Exception e) {
            logger.error("Redis decr Error key:{}", key, e);
            throw e;
        }

    }
}
