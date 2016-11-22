package com.handwin.safeguard.database;

import com.datastax.driver.core.exceptions.NoHostAvailableException;
import com.handwin.safeguard.IExceptionHandler;
import com.handwin.server.PrepareBeforeStop;
import com.handwin.service.MessageService;
import com.handwin.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by piguangtao on 15/3/14.
 */
@Service
public class CassandraExceptionHandler implements IExceptionHandler, InitializingBean {
    private static Logger logger = LoggerFactory.getLogger(CassandraExceptionHandler.class);

    private static final String checkUserId = "88888888888888888888888888888888";

    private static final Integer appId = 0;

    @Autowired
    protected UserService userService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private PrepareBeforeStop resouceManager;

    private static AtomicBoolean isMonitoringCassandra = new AtomicBoolean(false);

    private static Executor executor = Executors.newSingleThreadExecutor();

    @Override
    public void afterPropertiesSet() throws Exception {
        handlerMap.putIfAbsent(NoHostAvailableException.class.getName(), new CopyOnWriteArrayList<>());
        List handlers = handlerMap.get(NoHostAvailableException.class.getName());
        handlers.add(this);
    }

    @Override
    public void handle() {
        int checkout = 0;
        boolean isOk = false;

        //TODO 检查cassandra 数据库连接是否正常
        //TODO 访问数据库测试 如果正常 则退出，异常 在 每隔1，2秒，检测三次，每次都失败，则视为数据库连接异常
        while ((!isOk) && checkout < 3) {
            isOk = isCassandraOk();
            checkout++;
            try {
                Thread.sleep(checkout * 1000);
            } catch (InterruptedException e) {

            }
        }
        if (isOk) {
            return;
        } else {
            resouceManager.closeQueueConsumer();
        }

        //TODO 数据库连接异常，则启动数据正常检测流程，数据库恢复正常，则进程处理
        if (isMonitoringCassandra.compareAndSet(false, true)) {
            monitorCassandra();
        } else {
            logger.info("monitoring cassandra now.");
        }

    }


    private void monitorCassandra() {
        executor.execute(() -> {
            int[] sleepTimeInSec = new int[]{1, 2, 4};
            int tryCount = 0;
            while (true) {
                if (isCassandraOk()) {
                    resouceManager.openQueueConsumer();
                } else {
                    tryCount++;
                    try {
                        Thread.sleep(sleepTimeInSec[tryCount % sleepTimeInSec.length]);
                    } catch (InterruptedException e) {
                    }
                }
            }
        });
    }


    private boolean isCassandraOk() {
        return isCassandraOkByUserService() && isCassandraOkByMessageService();
    }

    private boolean isCassandraOkByUserService() {
        boolean isOk = true;
        try {
            userService.getTokenInfo(checkUserId, appId);
        } catch (Exception e) {
            //TODO 需要具体化，只有具体的数据库连接异常 才能断言数据库连接问题
            isOk = false;
        }
        return isOk;
    }

    private boolean isCassandraOkByMessageService() {
        boolean isOk = true;
        try {
            messageService.updateUnreadLocalCount(checkUserId, false);
        } catch (Exception e) {
            //TODO 需要具体化，只有具体的数据库连接异常 才能断言数据库连接问题
            isOk = false;
        }
        return isOk;
    }


}
