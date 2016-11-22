package com.handwin.safeguard;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by piguangtao on 15/3/13.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/xmls/config-loading.xml")
public class ExceptionCheckFilterTest {
    private static final Logger logger = LoggerFactory.getLogger(ExceptionCheckFilterTest.class);

    @BeforeClass
    public static void beforeClass() {
        System.setProperty("spring.profiles.active", "test-mock-cn");
    }

    @Test
    public void test() throws InterruptedException {
        try {
            Integer test = null;
            test.equals(1);
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
        Thread.sleep(5*60*1000);
    }
}
