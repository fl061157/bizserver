package com.handwin.metric;

import com.codahale.metrics.annotation.Timed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by piguangtao on 14/12/26.
 */
public class RedisTemplateMetric<K, V> extends RedisTemplate<K, V> {
    @Autowired
    private OperationErrorMetric errorMetric;

    @Override
    @Timed(name = "redis-execute-redis-call-back-timer", absolute = true)
    public <T> T execute(RedisCallback<T> action) {
        try{
            return super.execute(action);
        }
        catch (Throwable e){
            errorMetric.getRedisErrors().inc();
            throw e;
        }
    }

    @Override
    @Timed(name = "redis-execute-session-call-back-timer", absolute = true)
    public <T> T execute(SessionCallback<T> session) {
        try{
            return super.execute(session);
        }
        catch (Throwable e){
            errorMetric.getRedisErrors().inc();
            throw e;
        }
    }

    @Override
    @Timed(name = "redis-execute-pipelined-session-call-back-timer", absolute = true)
    public List<Object> executePipelined(final SessionCallback<?> session) {
        try{
            return super.executePipelined(session);
        }
        catch (Throwable e){
            errorMetric.getRedisErrors().inc();
            throw e;
        }
    }

    @Override
    @Timed(name = "redis-execute-pipelined-redis-call-back-timer", absolute = true)
    public List<Object> executePipelined(final RedisCallback<?> action) {
        try {
            return super.executePipelined(action);
        }catch (Throwable e){
            errorMetric.getRedisErrors().inc();
            throw e;
        }
    }

    @Override
    @Timed(name = "redis-delete-timer", absolute = true)
    public void delete(K key) {
        try{
            super.delete(key);
        }catch (Throwable e){
            errorMetric.getRedisErrors().inc();
            throw e;
        }
    }

    @Override
    @Timed(name = "redis-expire-timer", absolute = true)
    public Boolean expire(K key, final long timeout, final TimeUnit unit) {
        try{
            return super.expire(key, timeout, unit);
        }
        catch (Throwable e){
            errorMetric.getRedisErrors().inc();
            throw e;
        }
    }
}
