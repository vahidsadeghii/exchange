package com.exchange.me.controller.findorderbook;

import com.exchange.me.domain.Order;
import com.exchange.me.domain.TradePair;
import com.exchange.me.service.OrderBookService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
public class FindOrderBookController {
    private final OrderBookService orderBookService;

    @GetMapping(value = "/order")
    public Order getOrderBook(
            @RequestParam long orderId, @RequestParam TradePair tradePair) {
       return orderBookService.getOrder(tradePair, orderId);
    }
}
