package com.exchange.coregateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.exchange.coresdk.Client;

@Configuration
public class ApplicationConfig {

    @Bean
    public Client getClient(){
        return new Client();
    }
}
