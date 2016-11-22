package com.handwin.persist;

import java.util.Map;
import java.util.Set;

/**
 * @author fangliang
 */
public interface StatusStore {

    public byte[] get(final byte[] key) throws Exception;

    public String get(String key) throws Exception;

    public boolean set(byte[] key, byte[] value, int seconds) throws Exception;

    public boolean set(String key, String value) throws Exception;

    public boolean setEx(String key, int ttl, String value) throws Exception;

    public boolean setEx(byte[] key, int ttl, byte[] value) throws Exception;

    public boolean expire(byte[] key, int seconds) throws Exception;

    public boolean expire(String key, int seconds) throws Exception;

    public Set<byte[]> smembers(byte[] key) throws Exception;

    public Set<String> smembers(String key) throws Exception;

    public boolean sAdd(byte[] key, byte[] value) throws Exception;

    public boolean sAdd(String key, String value) throws Exception;

    public boolean sRem(byte[] key, byte[] value) throws Exception;

    public boolean sRem(String key, String value) throws Exception;

    public boolean del(byte[] key) throws Exception;

    public boolean del(String key) throws Exception;

    public boolean exists(byte[] key) throws Exception;

    public boolean zRem(String key, String value) throws Exception;

    public void zAdd(String key, double score, String value) throws Exception;

    public Map<String, String> hGetAll(String key) throws Exception;

    public Map<byte[], byte[]> hGetAll(byte[] key) throws Exception;

    public String hGet(String key, String field) throws Exception;

    public byte[] hGet(byte[] key, byte[] field) throws Exception;

    public Long hSet(String key, String field, String value) throws Exception;

    public Long hSet(byte[] key, byte[] field, byte[] value) throws Exception;

    public Long hDel(String key, String... field) throws Exception;

    public Long hDel(byte[] key, byte[]... field) throws Exception;

    public boolean hExists(String key, String field) throws Exception;

    public void incr(String key) throws Exception;

    public void decr(String key) throws Exception;


}
