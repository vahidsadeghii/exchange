package com.exchange.profile;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ProfileApp {
    public static void main(String[] args) {
        SpringApplication.run(ProfileApp.class, args);
    }
}
