package com.exchange.me;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ExchangeApp {
    public static void main(String[] args) {
        SpringApplication.run(ExchangeApp.class, args);
    }
}
