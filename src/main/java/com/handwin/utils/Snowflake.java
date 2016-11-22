package com.handwin.utils;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Created by piguangtao on 2014/5/20.
 */
@Component
public class Snowflake implements InitializingBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(Snowflake.class);
    public static final int NODE_SHIFT = 10;
    public static final int SEQ_SHIFT = 12;

    public static final short MAX_NODE = 1024;
    public static final short MAX_SEQUENCE = 4096;

    private short sequence;
    private long referenceTime;

    private int node;

    public void afterPropertiesSet() throws Exception {
        initNode();
    }

    private void initNode(){
        String nodeIdProperty = System.getProperty("node.id");
        if(StringUtils.isBlank(nodeIdProperty)){
            LOGGER.warn("node.id not set. use default value:{}",SystemConstant.NODE_ID_DEFAULT);
            node = SystemConstant.NODE_ID_DEFAULT;
        }
        else {
            try{
                node = Integer.valueOf(nodeIdProperty.trim());
            }
            catch (Exception e){
                LOGGER.warn("node.id {} is not correct.. use default value:{}",nodeIdProperty,SystemConstant.NODE_ID_DEFAULT);
                node = SystemConstant.NODE_ID_DEFAULT;
            }
        }

        if (node < 0 || node > MAX_NODE) {
            throw new IllegalArgumentException(String.format("node must be between %s and %s", 0, MAX_NODE));
        }
    }

    /**
     * Generates a k-ordered unique 64-bit integer. Subsequent invocations of this method will produce
     * increasing integer values.
     *
     * @return The next 64-bit integer.
     * 1毫秒内至多支持4096个消息
     */
    public long next() {
        long result = 0;
        boolean isSuccess = false;
        while (!isSuccess) {
            try {
                result = nextId();
                isSuccess = true;
            } catch (Exception e) {
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e1) {
                }
            }
        }

        if( LOGGER.isDebugEnabled() ) {
            LOGGER.debug( "MESSAGEID:{}" , result );
        }

        return result;
    }

    private long nextId() {
        long currentTime = System.currentTimeMillis();
        long counter;
        synchronized (this) {
            if (currentTime < referenceTime) {
                throw new RuntimeException(String.format("Last referenceTime %s is after reference time %s", referenceTime, currentTime));
            } else if (currentTime > referenceTime) {
                this.sequence = 0;
            } else {
                if (this.sequence < Snowflake.MAX_SEQUENCE) {
                    this.sequence++;
                } else {
                    throw new RuntimeException("Sequence exhausted at " + this.sequence);
                }
            }
            counter = this.sequence;
            referenceTime = currentTime;
        }

        return currentTime << NODE_SHIFT << SEQ_SHIFT | node << SEQ_SHIFT | counter;
    }

}
