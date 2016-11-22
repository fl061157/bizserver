package com.handwin.mr;

import com.handwin.mq.MessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by fangliang on 3/3/16.
 */
public class MessageListenerAdapter implements cn.v5.rpc.cluster.MessageListener {


    private MessageListener messageListener;

    protected static final Logger logger = LoggerFactory.getLogger(MessageListenerAdapter.class);

    public MessageListenerAdapter(MessageListener messageListener) {
        this.messageListener = messageListener;
    }

    @Override
    public void onMessage(byte[] bytes) throws Throwable {
        try {
            messageListener.onMessage(bytes);
        } catch (Throwable e) {
            logger.error("", e);
        }
    }


}
