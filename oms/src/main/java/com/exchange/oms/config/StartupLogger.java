package com.exchange.oms.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;



@Slf4j
@Component
@RequiredArgsConstructor
public class StartupLogger {
    private final BuildProperties buildProperties;

    @Value("${spring.application.name}")
    private String serviceName;

    @EventListener(ApplicationStartedEvent.class)
    public void logStartupHeader() {
        String separator = "===============================================================================";
        log.info(separator);
        log.info("Application: {} | Version: {}", serviceName, buildProperties.getVersion());
        log.info(separator);
    }
}
