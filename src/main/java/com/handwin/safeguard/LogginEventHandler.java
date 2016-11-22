package com.handwin.safeguard;

import ch.qos.logback.classic.spi.ILoggingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by piguangtao on 15/3/13.
 */
@Service
public class LogginEventHandler implements InitializingBean {
    private static Logger logger = LoggerFactory.getLogger(LogginEventHandler.class);

    private Executor executor = null;

    @Override
    public void afterPropertiesSet() throws Exception {
        init();
    }

    private void init() {
        executor = new ThreadPoolExecutor(1, 5, 100, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(100), new LoggingEventThreadFactory());

        //TODO 采用线程池进行处理，至少一个线程，最多2个线程
        new Thread("handling-event-log-thread") {
            public void run() {
                while (true) {
                    try {
                        ILoggingEvent loggingEvent = IExceptionHandler.eventQueue.take();
                        executor.execute(() -> handleLoggingEvent(loggingEvent));
                    } catch (Throwable e) {
                        logger.warn("fails to handle logging event.", e);
                    }
                }
            }
        }.start();
    }

    private void handleLoggingEvent(ILoggingEvent loggingEvent) {
        if (null != loggingEvent.getThrowableProxy()) {
            String className = loggingEvent.getThrowableProxy().getClassName();
            if (null != className && !"".equals(className)) {
                List<? extends IExceptionHandler> hanlderList = IExceptionHandler.handlerMap.get(className);
                if(null != hanlderList){
                    hanlderList.stream().forEach((handler)->handler.handle());
                }
            }
        }
    }

    static class LoggingEventThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        LoggingEventThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            namePrefix = "pool-logging-event-handle-" +
                    poolNumber.getAndIncrement() +
                    "-thread-";
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }
}
