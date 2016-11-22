package com.handwin.retry.service;

import com.handwin.database.bean.RetrySendMessage;
import com.handwin.message.bean.ChannelInformation;
import com.handwin.message.service.RetryMessageService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.GenericXmlApplicationContext;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.*;

public class RetryMessageServiceTest {

    CountDownLatch latch = new CountDownLatch(1);

    RetryMessageService service;

    @Before
    public void before() {
//        System.setProperty("spring.profiles.active", "test-mock-cn");


        initSystem(latch);

    }

    @Test
    public void testCreateMessage() throws InterruptedException {
        latch.await();

        RetrySendMessage retrySendMessage = new RetrySendMessage();
        retrySendMessage.setRetryMessageID("test");
        retrySendMessage.setStatus(0);
        retrySendMessage.setToUserID("11111111111111111111111111111111");
        retrySendMessage.setFromUserID("22222222222222222222222222222222");
        retrySendMessage.setToRegion("0086");
        ChannelInformation channelInformation= new ChannelInformation();
        channelInformation.setExchange("test_exchange");
        channelInformation.setRegion("0086");
        channelInformation.setRouteKey("test_routekey");
        byte[] content = "test".getBytes();
        boolean result = service.createMessage(retrySendMessage,channelInformation,content);
        assertTrue(result);
    }

    private void initSystem(CountDownLatch latch) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("config-node.properties");
        Properties prop = new Properties();
        try {
            prop.load(inputStream);
            String nodeId = prop.getProperty("node.id");
            String nodeIp = prop.getProperty("node.ip");
            System.setProperty("LOG_PREFIX", String.format("[biz:%s]", nodeId));
            System.setProperty("LOG_FLUME_IP", nodeIp);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String prefix = "classpath:";
        String loading = "config-loading.xml";
        String dir = "xmls";

        System.setProperty("spring.profiles.active", "test-mock-cn");
        final GenericXmlApplicationContext context = new GenericXmlApplicationContext();

        context.load(prefix + dir + File.separatorChar + loading);

        new Thread(() -> {

            try {
                Thread.sleep(10 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            service = (RetryMessageService) context.getBean("retryMessageServiceImpl");

            latch.countDown();

        }).start();

        new Thread(() -> context.refresh()).start();

    }

}