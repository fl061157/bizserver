package com.handwin.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import redis.clients.jedis.*;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by fangliang on 16/1/15.
 */
public class BinaryJedisCluster implements JedisCommands, BinaryJedisCommands, BasicCommands, Closeable, DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(BinaryJedisCluster.class);

    private int timeout;
    private int maxRedirections;

    private JedisClusterConnectionHandler connectionHandler;

    public BinaryJedisCluster(String hosts, int timeout, int maxRedirections,
                              final JedisPoolConfig poolConfig) {
        Set<HostAndPort> nodes;
        try {
            nodes = RedisAddressUtil.parse(hosts);
        } catch (Exception e) {
            logger.error("parse hosts to nodes error hosts:{} ", hosts, e);
            throw new RuntimeException(e);
        }
        this.connectionHandler = new JedisSlotBasedConnectionHandler(nodes, poolConfig, timeout);
        this.timeout = timeout;
        this.maxRedirections = maxRedirections;
    }


    @Override
    public String ping() {
        return new JedisClusterCommand<String>(connectionHandler, maxRedirections) {
            @Override
            public String execute(Jedis connection) {
                return connection.ping();
            }
        }.run(null);
    }

    @Override
    public String quit() {
        return new JedisClusterCommand<String>(connectionHandler, maxRedirections) {
            @Override
            public String execute(Jedis connection) {
                return connection.quit();
            }
        }.run(null);
    }

    @Override
    public String flushDB() {
        return new JedisClusterCommand<String>(connectionHandler, maxRedirections) {
            @Override
            public String execute(Jedis connection) {
                return connection.flushDB();
            }
        }.run(null);
    }

    @Override
    public Long dbSize() {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.dbSize();
            }
        }.run(null);
    }

    @Override
    public String select(int index) {
        return new JedisClusterCommand<String>(connectionHandler, maxRedirections) {
            @Override
            public String execute(Jedis connection) {
                return connection.select(index);
            }
        }.run(null);
    }

    @Override
    public String flushAll() {
        return new JedisClusterCommand<String>(connectionHandler, maxRedirections) {
            @Override
            public String execute(Jedis connection) {
                return connection.flushAll();
            }
        }.run(null);
    }

    @Override
    public String auth(String password) {
        return new JedisClusterCommand<String>(connectionHandler, maxRedirections) {
            @Override
            public String execute(Jedis connection) {
                return connection.auth(password);
            }
        }.run(null);
    }

    @Override
    public String save() {
        return new JedisClusterCommand<String>(connectionHandler, maxRedirections) {
            @Override
            public String execute(Jedis connection) {
                return connection.save();
            }
        }.run(null);
    }

    @Override
    public String bgsave() {
        return new JedisClusterCommand<String>(connectionHandler, maxRedirections) {
            @Override
            public String execute(Jedis connection) {
                return connection.bgsave();
            }
        }.run(null);
    }

    @Override
    public String bgrewriteaof() {
        return new JedisClusterCommand<String>(connectionHandler, maxRedirections) {
            @Override
            public String execute(Jedis connection) {
                return connection.bgrewriteaof();
            }
        }.run(null);
    }

    @Override
    public Long lastsave() {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.lastsave();
            }
        }.run(null);
    }

    @Override
    public String shutdown() {
        return new JedisClusterCommand<String>(connectionHandler, maxRedirections) {
            @Override
            public String execute(Jedis connection) {
                return connection.shutdown();
            }
        }.run(null);
    }

    @Override
    public String info() {
        return new JedisClusterCommand<String>(connectionHandler, maxRedirections) {
            @Override
            public String execute(Jedis connection) {
                return connection.info();
            }
        }.run(null);
    }

    @Override
    public String info(String section) {
        return new JedisClusterCommand<String>(connectionHandler, maxRedirections) {
            @Override
            public String execute(Jedis connection) {
                return connection.info(section);
            }
        }.run(null);
    }

    @Override
    public String slaveof(String host, int port) {
        return new JedisClusterCommand<String>(connectionHandler, maxRedirections) {
            @Override
            public String execute(Jedis connection) {
                return connection.slaveof(host, port);
            }
        }.run(null);
    }

    @Override
    public String slaveofNoOne() {
        return new JedisClusterCommand<String>(connectionHandler, maxRedirections) {
            @Override
            public String execute(Jedis connection) {
                return connection.slaveofNoOne();
            }
        }.run(null);
    }

    @Override
    public Long getDB() {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.getDB();
            }
        }.run(null);
    }

    @Override
    public String debug(DebugParams params) {
        return new JedisClusterCommand<String>(connectionHandler, maxRedirections) {
            @Override
            public String execute(Jedis connection) {
                return connection.debug(params);
            }
        }.run(null);
    }

    @Override
    public String configResetStat() {
        return new JedisClusterCommand<String>(connectionHandler, maxRedirections) {
            @Override
            public String execute(Jedis connection) {
                return connection.configResetStat();
            }
        }.run(null);
    }

    @Override
    public Long waitReplicas(int replicas, long t) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.waitReplicas(replicas, t);
            }
        }.run(null);
    }

    @Override
    public String set(byte[] key, byte[] value) {
        return new JedisClusterCommand<String>(connectionHandler, maxRedirections) {
            @Override
            public String execute(Jedis connection) {
                return connection.set(key, value);
            }
        }.run(new String(key));
    }

    @Override
    public byte[] get(byte[] key) {
        return new JedisClusterCommand<byte[]>(connectionHandler, maxRedirections) {
            @Override
            public byte[] execute(Jedis connection) {
                return connection.get(key);
            }
        }.run(new String(key));
    }

    @Override
    public Boolean exists(byte[] key) {
        return new JedisClusterCommand<Boolean>(connectionHandler, maxRedirections) {
            @Override
            public Boolean execute(Jedis connection) {
                return connection.exists(key);
            }
        }.run(new String(key));
    }

    @Override
    public Long persist(byte[] key) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.persist(key);
            }
        }.run(new String(key));
    }

    @Override
    public String type(byte[] key) {
        return new JedisClusterCommand<String>(connectionHandler, maxRedirections) {
            @Override
            public String execute(Jedis connection) {
                return connection.type(key);
            }
        }.run(new String(key));
    }

    @Override
    public Long expire(byte[] key, int seconds) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.expire(key, seconds);
            }
        }.run(new String(key));
    }

    @Override
    public Long expireAt(byte[] key, long unixTime) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.expireAt(key, unixTime);
            }
        }.run(new String(key));
    }

    @Override
    public Long ttl(byte[] key) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.ttl(key);
            }
        }.run(new String(key));
    }

    @Override
    public Boolean setbit(byte[] key, long offset, boolean value) {
        return new JedisClusterCommand<Boolean>(connectionHandler, maxRedirections) {
            @Override
            public Boolean execute(Jedis connection) {
                return connection.setbit(key, offset, value);
            }
        }.run(new String(key));
    }

    @Override
    public Boolean setbit(byte[] key, long offset, byte[] value) {
        return new JedisClusterCommand<Boolean>(connectionHandler, maxRedirections) {
            @Override
            public Boolean execute(Jedis connection) {
                return connection.setbit(key, offset, value);
            }
        }.run(new String(key));
    }

    @Override
    public Boolean getbit(byte[] key, long offset) {
        return new JedisClusterCommand<Boolean>(connectionHandler, maxRedirections) {
            @Override
            public Boolean execute(Jedis connection) {
                return connection.getbit(key, offset);
            }
        }.run(new String(key));
    }

    @Override
    public Long setrange(byte[] key, long offset, byte[] value) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.setrange(key, offset, value);
            }
        }.run(new String(key));
    }

    @Override
    public byte[] getrange(byte[] key, long startOffset, long endOffset) {
        return new JedisClusterCommand<byte[]>(connectionHandler, maxRedirections) {
            @Override
            public byte[] execute(Jedis connection) {
                return connection.getrange(key, startOffset, endOffset);
            }
        }.run(new String(key));
    }

    @Override
    public byte[] getSet(byte[] key, byte[] value) {
        return new JedisClusterCommand<byte[]>(connectionHandler, maxRedirections) {
            @Override
            public byte[] execute(Jedis connection) {
                return connection.getSet(key, value);
            }
        }.run(new String(key));
    }

    @Override
    public Long setnx(byte[] key, byte[] value) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.setnx(key, value);
            }
        }.run(new String(key));
    }

    @Override
    public String setex(byte[] key, int seconds, byte[] value) {
        return new JedisClusterCommand<String>(connectionHandler, maxRedirections) {
            @Override
            public String execute(Jedis connection) {
                return connection.setex(key, seconds, value);
            }
        }.run(new String(key));
    }

    @Override
    public Long decrBy(byte[] key, long integer) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.decrBy(key, integer);
            }
        }.run(new String(key));
    }

    @Override
    public Long decr(byte[] key) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.decr(key);
            }
        }.run(new String(key));
    }

    @Override
    public Long incrBy(byte[] key, long integer) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.incrBy(key, integer);
            }
        }.run(new String(key));
    }

    @Override
    public Double incrByFloat(byte[] key, double value) {
        return new JedisClusterCommand<Double>(connectionHandler, maxRedirections) {
            @Override
            public Double execute(Jedis connection) {
                return connection.incrByFloat(key, value);
            }
        }.run(new String(key));
    }

    @Override
    public Long incr(byte[] key) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.incr(key);
            }
        }.run(new String(key));
    }

    @Override
    public Long append(byte[] key, byte[] value) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.append(key, value);
            }
        }.run(new String(key));
    }

    @Override
    public byte[] substr(byte[] key, int start, int end) {
        return new JedisClusterCommand<byte[]>(connectionHandler, maxRedirections) {
            @Override
            public byte[] execute(Jedis connection) {
                return connection.substr(key, start, end);
            }
        }.run(new String(key));
    }

    @Override
    public Long hset(byte[] key, byte[] field, byte[] value) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.hset(key, field, value);
            }
        }.run(new String(key));
    }

    @Override
    public byte[] hget(byte[] key, byte[] field) {
        return new JedisClusterCommand<byte[]>(connectionHandler, maxRedirections) {
            @Override
            public byte[] execute(Jedis connection) {
                return connection.hget(key, field);
            }
        }.run(new String(key));
    }

    @Override
    public Long hsetnx(byte[] key, byte[] field, byte[] value) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.hsetnx(key, field, value);
            }
        }.run(new String(key));
    }

    @Override
    public String hmset(byte[] key, Map<byte[], byte[]> hash) {
        return new JedisClusterCommand<String>(connectionHandler, maxRedirections) {
            @Override
            public String execute(Jedis connection) {
                return connection.hmset(key, hash);
            }
        }.run(new String(key));
    }

    @Override
    public List<byte[]> hmget(byte[] key, byte[]... fields) {
        return new JedisClusterCommand<List<byte[]>>(connectionHandler, maxRedirections) {
            @Override
            public List<byte[]> execute(Jedis connection) {
                return connection.hmget(key, fields);
            }
        }.run(new String(key));
    }

    @Override
    public Long hincrBy(byte[] key, byte[] field, long value) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.hincrBy(key, field, value);
            }
        }.run(new String(key));
    }

    @Override
    public Double hincrByFloat(byte[] key, byte[] field, double value) {
        return new JedisClusterCommand<Double>(connectionHandler, maxRedirections) {
            @Override
            public Double execute(Jedis connection) {
                return connection.hincrByFloat(key, field, value);
            }
        }.run(new String(key));
    }

    @Override
    public Boolean hexists(byte[] key, byte[] field) {
        return new JedisClusterCommand<Boolean>(connectionHandler, maxRedirections) {
            @Override
            public Boolean execute(Jedis connection) {
                return connection.hexists(key, field);
            }
        }.run(new String(key));
    }

    @Override
    public Long hdel(byte[] key, byte[]... field) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.hdel(key, field);
            }
        }.run(new String(key));
    }

    @Override
    public Long hlen(byte[] key) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.hlen(key);
            }
        }.run(new String(key));
    }

    @Override
    public Set<byte[]> hkeys(byte[] key) {
        return new JedisClusterCommand<Set<byte[]>>(connectionHandler, maxRedirections) {
            @Override
            public Set<byte[]> execute(Jedis connection) {
                return connection.hkeys(key);
            }
        }.run(new String(key));
    }

    @Override
    public Collection<byte[]> hvals(byte[] key) {
        return new JedisClusterCommand<Collection<byte[]>>(connectionHandler, maxRedirections) {
            @Override
            public Collection<byte[]> execute(Jedis connection) {
                return connection.hvals(key);
            }
        }.run(new String(key));
    }

    @Override
    public Map<byte[], byte[]> hgetAll(byte[] key) {
        return new JedisClusterCommand<Map<byte[], byte[]>>(connectionHandler, maxRedirections) {
            @Override
            public Map<byte[], byte[]> execute(Jedis connection) {
                return connection.hgetAll(key);
            }
        }.run(new String(key));
    }

    @Override
    public Long rpush(byte[] key, byte[]... args) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.rpush(key, args);
            }
        }.run(new String(key));
    }

    @Override
    public Long lpush(byte[] key, byte[]... args) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.lpush(key, args);
            }
        }.run(new String(key));
    }

    @Override
    public Long llen(byte[] key) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.llen(key);
            }
        }.run(new String(key));
    }

    @Override
    public List<byte[]> lrange(byte[] key, long start, long end) {
        return new JedisClusterCommand<List<byte[]>>(connectionHandler, maxRedirections) {
            @Override
            public List<byte[]> execute(Jedis connection) {
                return connection.lrange(key, start, end);
            }
        }.run(new String(key));
    }

    @Override
    public String ltrim(byte[] key, long start, long end) {
        return new JedisClusterCommand<String>(connectionHandler, maxRedirections) {
            @Override
            public String execute(Jedis connection) {
                return connection.ltrim(key, start, end);
            }
        }.run(new String(key));
    }

    @Override
    public byte[] lindex(byte[] key, long index) {
        return new JedisClusterCommand<byte[]>(connectionHandler, maxRedirections) {
            @Override
            public byte[] execute(Jedis connection) {
                return connection.lindex(key, index);
            }
        }.run(new String(key));
    }

    @Override
    public String lset(byte[] key, long index, byte[] value) {
        return new JedisClusterCommand<String>(connectionHandler, maxRedirections) {
            @Override
            public String execute(Jedis connection) {
                return connection.lset(key, index, value);
            }
        }.run(new String(key));
    }

    @Override
    public Long lrem(byte[] key, long count, byte[] value) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.lrem(key, count, value);
            }
        }.run(new String(key));
    }

    @Override
    public byte[] lpop(byte[] key) {
        return new JedisClusterCommand<byte[]>(connectionHandler, maxRedirections) {
            @Override
            public byte[] execute(Jedis connection) {
                return connection.lpop(key);
            }
        }.run(new String(key));
    }

    @Override
    public byte[] rpop(byte[] key) {
        return new JedisClusterCommand<byte[]>(connectionHandler, maxRedirections) {
            @Override
            public byte[] execute(Jedis connection) {
                return connection.rpop(key);
            }
        }.run(new String(key));
    }

    @Override
    public Long sadd(byte[] key, byte[]... member) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.sadd(key, member);
            }
        }.run(new String(key));
    }

    @Override
    public Set<byte[]> smembers(byte[] key) {
        return new JedisClusterCommand<Set<byte[]>>(connectionHandler, maxRedirections) {
            @Override
            public Set<byte[]> execute(Jedis connection) {
                return connection.smembers(key);
            }
        }.run(new String(key));
    }

    @Override
    public Long srem(byte[] key, byte[]... member) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.srem(key, member);
            }
        }.run(new String(key));
    }

    @Override
    public byte[] spop(byte[] key) {
        return new JedisClusterCommand<byte[]>(connectionHandler, maxRedirections) {
            @Override
            public byte[] execute(Jedis connection) {
                return connection.spop(key);
            }
        }.run(new String(key));
    }

    @Override
    public Long scard(byte[] key) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.scard(key);
            }
        }.run(new String(key));
    }

    @Override
    public Boolean sismember(byte[] key, byte[] member) {
        return new JedisClusterCommand<Boolean>(connectionHandler, maxRedirections) {
            @Override
            public Boolean execute(Jedis connection) {
                return connection.sismember(key, member);
            }
        }.run(new String(key));
    }

    @Override
    public byte[] srandmember(byte[] key) {
        return new JedisClusterCommand<byte[]>(connectionHandler, maxRedirections) {
            @Override
            public byte[] execute(Jedis connection) {
                return connection.srandmember(key);
            }
        }.run(new String(key));
    }

    @Override
    public List<byte[]> srandmember(byte[] key, int count) {
        return new JedisClusterCommand<List<byte[]>>(connectionHandler, maxRedirections) {
            @Override
            public List<byte[]> execute(Jedis connection) {
                return connection.srandmember(key, count);
            }
        }.run(new String(key));
    }

    @Override
    public Long strlen(byte[] key) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.strlen(key);
            }
        }.run(new String(key));
    }

    @Override
    public Long zadd(byte[] key, double score, byte[] member) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.zadd(key, score, member);
            }
        }.run(new String(key));
    }

    @Override
    public Long zadd(byte[] key, Map<byte[], Double> scoreMembers) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.zadd(key, scoreMembers);
            }
        }.run(new String(key));
    }

    @Override
    public Set<byte[]> zrange(byte[] key, long start, long end) {
        return new JedisClusterCommand<Set<byte[]>>(connectionHandler, maxRedirections) {
            @Override
            public Set<byte[]> execute(Jedis connection) {
                return connection.zrange(key, start, end);
            }
        }.run(new String(key));
    }

    @Override
    public Long zrem(byte[] key, byte[]... member) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.zrem(key, member);
            }
        }.run(new String(key));
    }

    @Override
    public Double zincrby(byte[] key, double score, byte[] member) {
        return new JedisClusterCommand<Double>(connectionHandler, maxRedirections) {
            @Override
            public Double execute(Jedis connection) {
                return connection.zincrby(key, score, member);
            }
        }.run(new String(key));
    }

    @Override
    public Long zrank(byte[] key, byte[] member) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.zrank(key, member);
            }
        }.run(new String(key));
    }

    @Override
    public Long zrevrank(byte[] key, byte[] member) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.zrevrank(key, member);
            }
        }.run(new String(key));
    }

    @Override
    public Set<byte[]> zrevrange(byte[] key, long start, long end) {
        return new JedisClusterCommand<Set<byte[]>>(connectionHandler, maxRedirections) {
            @Override
            public Set<byte[]> execute(Jedis connection) {
                return connection.zrevrange(key, start, end);
            }
        }.run(new String(key));
    }

    @Override
    public Set<Tuple> zrangeWithScores(byte[] key, long start, long end) {
        return new JedisClusterCommand<Set<Tuple>>(connectionHandler, maxRedirections) {
            @Override
            public Set<Tuple> execute(Jedis connection) {
                return connection.zrangeWithScores(key, start, end);
            }
        }.run(new String(key));
    }

    @Override
    public Set<Tuple> zrevrangeWithScores(byte[] key, long start, long end) {
        return new JedisClusterCommand<Set<Tuple>>(connectionHandler, maxRedirections) {
            @Override
            public Set<Tuple> execute(Jedis connection) {
                return connection.zrevrangeWithScores(key, start, end);
            }
        }.run(new String(key));
    }

    @Override
    public Long zcard(byte[] key) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.zcard(key);
            }
        }.run(new String(key));
    }

    @Override
    public Double zscore(byte[] key, byte[] member) {
        return new JedisClusterCommand<Double>(connectionHandler, maxRedirections) {
            @Override
            public Double execute(Jedis connection) {
                return connection.zscore(key, member);
            }
        }.run(new String(key));
    }

    @Override
    public List<byte[]> sort(byte[] key) {
        return new JedisClusterCommand<List<byte[]>>(connectionHandler, maxRedirections) {
            @Override
            public List<byte[]> execute(Jedis connection) {
                return connection.sort(key);
            }
        }.run(new String(key));
    }

    @Override
    public List<byte[]> sort(byte[] key, SortingParams sortingParameters) {
        return new JedisClusterCommand<List<byte[]>>(connectionHandler, maxRedirections) {
            @Override
            public List<byte[]> execute(Jedis connection) {
                return connection.sort(key, sortingParameters);
            }
        }.run(new String(key));
    }

    @Override
    public Long zcount(byte[] key, double min, double max) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.zcount(key, min, max);
            }
        }.run(new String(key));
    }

    @Override
    public Long zcount(byte[] key, byte[] min, byte[] max) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.zcount(key, min, max);
            }
        }.run(new String(key));
    }

    @Override
    public Set<byte[]> zrangeByScore(byte[] key, double min, double max) {
        return new JedisClusterCommand<Set<byte[]>>(connectionHandler, maxRedirections) {
            @Override
            public Set<byte[]> execute(Jedis connection) {
                return connection.zrangeByScore(key, min, max);
            }
        }.run(new String(key));
    }

    @Override
    public Set<byte[]> zrangeByScore(byte[] key, byte[] min, byte[] max) {
        return new JedisClusterCommand<Set<byte[]>>(connectionHandler, maxRedirections) {
            @Override
            public Set<byte[]> execute(Jedis connection) {
                return connection.zrangeByScore(key, min, max);
            }
        }.run(new String(key));
    }

    @Override
    public Set<byte[]> zrevrangeByScore(byte[] key, double max, double min) {
        return new JedisClusterCommand<Set<byte[]>>(connectionHandler, maxRedirections) {
            @Override
            public Set<byte[]> execute(Jedis connection) {
                return connection.zrevrangeByScore(key, min, max);
            }
        }.run(new String(key));
    }

    @Override
    public Set<byte[]> zrangeByScore(byte[] key, double min, double max, int offset, int count) {
        return new JedisClusterCommand<Set<byte[]>>(connectionHandler, maxRedirections) {
            @Override
            public Set<byte[]> execute(Jedis connection) {
                return connection.zrangeByScore(key, min, max, offset, count);
            }
        }.run(new String(key));
    }

    @Override
    public Set<byte[]> zrevrangeByScore(byte[] key, byte[] max, byte[] min) {
        return new JedisClusterCommand<Set<byte[]>>(connectionHandler, maxRedirections) {
            @Override
            public Set<byte[]> execute(Jedis connection) {
                return connection.zrevrangeByScore(key, min, max);
            }
        }.run(new String(key));
    }

    @Override
    public Set<byte[]> zrangeByScore(byte[] key, byte[] min, byte[] max, int offset, int count) {
        return new JedisClusterCommand<Set<byte[]>>(connectionHandler, maxRedirections) {
            @Override
            public Set<byte[]> execute(Jedis connection) {
                return connection.zrangeByScore(key, min, max, offset, count);
            }
        }.run(new String(key));
    }

    @Override
    public Set<byte[]> zrevrangeByScore(byte[] key, double max, double min, int offset, int count) {
        return new JedisClusterCommand<Set<byte[]>>(connectionHandler, maxRedirections) {
            @Override
            public Set<byte[]> execute(Jedis connection) {
                return connection.zrevrangeByScore(key, max, min, offset, count);
            }
        }.run(new String(key));
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(byte[] key, double min, double max) {
        return new JedisClusterCommand<Set<Tuple>>(connectionHandler, maxRedirections) {
            @Override
            public Set<Tuple> execute(Jedis connection) {
                return connection.zrangeByScoreWithScores(key, min, max);
            }
        }.run(new String(key));
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(byte[] key, double max, double min) {
        return new JedisClusterCommand<Set<Tuple>>(connectionHandler, maxRedirections) {
            @Override
            public Set<Tuple> execute(Jedis connection) {
                return connection.zrevrangeByScoreWithScores(key, max, min);
            }
        }.run(new String(key));
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(byte[] key, double min, double max, int offset, int count) {
        return new JedisClusterCommand<Set<Tuple>>(connectionHandler, maxRedirections) {
            @Override
            public Set<Tuple> execute(Jedis connection) {
                return connection.zrangeByScoreWithScores(key, min, max, offset, count);
            }
        }.run(new String(key));
    }

    @Override
    public Set<byte[]> zrevrangeByScore(byte[] key, byte[] max, byte[] min, int offset, int count) {
        return new JedisClusterCommand<Set<byte[]>>(connectionHandler, maxRedirections) {
            @Override
            public Set<byte[]> execute(Jedis connection) {
                return connection.zrevrangeByScore(key, max, min, offset, count);
            }
        }.run(new String(key));
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(byte[] key, byte[] min, byte[] max) {
        return new JedisClusterCommand<Set<Tuple>>(connectionHandler, maxRedirections) {
            @Override
            public Set<Tuple> execute(Jedis connection) {
                return connection.zrangeByScoreWithScores(key, min, max);
            }
        }.run(new String(key));
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(byte[] key, byte[] max, byte[] min) {
        return new JedisClusterCommand<Set<Tuple>>(connectionHandler, maxRedirections) {
            @Override
            public Set<Tuple> execute(Jedis connection) {
                return connection.zrevrangeByScoreWithScores(key, max, min);
            }
        }.run(new String(key));
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(byte[] key, byte[] min, byte[] max, int offset, int count) {
        return new JedisClusterCommand<Set<Tuple>>(connectionHandler, maxRedirections) {
            @Override
            public Set<Tuple> execute(Jedis connection) {
                return connection.zrangeByScoreWithScores(key, min, max, offset, count);
            }
        }.run(new String(key));
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(byte[] key, double max, double min, int offset, int count) {
        return new JedisClusterCommand<Set<Tuple>>(connectionHandler, maxRedirections) {
            @Override
            public Set<Tuple> execute(Jedis connection) {
                return connection.zrevrangeByScoreWithScores(key, max, min, offset, count);
            }
        }.run(new String(key));
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(byte[] key, byte[] max, byte[] min, int offset, int count) {
        return new JedisClusterCommand<Set<Tuple>>(connectionHandler, maxRedirections) {
            @Override
            public Set<Tuple> execute(Jedis connection) {
                return connection.zrevrangeByScoreWithScores(key, max, min, offset, count);
            }
        }.run(new String(key));
    }

    @Override
    public Long zremrangeByRank(byte[] key, long start, long end) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.zremrangeByRank(key, start, end);
            }
        }.run(new String(key));
    }

    @Override
    public Long zremrangeByScore(byte[] key, double start, double end) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.zremrangeByScore(key, start, end);
            }
        }.run(new String(key));
    }

    @Override
    public Long zremrangeByScore(byte[] key, byte[] start, byte[] end) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.zremrangeByScore(key, start, end);
            }
        }.run(new String(key));
    }

    @Override
    public Long zlexcount(byte[] key, byte[] min, byte[] max) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.zlexcount(key, min, max);
            }
        }.run(new String(key));
    }

    @Override
    public Set<byte[]> zrangeByLex(byte[] key, byte[] min, byte[] max) {
        return new JedisClusterCommand<Set<byte[]>>(connectionHandler, maxRedirections) {
            @Override
            public Set<byte[]> execute(Jedis connection) {
                return connection.zrangeByLex(key, min, max);
            }
        }.run(new String(key));
    }

    @Override
    public Set<byte[]> zrangeByLex(byte[] key, byte[] min, byte[] max, int offset, int count) {
        return new JedisClusterCommand<Set<byte[]>>(connectionHandler, maxRedirections) {
            @Override
            public Set<byte[]> execute(Jedis connection) {
                return connection.zrangeByLex(key, min, max, offset, count);
            }
        }.run(new String(key));
    }

    @Override
    public Long zremrangeByLex(byte[] key, byte[] min, byte[] max) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.zremrangeByLex(key, min, max);
            }
        }.run(new String(key));
    }

    @Override
    public Long linsert(byte[] key, BinaryClient.LIST_POSITION where, byte[] pivot, byte[] value) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.linsert(key, where, pivot, value);
            }
        }.run(new String(key));
    }

    @Override
    public Long lpushx(byte[] key, byte[]... arg) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.lpushx(key, arg);
            }
        }.run(new String(key));
    }

    @Override
    public Long rpushx(byte[] key, byte[]... arg) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.rpushx(key, arg);
            }
        }.run(new String(key));
    }

    @Override
    public List<byte[]> blpop(byte[] arg) {
        return new JedisClusterCommand<List<byte[]>>(connectionHandler, maxRedirections) {
            @Override
            public List<byte[]> execute(Jedis connection) {
                return connection.blpop(arg);
            }
        }.run(new String(new String(arg)));
    }

    @Override
    public List<byte[]> brpop(byte[] arg) {
        return new JedisClusterCommand<List<byte[]>>(connectionHandler, maxRedirections) {
            @Override
            public List<byte[]> execute(Jedis connection) {
                return connection.brpop(arg);
            }
        }.run(new String(new String(arg)));
    }

    @Override
    public Long del(byte[] key) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.del(key);
            }
        }.run(new String(key));
    }

    @Override
    public byte[] echo(byte[] arg) {
        return new JedisClusterCommand<byte[]>(connectionHandler, maxRedirections) {
            @Override
            public byte[] execute(Jedis connection) {
                return connection.echo(arg);
            }
        }.run(new String(arg));
    }

    @Override
    public Long move(byte[] key, int dbIndex) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.move(key, dbIndex);
            }
        }.run(new String(key));
    }

    @Override
    public Long bitcount(byte[] key) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.bitcount(key);
            }
        }.run(new String(key));
    }

    @Override
    public Long bitcount(byte[] key, long start, long end) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.bitcount(key, start, end);
            }
        }.run(new String(key));
    }

    @Override
    public Long pfadd(byte[] key, byte[]... elements) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.pfadd(key, elements);
            }
        }.run(new String(key));
    }

    @Override
    public long pfcount(byte[] key) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.pfcount(key);
            }
        }.run(new String(key));
    }

    @Override
    public void close() throws IOException {
        if (connectionHandler != null) {
            for (JedisPool pool : connectionHandler.getNodes().values()) {
                try {
                    if (pool != null) {
                        pool.destroy();
                    }
                } catch (Exception e) {
                    // pass
                }
            }
        }
    }

    @Override
    public void destroy() throws Exception {
        if (logger.isInfoEnabled()) {
            logger.info("Close BinaryRedisCluster !!!");
        }
        close();
    }

    @Override
    public String set(String key, String value) {
        return new JedisClusterCommand<String>(connectionHandler, maxRedirections) {
            @Override
            public String execute(Jedis connection) {
                return connection.set(key, value);
            }
        }.run(key);
    }

    @Override
    public String set(String key, String value, String nxxx, String expx, long time) {
        return new JedisClusterCommand<String>(connectionHandler, maxRedirections) {
            @Override
            public String execute(Jedis connection) {
                return connection.set(key, value, nxxx, expx, time);
            }
        }.run(key);
    }

    @Override
    public String get(String key) {
        return new JedisClusterCommand<String>(connectionHandler, maxRedirections) {
            @Override
            public String execute(Jedis connection) {
                return connection.get(key);
            }
        }.run(key);
    }

    @Override
    public Boolean exists(String key) {
        return new JedisClusterCommand<Boolean>(connectionHandler, maxRedirections) {
            @Override
            public Boolean execute(Jedis connection) {
                return connection.exists(key);
            }
        }.run(key);
    }

    @Override
    public Long persist(String key) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.persist(key);
            }
        }.run(key);
    }

    @Override
    public String type(String key) {
        return new JedisClusterCommand<String>(connectionHandler, maxRedirections) {
            @Override
            public String execute(Jedis connection) {
                return connection.type(key);
            }
        }.run(key);
    }

    @Override
    public Long expire(String key, int seconds) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.expire(key, seconds);
            }
        }.run(key);
    }

    @Override
    public Long expireAt(String key, long unixTime) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.expireAt(key, unixTime);
            }
        }.run(key);
    }

    @Override
    public Long ttl(String key) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.ttl(key);
            }
        }.run(key);
    }

    @Override
    public Boolean setbit(String key, long offset, boolean value) {
        return new JedisClusterCommand<Boolean>(connectionHandler, maxRedirections) {
            @Override
            public Boolean execute(Jedis connection) {
                return connection.setbit(key, offset, value);
            }
        }.run(key);
    }

    @Override
    public Boolean setbit(String key, long offset, String value) {
        return new JedisClusterCommand<Boolean>(connectionHandler, maxRedirections) {
            @Override
            public Boolean execute(Jedis connection) {
                return connection.setbit(key, offset, value);
            }
        }.run(key);
    }

    @Override
    public Boolean getbit(String key, long offset) {
        return new JedisClusterCommand<Boolean>(connectionHandler, maxRedirections) {
            @Override
            public Boolean execute(Jedis connection) {
                return connection.getbit(key, offset);
            }
        }.run(key);
    }

    @Override
    public Long setrange(String key, long offset, String value) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.setrange(key, offset, value);
            }
        }.run(key);
    }

    @Override
    public String getrange(String key, long startOffset, long endOffset) {
        return new JedisClusterCommand<String>(connectionHandler, maxRedirections) {
            @Override
            public String execute(Jedis connection) {
                return connection.getrange(key, startOffset, endOffset);
            }
        }.run(key);
    }

    @Override
    public String getSet(String key, String value) {
        return new JedisClusterCommand<String>(connectionHandler, maxRedirections) {
            @Override
            public String execute(Jedis connection) {
                return connection.getSet(key, value);
            }
        }.run(key);
    }

    @Override
    public Long setnx(String key, String value) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.setnx(key, value);
            }
        }.run(key);
    }

    @Override
    public String setex(String key, int seconds, String value) {
        return new JedisClusterCommand<String>(connectionHandler, maxRedirections) {
            @Override
            public String execute(Jedis connection) {
                return connection.setex(key, seconds, value);
            }
        }.run(key);
    }

    @Override
    public Long decrBy(String key, long integer) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.decrBy(key, integer);
            }
        }.run(key);
    }

    @Override
    public Long decr(String key) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.decr(key);
            }
        }.run(key);
    }

    @Override
    public Long incrBy(String key, long integer) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.incrBy(key, integer);
            }
        }.run(key);
    }

    @Override
    public Long incr(String key) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.incr(key);
            }
        }.run(key);
    }

    @Override
    public Long append(String key, String value) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.append(key, value);
            }
        }.run(key);
    }

    @Override
    public String substr(String key, int start, int end) {
        return new JedisClusterCommand<String>(connectionHandler, maxRedirections) {
            @Override
            public String execute(Jedis connection) {
                return connection.substr(key, start, end);
            }
        }.run(key);
    }

    @Override
    public Long hset(String key, String field, String value) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.hset(key, field, value);
            }
        }.run(key);
    }

    @Override
    public String hget(String key, String field) {
        return new JedisClusterCommand<String>(connectionHandler, maxRedirections) {
            @Override
            public String execute(Jedis connection) {
                return connection.hget(key, field);
            }
        }.run(key);
    }

    @Override
    public Long hsetnx(String key, String field, String value) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.hsetnx(key, field, value);
            }
        }.run(key);
    }

    @Override
    public String hmset(String key, Map<String, String> hash) {
        return new JedisClusterCommand<String>(connectionHandler, maxRedirections) {
            @Override
            public String execute(Jedis connection) {
                return connection.hmset(key, hash);
            }
        }.run(key);
    }

    @Override
    public List<String> hmget(String key, String... fields) {
        return new JedisClusterCommand<List<String>>(connectionHandler, maxRedirections) {
            @Override
            public List<String> execute(Jedis connection) {
                return connection.hmget(key, fields);
            }
        }.run(key);
    }

    @Override
    public Long hincrBy(String key, String field, long value) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.hincrBy(key, field, value);
            }
        }.run(key);
    }

    @Override
    public Boolean hexists(String key, String field) {
        return new JedisClusterCommand<Boolean>(connectionHandler, maxRedirections) {
            @Override
            public Boolean execute(Jedis connection) {
                return connection.hexists(key, field);
            }
        }.run(key);
    }

    @Override
    public Long hdel(String key, String... field) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.hdel(key, field);
            }
        }.run(key);
    }

    @Override
    public Long hlen(String key) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.hlen(key);
            }
        }.run(key);
    }

    @Override
    public Set<String> hkeys(String key) {
        return new JedisClusterCommand<Set<String>>(connectionHandler, maxRedirections) {
            @Override
            public Set<String> execute(Jedis connection) {
                return connection.hkeys(key);
            }
        }.run(key);
    }

    @Override
    public List<String> hvals(String key) {
        return new JedisClusterCommand<List<String>>(connectionHandler, maxRedirections) {
            @Override
            public List<String> execute(Jedis connection) {
                return connection.hvals(key);
            }
        }.run(key);
    }

    @Override
    public Map<String, String> hgetAll(String key) {
        return new JedisClusterCommand<Map<String, String>>(connectionHandler, maxRedirections) {
            @Override
            public Map<String, String> execute(Jedis connection) {
                return connection.hgetAll(key);
            }
        }.run(key);
    }

    @Override
    public Long rpush(String key, String... string) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.rpush(key, string);
            }
        }.run(key);
    }

    @Override
    public Long lpush(String key, String... string) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.lpush(key, string);
            }
        }.run(key);
    }

    @Override
    public Long llen(String key) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.llen(key);
            }
        }.run(key);
    }

    @Override
    public List<String> lrange(String key, long start, long end) {
        return new JedisClusterCommand<List<String>>(connectionHandler, maxRedirections) {
            @Override
            public List<String> execute(Jedis connection) {
                return connection.lrange(key, start, end);
            }
        }.run(key);
    }

    @Override
    public String ltrim(String key, long start, long end) {
        return new JedisClusterCommand<String>(connectionHandler, maxRedirections) {
            @Override
            public String execute(Jedis connection) {
                return connection.ltrim(key, start, end);
            }
        }.run(key);
    }

    @Override
    public String lindex(String key, long index) {
        return new JedisClusterCommand<String>(connectionHandler, maxRedirections) {
            @Override
            public String execute(Jedis connection) {
                return connection.lindex(key, index);
            }
        }.run(key);
    }

    @Override
    public String lset(String key, long index, String value) {
        return new JedisClusterCommand<String>(connectionHandler, maxRedirections) {
            @Override
            public String execute(Jedis connection) {
                return connection.lset(key, index, value);
            }
        }.run(key);
    }

    @Override
    public Long lrem(String key, long count, String value) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.lrem(key, count, value);
            }
        }.run(key);
    }

    @Override
    public String lpop(String key) {
        return new JedisClusterCommand<String>(connectionHandler, maxRedirections) {
            @Override
            public String execute(Jedis connection) {
                return connection.lpop(key);
            }
        }.run(key);
    }

    @Override
    public String rpop(String key) {
        return new JedisClusterCommand<String>(connectionHandler, maxRedirections) {
            @Override
            public String execute(Jedis connection) {
                return connection.rpop(key);
            }
        }.run(key);
    }

    @Override
    public Long sadd(String key, String... member) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.sadd(key, member);
            }
        }.run(key);
    }

    @Override
    public Set<String> smembers(String key) {
        return new JedisClusterCommand<Set<String>>(connectionHandler, maxRedirections) {
            @Override
            public Set<String> execute(Jedis connection) {
                return connection.smembers(key);
            }
        }.run(key);
    }

    @Override
    public Long srem(String key, String... member) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.srem(key, member);
            }
        }.run(key);
    }

    @Override
    public String spop(String key) {
        return new JedisClusterCommand<String>(connectionHandler, maxRedirections) {
            @Override
            public String execute(Jedis connection) {
                return connection.spop(key);
            }
        }.run(key);
    }

    @Override
    public Long scard(String key) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.scard(key);
            }
        }.run(key);
    }

    @Override
    public Boolean sismember(String key, String member) {
        return new JedisClusterCommand<Boolean>(connectionHandler, maxRedirections) {
            @Override
            public Boolean execute(Jedis connection) {
                return connection.sismember(key, member);
            }
        }.run(key);
    }

    @Override
    public String srandmember(String key) {
        return new JedisClusterCommand<String>(connectionHandler, maxRedirections) {
            @Override
            public String execute(Jedis connection) {
                return connection.srandmember(key);
            }
        }.run(key);
    }

    @Override
    public List<String> srandmember(String key, int count) {
        return new JedisClusterCommand<List<String>>(connectionHandler, maxRedirections) {
            @Override
            public List<String> execute(Jedis connection) {
                return connection.srandmember(key, count);
            }
        }.run(key);
    }

    @Override
    public Long strlen(String key) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.strlen(key);
            }
        }.run(key);
    }

    @Override
    public Long zadd(String key, double score, String member) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.zadd(key, score, member);
            }
        }.run(key);
    }

    @Override
    public Long zadd(String key, Map<String, Double> scoreMembers) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.zadd(key, scoreMembers);
            }
        }.run(key);
    }

    @Override
    public Set<String> zrange(String key, long start, long end) {
        return new JedisClusterCommand<Set<String>>(connectionHandler, maxRedirections) {
            @Override
            public Set<String> execute(Jedis connection) {
                return connection.zrange(key, start, end);
            }
        }.run(key);
    }

    @Override
    public Long zrem(String key, String... member) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.zrem(key, member);
            }
        }.run(key);
    }

    @Override
    public Double zincrby(String key, double score, String member) {
        return new JedisClusterCommand<Double>(connectionHandler, maxRedirections) {
            @Override
            public Double execute(Jedis connection) {
                return connection.zincrby(key, score, member);
            }
        }.run(key);
    }

    @Override
    public Long zrank(String key, String member) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.zrank(key, member);
            }
        }.run(key);
    }

    @Override
    public Long zrevrank(String key, String member) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.zrevrank(key, member);
            }
        }.run(key);
    }

    @Override
    public Set<String> zrevrange(String key, long start, long end) {
        return new JedisClusterCommand<Set<String>>(connectionHandler, maxRedirections) {
            @Override
            public Set<String> execute(Jedis connection) {
                return connection.zrevrange(key, start, end);
            }
        }.run(key);
    }

    @Override
    public Set<Tuple> zrangeWithScores(String key, long start, long end) {
        return new JedisClusterCommand<Set<Tuple>>(connectionHandler, maxRedirections) {
            @Override
            public Set<Tuple> execute(Jedis connection) {
                return connection.zrangeWithScores(key, start, end);
            }
        }.run(key);
    }

    @Override
    public Set<Tuple> zrevrangeWithScores(String key, long start, long end) {
        return new JedisClusterCommand<Set<Tuple>>(connectionHandler, maxRedirections) {
            @Override
            public Set<Tuple> execute(Jedis connection) {
                return connection.zrevrangeWithScores(key, start, end);
            }
        }.run(key);
    }

    @Override
    public Long zcard(String key) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.zcard(key);
            }
        }.run(key);
    }

    @Override
    public Double zscore(String key, String member) {
        return new JedisClusterCommand<Double>(connectionHandler, maxRedirections) {
            @Override
            public Double execute(Jedis connection) {
                return connection.zscore(key, member);
            }
        }.run(key);
    }

    @Override
    public List<String> sort(String key) {
        return new JedisClusterCommand<List<String>>(connectionHandler, maxRedirections) {
            @Override
            public List<String> execute(Jedis connection) {
                return connection.sort(key);
            }
        }.run(key);
    }

    @Override
    public List<String> sort(String key, SortingParams sortingParameters) {
        return new JedisClusterCommand<List<String>>(connectionHandler, maxRedirections) {
            @Override
            public List<String> execute(Jedis connection) {
                return connection.sort(key, sortingParameters);
            }
        }.run(key);
    }

    @Override
    public Long zcount(String key, double min, double max) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.zcount(key, min, max);
            }
        }.run(key);
    }

    @Override
    public Long zcount(String key, String min, String max) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.zcount(key, min, max);
            }
        }.run(key);
    }

    @Override
    public Set<String> zrangeByScore(String key, double min, double max) {
        return new JedisClusterCommand<Set<String>>(connectionHandler, maxRedirections) {
            @Override
            public Set<String> execute(Jedis connection) {
                return connection.zrangeByScore(key, min, max);
            }
        }.run(key);
    }

    @Override
    public Set<String> zrangeByScore(String key, String min, String max) {
        return new JedisClusterCommand<Set<String>>(connectionHandler, maxRedirections) {
            @Override
            public Set<String> execute(Jedis connection) {
                return connection.zrangeByScore(key, min, max);
            }
        }.run(key);
    }

    @Override
    public Set<String> zrevrangeByScore(String key, double max, double min) {
        return new JedisClusterCommand<Set<String>>(connectionHandler, maxRedirections) {
            @Override
            public Set<String> execute(Jedis connection) {
                return connection.zrevrangeByScore(key, max, min);
            }
        }.run(key);
    }

    @Override
    public Set<String> zrangeByScore(String key, double min, double max, int offset, int count) {
        return new JedisClusterCommand<Set<String>>(connectionHandler, maxRedirections) {
            @Override
            public Set<String> execute(Jedis connection) {
                return connection.zrangeByScore(key, min, max, offset, count);
            }
        }.run(key);
    }

    @Override
    public Set<String> zrevrangeByScore(String key, String max, String min) {
        return new JedisClusterCommand<Set<String>>(connectionHandler, maxRedirections) {
            @Override
            public Set<String> execute(Jedis connection) {
                return connection.zrevrangeByScore(key, max, min);
            }
        }.run(key);
    }

    @Override
    public Set<String> zrangeByScore(String key, String min, String max, int offset, int count) {
        return new JedisClusterCommand<Set<String>>(connectionHandler, maxRedirections) {
            @Override
            public Set<String> execute(Jedis connection) {
                return connection.zrangeByScore(key, min, max, offset, count);
            }
        }.run(key);
    }

    @Override
    public Set<String> zrevrangeByScore(String key, double max, double min, int offset, int count) {
        return new JedisClusterCommand<Set<String>>(connectionHandler, maxRedirections) {
            @Override
            public Set<String> execute(Jedis connection) {
                return connection.zrevrangeByScore(key, max, min, offset, count);
            }
        }.run(key);
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max) {
        return new JedisClusterCommand<Set<Tuple>>(connectionHandler, maxRedirections) {
            @Override
            public Set<Tuple> execute(Jedis connection) {
                return connection.zrangeByScoreWithScores(key, min, max);
            }
        }.run(key);
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(String key, double max, double min) {
        return new JedisClusterCommand<Set<Tuple>>(connectionHandler, maxRedirections) {
            @Override
            public Set<Tuple> execute(Jedis connection) {
                return connection.zrevrangeByScoreWithScores(key, max, min);
            }
        }.run(key);
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max, int offset, int count) {
        return new JedisClusterCommand<Set<Tuple>>(connectionHandler, maxRedirections) {
            @Override
            public Set<Tuple> execute(Jedis connection) {
                return connection.zrangeByScoreWithScores(key, min, max, offset, count);
            }
        }.run(key);
    }

    @Override
    public Set<String> zrevrangeByScore(String key, String max, String min, int offset, int count) {
        return new JedisClusterCommand<Set<String>>(connectionHandler, maxRedirections) {
            @Override
            public Set<String> execute(Jedis connection) {
                return connection.zrevrangeByScore(key, max, min, offset, count);
            }
        }.run(key);
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(String key, String min, String max) {
        return new JedisClusterCommand<Set<Tuple>>(connectionHandler, maxRedirections) {
            @Override
            public Set<Tuple> execute(Jedis connection) {
                return connection.zrangeByScoreWithScores(key, min, max);
            }
        }.run(key);
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(String key, String max, String min) {
        return new JedisClusterCommand<Set<Tuple>>(connectionHandler, maxRedirections) {
            @Override
            public Set<Tuple> execute(Jedis connection) {
                return connection.zrevrangeByScoreWithScores(key, max, min);
            }
        }.run(key);
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(String key, String min, String max, int offset, int count) {
        return new JedisClusterCommand<Set<Tuple>>(connectionHandler, maxRedirections) {
            @Override
            public Set<Tuple> execute(Jedis connection) {
                return connection.zrangeByScoreWithScores(key, min, max, offset, count);
            }
        }.run(key);
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(String key, double max, double min, int offset, int count) {
        return new JedisClusterCommand<Set<Tuple>>(connectionHandler, maxRedirections) {
            @Override
            public Set<Tuple> execute(Jedis connection) {
                return connection.zrevrangeByScoreWithScores(key, max, min, offset, count);
            }
        }.run(key);
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(String key, String max, String min, int offset, int count) {
        return new JedisClusterCommand<Set<Tuple>>(connectionHandler, maxRedirections) {
            @Override
            public Set<Tuple> execute(Jedis connection) {
                return connection.zrevrangeByScoreWithScores(key, max, min, offset, count);
            }
        }.run(key);
    }

    @Override
    public Long zremrangeByRank(String key, long start, long end) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.zremrangeByRank(key, start, end);
            }
        }.run(key);
    }

    @Override
    public Long zremrangeByScore(String key, double start, double end) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.zremrangeByScore(key, start, end);
            }
        }.run(key);
    }

    @Override
    public Long zremrangeByScore(String key, String start, String end) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.zremrangeByScore(key, start, end);
            }
        }.run(key);
    }

    @Override
    public Long zlexcount(String key, String min, String max) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.zlexcount(key, min, max);
            }
        }.run(key);
    }

    @Override
    public Set<String> zrangeByLex(String key, String min, String max) {
        return new JedisClusterCommand<Set<String>>(connectionHandler, maxRedirections) {
            @Override
            public Set<String> execute(Jedis connection) {
                return connection.zrangeByLex(key, min, max);
            }
        }.run(key);
    }

    @Override
    public Set<String> zrangeByLex(String key, String min, String max, int offset, int count) {
        return new JedisClusterCommand<Set<String>>(connectionHandler, maxRedirections) {
            @Override
            public Set<String> execute(Jedis connection) {
                return connection.zrangeByLex(key, min, max, offset, count);
            }
        }.run(key);
    }

    @Override
    public Long zremrangeByLex(String key, String min, String max) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.zremrangeByLex(key, min, max);
            }
        }.run(key);
    }

    @Override
    public Long linsert(String key, BinaryClient.LIST_POSITION where, String pivot, String value) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.linsert(key, where, pivot, value);
            }
        }.run(key);
    }

    @Override
    public Long lpushx(String key, String... string) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.lpushx(key, string);
            }
        }.run(key);
    }

    @Override
    public Long rpushx(String key, String... string) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.rpushx(key, string);
            }
        }.run(key);
    }

    @Override
    public List<String> blpop(String arg) {
        return new JedisClusterCommand<List<String>>(connectionHandler, maxRedirections) {
            @Override
            public List<String> execute(Jedis connection) {
                return connection.blpop(arg);
            }
        }.run(arg);
    }


    @Override
    public List<String> brpop(String arg) {
        return new JedisClusterCommand<List<String>>(connectionHandler, maxRedirections) {
            @Override
            public List<String> execute(Jedis connection) {
                return connection.brpop(arg);
            }
        }.run(arg);
    }


    @Override
    public Long del(String key) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.del(key);
            }
        }.run(key);
    }

    @Override
    public String echo(String string) {
        return new JedisClusterCommand<String>(connectionHandler, maxRedirections) {
            @Override
            public String execute(Jedis connection) {
                return connection.echo(string);
            }
        }.run(null);
    }

    @Override
    public Long move(String key, int dbIndex) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.move(key, dbIndex);
            }
        }.run(key);
    }

    @Override
    public Long bitcount(String key) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.bitcount(key);
            }
        }.run(key);
    }

    @Override
    public Long bitcount(String key, long start, long end) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.bitcount(key, start, end);
            }
        }.run(key);
    }

    @Override
    public ScanResult<Map.Entry<String, String>> hscan(String key, int cursor) {
        return new JedisClusterCommand<ScanResult<Map.Entry<String, String>>>(connectionHandler, maxRedirections) {
            @Override
            public ScanResult<Map.Entry<String, String>> execute(Jedis connection) {
                return connection.hscan(key, cursor);
            }
        }.run(key);
    }

    @Override
    public ScanResult<String> sscan(String key, int cursor) {
        return new JedisClusterCommand<ScanResult<String>>(connectionHandler, maxRedirections) {
            @Override
            public ScanResult<String> execute(Jedis connection) {
                return connection.sscan(key, cursor);
            }
        }.run(key);
    }

    @Override
    public ScanResult<Tuple> zscan(String key, int cursor) {
        return new JedisClusterCommand<ScanResult<Tuple>>(connectionHandler, maxRedirections) {
            @Override
            public ScanResult<Tuple> execute(Jedis connection) {
                return connection.zscan(key, cursor);
            }
        }.run(key);
    }

    @Override
    public ScanResult<Map.Entry<String, String>> hscan(String key, String cursor) {
        return new JedisClusterCommand<ScanResult<Map.Entry<String, String>>>(connectionHandler, maxRedirections) {
            @Override
            public ScanResult<Map.Entry<String, String>> execute(Jedis connection) {
                return connection.hscan(key, cursor);
            }
        }.run(key);
    }

    @Override
    public ScanResult<String> sscan(String key, String cursor) {
        return new JedisClusterCommand<ScanResult<String>>(connectionHandler, maxRedirections) {
            @Override
            public ScanResult<String> execute(Jedis connection) {
                return connection.sscan(key, cursor);
            }
        }.run(key);
    }

    @Override
    public ScanResult<Tuple> zscan(String key, String cursor) {
        return new JedisClusterCommand<ScanResult<Tuple>>(connectionHandler, maxRedirections) {
            @Override
            public ScanResult<Tuple> execute(Jedis connection) {
                return connection.zscan(key, cursor);
            }
        }.run(key);
    }

    @Override
    public Long pfadd(String key, String... elements) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.pfadd(key, elements);
            }
        }.run(key);
    }

    @Override
    public long pfcount(String key) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.pfcount(key);
            }
        }.run(key);
    }


    @Override
    public String set(byte[] key, byte[] value, byte[] nxxx, byte[] expx, long time) {
        return new JedisClusterCommand<String>(connectionHandler, maxRedirections) {
            @Override
            public String execute(Jedis connection) {
                return connection.set(key, value, nxxx, expx, time);
            }
        }.run(new String(key));
    }

    @Override
    public Long pexpire(byte[] key, long milliseconds) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.pexpire(key, milliseconds);
            }
        }.run(new String(key));
    }

    @Override
    public Long pexpireAt(byte[] key, long millisecondsTimestamp) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.pexpireAt(key, millisecondsTimestamp);
            }
        }.run(new String(key));
    }

    @Override
    public Set<byte[]> spop(byte[] key, long count) {
        return new JedisClusterCommand<Set<byte[]>>(connectionHandler, maxRedirections) {
            @Override
            public Set<byte[]> execute(Jedis connection) {
                return connection.spop(key, count);
            }
        }.run(new String(key));
    }

    @Override
    public Set<byte[]> zrevrangeByLex(byte[] key, byte[] max, byte[] min) {
        return new JedisClusterCommand<Set<byte[]>>(connectionHandler, maxRedirections) {
            @Override
            public Set<byte[]> execute(Jedis connection) {
                return connection.zrevrangeByLex(key, max, min);
            }
        }.run(new String(key));
    }

    @Override
    public Set<byte[]> zrevrangeByLex(byte[] key, byte[] max, byte[] min, int offset, int count) {
        return new JedisClusterCommand<Set<byte[]>>(connectionHandler, maxRedirections) {
            @Override
            public Set<byte[]> execute(Jedis connection) {
                return connection.zrevrangeByLex(key, max, min, offset , count);
            }
        }.run(new String(key));
    }

    @Override
    public Long pexpire(String key, long milliseconds) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.pexpire(key, milliseconds);
            }
        }.run(key);
    }

    @Override
    public Long pexpireAt(String key, long millisecondsTimestamp) {
        return new JedisClusterCommand<Long>(connectionHandler, maxRedirections) {
            @Override
            public Long execute(Jedis connection) {
                return connection.pexpireAt(key, millisecondsTimestamp);
            }
        }.run(key);
    }

    @Override
    public Double incrByFloat(String key, double value) {
        return new JedisClusterCommand<Double>(connectionHandler, maxRedirections) {
            @Override
            public Double execute(Jedis connection) {
                return connection.incrByFloat(key, value);
            }
        }.run( key);
    }

    @Override
    public Set<String> spop(String key, long count) {
        return new JedisClusterCommand<Set<String>>(connectionHandler, maxRedirections) {
            @Override
            public Set<String> execute(Jedis connection) {
                return connection.spop(key, count);
            }
        }.run(key);
    }

    @Override
    public Set<String> zrevrangeByLex(String key, String max, String min) {
        return new JedisClusterCommand<Set<String>>(connectionHandler, maxRedirections) {
            @Override
            public Set<String> execute(Jedis connection) {
                return connection.zrevrangeByLex(key, max,min);
            }
        }.run(key);
    }

    @Override
    public Set<String> zrevrangeByLex(String key, String max, String min, int offset, int count) {
        return new JedisClusterCommand<Set<String>>(connectionHandler, maxRedirections) {
            @Override
            public Set<String> execute(Jedis connection) {
                return connection.zrevrangeByLex(key, max,min, offset , count);
            }
        }.run(key);
    }

    @Override
    public List<String> blpop(int timeout, String key) {
        return new JedisClusterCommand<List<String>>(connectionHandler, maxRedirections) {
            @Override
            public List<String> execute(Jedis connection) {
                return connection.blpop( timeout , key );
            }
        }.run(key);
    }

    @Override
    public List<String> brpop(int timeout, String key) {
        return new JedisClusterCommand<List<String>>(connectionHandler, maxRedirections) {
            @Override
            public List<String> execute(Jedis connection) {
                return connection.brpop( timeout , key );
            }
        }.run(key);
    }
}
