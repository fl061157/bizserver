package com.handwin.service;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.fluent.Request;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/**
 * Created by sunhao on 15-2-4.
 */
@Service
public class SmsService implements InitializingBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(SmsService.class);

    @Value("${send.sms.key}")
    private String smsKey;

    @Value("${send.sms.exclude.auth.url}")
    private String smsExcludeAuthUrl;


    @Override
    public void afterPropertiesSet() throws Exception {
        LOGGER.info("sms service initialized. sms_exclude_auth_server_url=[{}],smsKey:{}", smsExcludeAuthUrl, smsKey);
    }

    /**
     * 直接调用短信发送模块 发送短信
     *
     * @param mobile
     * @param countryCode
     * @param msg
     */
    public boolean sendSmsExcludeAuth(String mobile, String countryCode, String msg) {
        long currentTime = System.currentTimeMillis();
        String authKey = DigestUtils.md5Hex(smsKey + mobile + currentTime);

        boolean isSendOk = false;
        boolean isNeedResend = true;
        for (int i = 0; i < 3 && isNeedResend; i++) {
            try {
                Request request = Request.Post(smsExcludeAuthUrl)
                        .bodyForm(((Supplier<List<BasicNameValuePair>>) () -> {
                            List<BasicNameValuePair> basicNameValuePairList = Arrays.asList(new BasicNameValuePair("mobile", mobile),
                                    new BasicNameValuePair("msg", msg), new BasicNameValuePair("currentTime", currentTime + ""),
                                    new BasicNameValuePair("authkey", authKey), new BasicNameValuePair("countrycode", countryCode), new BasicNameValuePair("type", "general"));
                            return basicNameValuePairList;
                        }).get(), Charset.forName("UTF-8"));

                String result = request.execute().returnContent().asString();
                if (StringUtils.isNotBlank(result)) {
                    String resultTrim = result.replaceAll(" ", "");
                    if ("{\"error_code\":2000}".equals(resultTrim)) {
                        isSendOk = true;
                    }
                }
                isNeedResend = false;
                LOGGER.debug("result: {}. success to send:{} ", result, isSendOk);
            } catch (Throwable e) {
                LOGGER.error("[send sm].fails to send sm.", e);
                isNeedResend = true;
            }
        }
        return isSendOk;
    }

    public void setSmsKey(String smsKey) {
        this.smsKey = smsKey;
    }

    public void setSmsExcludeAuthUrl(String smsExcludeAuthUrl) {
        this.smsExcludeAuthUrl = smsExcludeAuthUrl;
    }
}
