package com.exchange.oms;

import org.springframework.boot.SpringApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

import org.springframework.boot.autoconfigure.SpringBootApplication;


//TODO:Why did you set basePackages?
@SpringBootApplication
@EnableFeignClients(basePackages = "com.exchange.oms.client")
public class OMS {
    public static void main(String[] args) {
        SpringApplication.run(OMS.class, args);
    }
}