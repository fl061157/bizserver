package com.handwin;

import com.handwin.utils.LocaleUtils;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import java.util.Locale;

/**
 * Created by piguangtao on 15/8/15.
 */
public class TestResource {

    static ReloadableResourceBundleMessageSource source;

    public static void testResource(String language) {
        source = new ReloadableResourceBundleMessageSource();
        source.setBasename("classpath:i18n/message");
        source.setDefaultEncoding("UTF-8");


        Locale locale = LocaleUtils.parseLocaleString(language);


        String msgs = source.getMessage("ios.offline.push.pic.template", null, locale);
        String[] multiMsg = null;
        if (null != msgs && !"".equals(msgs)) {
            multiMsg = msgs.split("\\^");
        }

        System.out.println(language);
        for (String msg : multiMsg) {
            System.out.println(msg);
        }
    }


    public static void main(String[] args) {
        String language = "th_TH";
        testResource(language);
        language = "th";
        testResource(language);
        language = "th_dddd";
        testResource(language);

        language = "zh";
        testResource(language);

        language = "zh_#hans";
        testResource(language);


        language = "ZH";
        testResource(language);

        language = "zh_cn";
        testResource(language);

        language = "zh-cn";
        testResource(language);

        language = "zh-Hans";
        testResource(language);

        language = "ar";
        testResource(language);

        language = "ar_AE";
        testResource(language);

    }
}
