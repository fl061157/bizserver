package com.handwin.safeguard.database;

import com.handwin.service.UserService;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.CountDownLatch;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/xmls/config-loading.xml")
public class CassandraExceptionHandlerTest {
    private static final Logger logger = LoggerFactory.getLogger(CassandraExceptionHandlerTest.class);
    private static final String checkUserId = "88888888888888888888888888888888";

    @Autowired
    private UserService userService;

    @BeforeClass
    public static void beforeClass() {
        System.setProperty("spring.profiles.active", "test-mock-us");
    }

    @Test
    public void testHandle() throws Exception {
        try {
            userService.getTokenInfo(checkUserId,0);
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
        }
        CountDownLatch latch = new CountDownLatch(1);
        latch.await();
    }
}