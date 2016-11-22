package com.handwin.admin.http;

import com.handwin.admin.http.controller.*;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by fangliang on 21/1/15.
 */
@Service
public class HttpCheckerServer implements InitializingBean, DisposableBean {

    private Tomcat tomcat;

    @Autowired
    protected RedisCheckerServlet redisCheckerServlet;

    @Autowired
    private RabbitmqCheckerServlet rabbitmqCheckerServlet;

    @Autowired
    private MysqlCheckerServlet mysqlCheckerServlet;

    @Autowired
    private CassendraCheckerServlet cassendraCheckerServlet;

    @Autowired
    private LiveStopServerlet liveStopServerlet;

    private static final int DEFAULT_HTTP_PORT = 7777;

    private static final String CONTEXT_FILE_PATH = System.getProperty("java.io.tmpdir");

    @Value("#{configproperties['http.listen.port']}")
    private int httpListenPort;

    private static Object lock = new Object();

    private static AtomicBoolean init = new AtomicBoolean(false);

    private static final Logger logger = LoggerFactory.getLogger(HttpCheckerServer.class);

    private Tomcat createTomcat() {
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(httpListenPort <= 0 ? DEFAULT_HTTP_PORT : httpListenPort);
        File base = new File(CONTEXT_FILE_PATH);
        Context context = tomcat.addContext("", base.getAbsolutePath());
        tomcat.addServlet(context, redisCheckerServlet.myServletName, redisCheckerServlet);
        context.addServletMapping("/check/redis", redisCheckerServlet.myServletName);
        tomcat.addServlet(context, rabbitmqCheckerServlet.myServletName, rabbitmqCheckerServlet);
        context.addServletMapping("/check/rabbitmq", rabbitmqCheckerServlet.myServletName);
        tomcat.addServlet(context, mysqlCheckerServlet.myServletName, mysqlCheckerServlet);
        context.addServletMapping("/check/mysql", mysqlCheckerServlet.myServletName);
        tomcat.addServlet(context, cassendraCheckerServlet.myServletName, cassendraCheckerServlet);
        context.addServletMapping("/check/cassendra", cassendraCheckerServlet.myServletName);

        tomcat.addServlet(context, liveStopServerlet.myServletName, liveStopServerlet);
        context.addServletMapping("/live/message", liveStopServerlet.myServletName);

        return tomcat;
    }


    public void start() {
        synchronized (lock) {
            if (init.compareAndSet(false, true)) {
                if (logger.isInfoEnabled()) {
                    logger.info("Http check server Starting ...");
                }
                tomcat = createTomcat();
                try {
                    tomcat.start();
                } catch (LifecycleException e) {
                    logger.error("Start http check server error !", e);
                    init.compareAndSet(true, false);
                    tomcat = null;
                }
            }
        }
    }


    @Override
    public void destroy() throws Exception {
        if (logger.isInfoEnabled()) {
            logger.info("Stop http check server !");
        }
        stop();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (logger.isInfoEnabled()) {
            logger.info("Start http check server !");
        }
        start();
        //TODO 稍后启动
    }

    public void stop() {
        if (tomcat != null) {
            try {
                tomcat.stop();
            } catch (LifecycleException e) {
                e.printStackTrace();
            }
        }
    }

}
