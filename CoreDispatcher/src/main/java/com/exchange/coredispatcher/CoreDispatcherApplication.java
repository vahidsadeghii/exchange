package com.exchange.coredispatcher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.exchange.coredispatcher.client")
@EnableScheduling
public class CoreDispatcherApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoreDispatcherApplication.class, args);
    }

}
