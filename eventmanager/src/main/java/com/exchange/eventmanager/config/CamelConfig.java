package com.exchange.eventmanager.config;

import org.apache.camel.CamelContext;
import org.apache.camel.spi.ThreadPoolProfile;
import org.apache.camel.spring.boot.CamelContextConfiguration;
import org.apache.camel.util.concurrent.ThreadPoolRejectedPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CamelConfig {

    @Bean
    public CamelContextConfiguration camelContextConfiguration() {
        return new CamelContextConfiguration() {

            @Override
            public void beforeApplicationStart(CamelContext camelContext) {

                ThreadPoolProfile profile = new ThreadPoolProfile();
                profile.setId("MyDefault");
                profile.setPoolSize(10);
                profile.setMaxPoolSize(15);
                profile.setMaxQueueSize(250);
                profile.setKeepAliveTime(25L);
                profile.setRejectedPolicy(ThreadPoolRejectedPolicy.Abort);

                camelContext.getExecutorServiceManager()
                        .registerThreadPoolProfile(profile);

                camelContext.getShutdownStrategy()
                        .setTimeout(1200);
            }

            @Override
            public void afterApplicationStart(CamelContext camelContext) {
            }
        };
    }
}