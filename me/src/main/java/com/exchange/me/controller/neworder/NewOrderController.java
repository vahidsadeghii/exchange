package com.exchange.me.controller.neworder;


import com.exchange.me.domain.*;
import com.exchange.me.service.OrderBookService;
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
    private final OrderBookService orderBookService;


    @PostMapping(value = "/api/${api.prefix.internal}/{timestamp}/match-info")
    public void createOrder(@RequestParam LocalDateTime timestamp, @RequestBody NewOrderRequest request) {
        orderBookService.createNewOrder(timestamp
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli(), request.orderId(),
                request.userId(), request.tradeSide(),
                request.tradePair(), request.orderType(),
                request.quantity(), request.price());
    }
}
