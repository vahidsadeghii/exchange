package com.exchange.gateway.exception;

import org.springframework.context.MessageSource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import java.util.Locale;

public class MessageBundleLoader {
    private static ReloadableResourceBundleMessageSource messageSource;

    public static MessageSource getMessageSource() {
        messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setBasename("classpath:messages");

        return messageSource;
    }

    public static String getMessage(String key, Locale locale) {
        return getMessageSource().getMessage(key, null, locale);
    }
}
