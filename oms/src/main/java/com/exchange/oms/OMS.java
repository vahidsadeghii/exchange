package com.exchange.oms;

import org.springframework.boot.SpringApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableFeignClients
public class OMS {
    public static void main(String[] args) {
        SpringApplication.run(OMS.class, args);
    }
}