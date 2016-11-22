package com.handwin.utils;

import java.util.Locale;

/**
 * Created by sunhao on 14-8-15.
 */
public class LocaleUtils {
    public static Locale parseLocaleString(String localeCode, Locale defaultLocale) {
        if (defaultLocale == null) {
            defaultLocale = Locale.getDefault();
        }
        if (localeCode == null || localeCode.isEmpty()) {
            return defaultLocale;
        }
        String code = localeCode.replace("_", "-");
        return Locale.forLanguageTag(code);
    }

    public static Locale parseLocaleString(String localeCode) {
        return parseLocaleString(localeCode, Locale.US);
    }
}
