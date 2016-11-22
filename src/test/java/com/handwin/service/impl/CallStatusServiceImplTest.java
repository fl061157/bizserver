package com.handwin.service.impl;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/xmls/config-loading.xml")
public class CallStatusServiceImplTest {

    @Autowired
    private CallStatusServiceImpl service;

    private String roomId = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";

    private Long startTime = 1425969285L;

    @BeforeClass
    public static void beforeClass() {
        System.setProperty("spring.profiles.active", "test-mock-cn");
    }

    @Test
    public void testSetCallStartTime() throws Exception {
        service.setCallStartTime(roomId,startTime);
        Assert.assertTrue(true);
    }

    @Test
    public void testGetCallStartTime() throws Exception {
        Long startTimeFromCache = service.getCallStartTime(roomId);
        Assert.assertNotNull(startTimeFromCache);
        Assert.assertEquals(startTimeFromCache,startTime);

    }
}