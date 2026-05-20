package com.exchange.wallet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;


@EnableFeignClients
@SpringBootApplication
public class WalletApp {
    public static void main(String[] args) {
        SpringApplication.run(WalletApp.class, args);
    }
}
