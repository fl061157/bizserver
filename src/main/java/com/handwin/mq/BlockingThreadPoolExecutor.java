package com.handwin.mq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author fangliang
 */
public class BlockingThreadPoolExecutor extends ThreadPoolExecutor {

    private static final Logger logger = LoggerFactory.getLogger(BlockingThreadPoolExecutor.class);

    public BlockingThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, int queueSize) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(queueSize), new NamedThreadFactory(), new BlockPolicy());
    }


    public static class BlockPolicy implements RejectedExecutionHandler {

        public BlockPolicy() {
        }

        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            try {
                r.run();
            } catch (Throwable t) {
                logger.error("Work discarded, thread was interrupted while waiting for space to schedule: {}", t);
            }
        }
    }


    public static class GoRuningPolicy implements RejectedExecutionHandler {

        public GoRuningPolicy() {
        }

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            try {
                if (logger.isInfoEnabled()) {
                    logger.info("Executor queue full, begin execute rejected !");
                }
                r.run();
            } catch (Throwable throwable) {
                logger.error("Work rejected , thread was interrupted while waiting for space to schedule: {}", throwable);
            }
        }
    }


    static class NamedThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        NamedThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            namePrefix = "BIZ" +
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
