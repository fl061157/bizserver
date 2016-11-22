package com.handwin.safeguard;

import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by piguangtao on 15/3/13.
 */
public interface IExceptionHandler {
    public static final LinkedBlockingQueue<ILoggingEvent> eventQueue = new LinkedBlockingQueue<>(100);
    public static final ConcurrentHashMap<String,CopyOnWriteArrayList<? extends IExceptionHandler>> handlerMap = new ConcurrentHashMap<>();
    public void handle();
}
