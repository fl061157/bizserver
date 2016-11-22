package com.handwin.server;

import com.handwin.server.handler.BroadcastRefresh;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.GenericXmlApplicationContext;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


public class Startup {
    private static final Logger logger = LoggerFactory.getLogger(Startup.class);;

    private final static String prefix = "classpath:";
    private final static String loading = "config-loading.xml";
    private final static String dir = "xmls";

    private final static AtomicBoolean isShutdown = new AtomicBoolean(false);

    public static void main(String[] args) {

//        String profile = args.length > 0 ? args[0] : "dev";
        String profile = args.length > 0 ? args[0] : "performance";
//        String profile = args.length > 0 ? args[0] : "test-mock-cn";
        logger.info("spring.profiles.active:{}", profile);
        System.setProperty("spring.profiles.active", profile);
        final GenericXmlApplicationContext context = new GenericXmlApplicationContext();

        context.load(prefix + dir + File.separatorChar + loading);
        context.refresh();

        logger.info("biz server initialize success");

        //TODO 后面需要考虑把此处移至运维模块
        BroadcastRefresh broadcastRefresh = context.getBean(BroadcastRefresh.class);
        Executors.newSingleThreadScheduledExecutor().schedule(broadcastRefresh, 0, TimeUnit.SECONDS);

        Runtime.getRuntime().addShutdownHook(new Thread("handle-before-stop") {
            public void run() {
                logger.info("waiting for server shutdown");
                PrepareBeforeStop prepareBeforeStop = (PrepareBeforeStop) context.getBean("prepareBeforeStop");
                prepareBeforeStop.clean();
                context.close();
                logger.info("biz server shutdown success.");
            }
        });

    }

    public static AtomicBoolean isShutdown() {
        return isShutdown;
    }
}
