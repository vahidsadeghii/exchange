package com.exchange.me.controller.neworder;


import com.exchange.me.domain.Order;
import com.exchange.me.domain.OrderType;
import com.exchange.me.domain.TradePair;
import com.exchange.me.domain.TradeSide;
import com.exchange.me.handler.OrderBookHandler;
import com.exchange.me.service.EngineService;
import com.netflix.spectator.impl.PatternExpr;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.ZoneId;

@RestController
@RequiredArgsConstructor
@Slf4j
public class NewOrderController {
    private final OrderBookHandler orderBookHandler;


    @PostMapping(value = "/api/${api.prefix.internal}/{timestamp}/match-info")
    public void createOrder(@RequestParam LocalDateTime timestamp, @RequestBody NewOrderRequest request) {
        orderBookHandler.matchOrder(timestamp
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli(), Order.builder()
                .id(request.orderId())
                .userId(request.userId())
                .orderSide(request.tradeSide())
                .orderType(request.orderType())
                .tradePair(request.tradePair())
                .quantity(request.quantity())
                .price(request.price())
                .build());
    }
}
