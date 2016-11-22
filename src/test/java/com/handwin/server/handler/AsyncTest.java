package com.handwin.server.handler;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

/**
 * Created by piguangtao on 2014/11/28.
 */
@Configurable
@EnableAsync
@Component
//@ComponentScan("com.handwin.server.handler")
public class AsyncTest implements InitializingBean{

    @Autowired
    private TaskExecutor executorInterBizServers;


    @Override
    public void afterPropertiesSet() throws Exception {
        new Thread(()->{
            System.out.println("before async. thread:"+Thread.currentThread().getId());
            executorInterBizServers.execute(()->asyncTest());
            System.out.println("after async thread:"+Thread.currentThread().getId());
        }).start();
    }


    @Async
    public void asyncTest(){
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("enter async test. Thread:"+Thread.currentThread().getId());
    }
}
