package com.handwin.service;

import io.netty.util.HashedWheelTimer;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Created by piguangtao on 14-3-4.
 * 延迟任务的处理
 */
@Service
public class DelayTaskService {

    private static final HashedWheelTimer timer = new HashedWheelTimer(100, TimeUnit.MILLISECONDS);

    public static final int MAX_TRY_TIMES = 1;

    public HashedWheelTimer getTimer() {
        return timer;
    }

    public void submitDelayTask(long delayMs, final Runnable task) {
        getTimer().newTimeout(timeout -> task.run(), delayMs, TimeUnit.MILLISECONDS);
    }
}
