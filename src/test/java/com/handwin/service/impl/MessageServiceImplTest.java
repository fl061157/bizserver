package com.handwin.service.impl;

import com.handwin.utils.SystemConstant;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericXmlApplicationContext;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import org.junit.Assert;

public class MessageServiceImplTest {

    @Autowired
    private MessageServiceImpl messageService;

    CountDownLatch latch = null;

    @Before
    public void before() {
        System.setProperty("spring.profiles.active", "test-mock-cn");

        latch = new CountDownLatch(1);

        initSystem(latch);

    }

    @Test
    public void testAddServerReceivedMessage() throws InterruptedException {
        latch.await();
        String cmsgId = "d5c9ad433ad84770834a6acef933b1831800";
        int ttl = 60000;

        for (int i =0;i< 1000;i++){
            Long messageId = System.currentTimeMillis();
            String userId = "8411fec0292811e49a2d4978faf390ea";
            messageService.addServerReceivedMessage(cmsgId,messageId,ttl,userId);

            Assert.assertEquals(messageService.isServerReceived(cmsgId,
                    SystemConstant.MSGFLAG_RESENT, userId),messageId);
            Thread.sleep(5);
        }



    }


    private void initSystem(CountDownLatch latch) {
        String nodeId = System.getProperty("node.id");
        if(null == nodeId) nodeId = String.valueOf(SystemConstant.NODE_ID_DEFAULT);
        String nodeIp = System.getProperty("node.ip");
        if(null == nodeIp) nodeIp = SystemConstant.NODE_IP_DEFAULT;
        System.setProperty("LOG_PREFIX", String.format("[biz:%s]", nodeId));
        System.setProperty("LOG_FLUME_IP", nodeIp);

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
            messageService = (MessageServiceImpl) context.getBean("messageServiceImpl");

            latch.countDown();

        }).start();

        new Thread(() -> context.refresh()).start();

    }


}