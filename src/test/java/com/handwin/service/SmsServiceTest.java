package com.handwin.service;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created by piguangtao on 15/9/8.
 */
public class SmsServiceTest {

    private SmsService smsService;

    @Before
    public void before() throws InterruptedException {
        smsService = new SmsService();
        smsService.setSmsKey("online-test.v5.cn");
        smsService.setSmsExcludeAuthUrl("http://115.29.237.149:8083/api/sms/sendMsg");
    }

    @Test
    public void testSendSmsExcludeAuth() throws Exception {
        String mobile = "13770508309";
        String countryCode = "0086";
        String msg = "【露脸】普通短信发送测试";
        boolean result = smsService.sendSmsExcludeAuth(mobile, countryCode, msg);
        assertTrue(result);
    }

}