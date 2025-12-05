package com.exchange.me.controller.findorderbook;

import com.exchange.me.domain.OrderBook;
import com.exchange.me.domain.TradePair;
import com.exchange.me.exception.InvalidTradPairException;
import com.exchange.me.handler.OrderBookHandler;
import com.exchange.me.service.impl.OrderBookService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
public class FindOrderBookController {
    private final OrderBookService orderBookService;

    @GetMapping
    public OrderBookResponse getOrderBook(
            @RequestParam TradePair tradePair,
            @RequestParam(required = false, defaultValue = "0") long userId) {

        OrderBookHandler book = orderBookService.getBook(tradePair);
        OrderBook orderBook = book.getOrderBook(userId);

        return OrderBookResponse.builder()
                .asks(orderBook.getAsks())
                .bids(orderBook.getBids())
                .userOrders(orderBook.getUserOrders())
                .build();

    }
}
