package com.exchange.eventmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class EventmanagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventmanagerApplication.class, args);
    }

}
