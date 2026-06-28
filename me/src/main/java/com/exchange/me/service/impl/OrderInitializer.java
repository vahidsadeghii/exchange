package com.exchange.me.service.impl;

import com.exchange.me.domain.Order;
import com.exchange.me.domain.OrderType;
import com.exchange.me.domain.TradePair;
import com.exchange.me.domain.TradeSide;
import com.exchange.me.repository.InMemoryOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@RequiredArgsConstructor
@Slf4j
public class OrderInitializer implements CommandLineRunner {
    public final InMemoryOrderRepository orderRepository;


    @Override
    public void run(String... args) throws Exception {
        if (orderRepository.findByUserId(111L) == null) {
            List<Order> orders = List.of(
                    Order.builder()
                            .id(1)
                            .userId(111L)
                            .tradeSide(TradeSide.BUY)
                            .orderType(OrderType.LIMIT)
                            .tradePair(TradePair.BTC_EURO)
                            .quantity(0.500000000000000000)
                            .price(420.750000000000000000)
                            .build(),
                    Order.builder()
                            .id(2)
                            .userId(111L)
                            .tradeSide(TradeSide.BUY)
                            .orderType(OrderType.LIMIT)
                            .tradePair(TradePair.BTC_EURO)
                            .quantity(0.500000000000000000)
                            .price(820.750000000000000000)
                            .build(),
                    Order.builder()
                            .id(3)
                            .userId(111L)
                            .tradeSide(TradeSide.BUY)
                            .orderType(OrderType.LIMIT)
                            .tradePair(TradePair.BTC_EURO)
                            .quantity(0.500000000000000000)
                            .price(1200.750000000000000000)
                            .build()

            );
            orders.forEach(orderRepository::save);

            log.info("Default orders created for user 111");

        }
    }
}
