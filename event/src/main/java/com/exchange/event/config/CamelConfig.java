package com.exchange.event.config;

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

                ThreadPoolProfile threadPoolProfile = new ThreadPoolProfile();
                threadPoolProfile.setId("MyDefault");
                threadPoolProfile.setPoolSize(10);
                threadPoolProfile.setMaxPoolSize(15);
                threadPoolProfile.setMaxQueueSize(250);
                threadPoolProfile.setKeepAliveTime(25L);
                threadPoolProfile.setRejectedPolicy(ThreadPoolRejectedPolicy.Abort);

                camelContext.getExecutorServiceManager()
                        .registerThreadPoolProfile(threadPoolProfile);

                camelContext.getShutdownStrategy()
                        .setTimeout(1200);
            }

            @Override
            public void afterApplicationStart(CamelContext camelContext) {
                // optional
            }
        };
    }
}
