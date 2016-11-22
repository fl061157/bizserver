package com.handwin.persist;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Map;
import java.util.Set;

/**
 * @author fangliang
 */
public class StatusStoreImpl implements StatusStore {

    private static final Logger logger = LoggerFactory.getLogger(StatusStoreImpl.class);

    @Autowired
    private RedisTemplate<byte[], byte[]> redisTemplate;

    @Override
    public byte[] get(final byte[] key) {
        return redisTemplate.execute((RedisConnection connection) -> {
            byte[] result = connection.get(key);
            return result;
        });
    }

    @Override
    public boolean setEx(String key, int ttl, String value) {
        return redisTemplate.execute((RedisConnection connection) -> {
            connection.setEx(key.getBytes(), ttl, value.getBytes());
            return true;
        });
    }

    @Override
    public String get(String key) {
        return redisTemplate.execute((RedisConnection connection) -> {
            byte[] result = connection.get(key.getBytes());
            return new String(result);
        });
    }

    @Override
    public boolean del(final byte[] key) {
        RedisCallback<Void> callBack = connection -> {
            connection.del(key);
            return null;
        };
        try {
            redisTemplate.execute(callBack);
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }


    @Override
    public boolean del(String key) throws Exception {
        return false;
    }

    @Override
    public boolean sAdd(final byte[] key, final byte[] value) {
        RedisCallback<Void> callBack = connection -> {
            connection.sAdd(key, value);
            return null;
        };
        try {
            redisTemplate.execute(callBack);
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean sRem(final byte[] key, final byte[] value) {
        RedisCallback<Void> callBack = connection -> {
            connection.sRem(key, value);
            return null;
        };
        try {
            redisTemplate.execute(callBack);
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean sRem(String key, String value) throws Exception {
        return false;
    }

    @Override
    public boolean sAdd(String key, String value) throws Exception {
        return false;
    }

    @Override
    public Set<String> smembers(String key) throws Exception {
        return null;
    }

    @Override
    public boolean set(final byte[] key, final byte[] value, final int seconds) {
        RedisCallback<Void> callBack = connection -> {
            connection.setEx(key, seconds, value);
            return null;
        };
        try {
            redisTemplate.execute(callBack);
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }

    @Override
    public Set<byte[]> smembers(final byte[] key) {
        RedisCallback<Set<byte[]>> callBack = connection -> {
            Set<byte[]> result = connection.sMembers(key);
            return result;
        };
        return redisTemplate.execute(callBack);
    }

    @Override
    public boolean expire(final byte[] key, final int seconds) {
        RedisCallback<Boolean> callBack = connection -> connection.expire(key, seconds);
        try {
            return redisTemplate.execute(callBack);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean set(String key, String value) {
        return false;
    }


    @Override
    public boolean exists(byte[] key) {
        return false;
    }

    @Override
    public boolean setEx(byte[] key, int ttl, byte[] value) {
        return false;
    }

    @Override
    public boolean zRem(String key, String value) {
        return false;
    }


    @Override
    public void zAdd(String key, double score, String value) throws Exception {

    }

    @Override
    public Map<String, String> hGetAll(String key) throws Exception {
        return null;
    }

    @Override
    public Map<byte[], byte[]> hGetAll(byte[] key) throws Exception {
        return null;
    }

    @Override
    public Long hSet(String key, String field, String value) throws Exception {
        return null;
    }

    @Override
    public Long hSet(byte[] key, byte[] field, byte[] value) throws Exception {
        return null;
    }

    @Override
    public Long hDel(String key, String... field) throws Exception {
        return null;
    }

    @Override
    public Long hDel(byte[] key, byte[]... field) throws Exception {
        return null;
    }

    @Override
    public boolean expire(String key, int seconds) throws Exception {
        return false;
    }

    @Override
    public String hGet(String key, String field) throws Exception {
        return null;
    }

    @Override
    public byte[] hGet(byte[] key, byte[] field) throws Exception {
        return new byte[0];
    }

    @Override
    public boolean hExists(String key, String field) throws Exception {
        return false;
    }


    @Override
    public void incr(String key) throws Exception {

    }

    @Override
    public void decr(String key) throws Exception {

    }
}
