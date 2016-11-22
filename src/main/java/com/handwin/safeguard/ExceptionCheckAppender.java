package com.handwin.safeguard;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;

import java.util.concurrent.TimeUnit;

/**
 * Created by piguangtao on 15/3/13.
 */
public class ExceptionCheckAppender<E> extends UnsynchronizedAppenderBase<E> {

    @Override
    protected void append(E eventObject) {
        if (!(eventObject instanceof ILoggingEvent)) return;
        ILoggingEvent event = (ILoggingEvent) eventObject;
        try {
            IExceptionHandler.eventQueue.offer(event, 1, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {

        }
    }
}
