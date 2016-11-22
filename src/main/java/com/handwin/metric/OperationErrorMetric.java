package com.handwin.metric;

import com.codahale.metrics.Counter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

/**
 * Created by piguangtao on 14/12/26.
 */
@Service
@ManagedResource(objectName = "operatioin.error:name=operationErrorMetric",description = "operation error statistic")
public class OperationErrorMetric extends DefaultMetric implements InitializingBean{

    private Counter udpStrategyErrors;
//
    private Counter mysqlErrors;

    private Counter redisErrors;

//    private long udpStrategyErrorsLong;
//
//    private long mysqlErrorsLong;
//
//    private long redisErrorsLong;

    /**
     * Invoked by a BeanFactory after it has set all bean properties supplied
     * (and satisfied BeanFactoryAware and ApplicationContextAware).
     * <p>This method allows the bean instance to perform initialization only
     * possible when all bean properties have been set and to throw an
     * exception in the event of misconfiguration.
     *
     * @throws Exception in the event of misconfiguration (such
     *                   as failure to set an essential property) or if initialization fails.
     */
    @Override
    public void afterPropertiesSet() {
        udpStrategyErrors = getMetricRegistry().counter("udp-strategy-errors-counter");
        mysqlErrors = getMetricRegistry().counter("mysql-errors-counter");
        redisErrors = getMetricRegistry().counter("redis-errors-counter");
    }

    public Counter getUdpStrategyErrors() {
        return udpStrategyErrors;
    }

    public Counter getMysqlErrors() {
        return mysqlErrors;
    }

    public Counter getRedisErrors() {
        return redisErrors;
    }

    @ManagedAttribute(description = "errors to access udp strategy")
    public long getUdpStrategyErrorsLong() {

        long errorCount = udpStrategyErrors.getCount();

        if(errorCount > 0){
            resetUdpStrategyErrors();
        }
        return errorCount;
    }

    @ManagedAttribute(description = "errors to access mysql")
    public long getMysqlErrorsLong() {
        long errorCount = mysqlErrors.getCount();
        if(errorCount > 0){
            resetMysqlErrors();
        }
        return errorCount;
    }

    @ManagedAttribute(description = "errors to redis")
    public long getRedisErrorsLong() {
        long errorCount = redisErrors.getCount();
        if(errorCount > 0){
            resetRedisErrors();
        }
        return errorCount;
    }

//    @ManagedAttribute()
//    public void setUdpStrategyErrorsLong(long udpStrategyErrorsLong) {
//        this.udpStrategyErrorsLong = udpStrategyErrorsLong;
//    }
//
//    @ManagedAttribute()
//    public void setMysqlErrorsLong(long mysqlErrorsLong) {
//        this.mysqlErrorsLong = mysqlErrorsLong;
//    }
//
//    @ManagedAttribute()
//    public void setRedisErrorsLong(long redisErrorsLong) {
//        this.redisErrorsLong = redisErrorsLong;
//    }

    @ManagedOperation(description = "clean udp strategy error number")
    public void resetUdpStrategyErrors(){
//        udpStrategyErrorsLong = 0;
        udpStrategyErrors.dec(udpStrategyErrors.getCount());
    }

    @ManagedOperation(description = "clean mysql strategy error number")
    public void resetMysqlErrors(){
//        mysqlErrorsLong = 0;
        mysqlErrors.dec(mysqlErrors.getCount());
    }

    @ManagedOperation(description = "clean redis error number")
    public void resetRedisErrors(){
//        redisErrorsLong = 0;
        redisErrors.dec(redisErrors.getCount());
    }

}
