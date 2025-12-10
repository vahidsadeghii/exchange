package com.exchange.me.controller.findorderbook;

import com.exchange.me.domain.Order;
import com.exchange.me.handler.OrderBookHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
public class FindOrderBookController {
    private final OrderBookHandler orderBookHandler;

    @GetMapping
    public Order getOrderBook(
            @RequestParam long orderId) {
       return orderBookHandler.getOrder(orderId);
    }
}
