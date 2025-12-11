package com.exchange.wallet.config.exception;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import java.util.Locale;

@Configuration
public class MessageBundleLoader {
    private static ReloadableResourceBundleMessageSource messageSource;

    @Bean
    public static MessageSource messageSource() {
        messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setDefaultEncoding( "UTF-8" );
        messageSource.setBasename( "classpath:messages" );
        messageSource.setUseCodeAsDefaultMessage(true);
        return messageSource;
    }

    public static String getMessage(String key, Locale locale) {
        return messageSource().getMessage( key, null, locale );
    }
}
