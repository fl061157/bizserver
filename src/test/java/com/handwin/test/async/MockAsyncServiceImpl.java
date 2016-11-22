package com.handwin.test.async;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Created by Danny on 2014-11-29.
 */
@Component
public class MockAsyncServiceImpl implements MockAsyncService {

    @Async
    public void asyncRun(String msg) {
        System.out.println(msg + ":" + Thread.currentThread().getId());
    }
}
