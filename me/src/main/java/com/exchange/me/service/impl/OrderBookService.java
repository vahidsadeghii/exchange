package com.exchange.me.service.impl;

import com.exchange.me.domain.TradePair;
import com.exchange.me.exception.InvalidTradPairException;
import com.exchange.me.exception.NotFoundOrderBookHandlerException;
import com.exchange.me.handler.OrderBookHandler;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OrderBookService {

    private final Map<TradePair, OrderBookHandler> books = new ConcurrentHashMap<>();

    public OrderBookHandler getOrCreateBook(TradePair pair) {
        return books.computeIfAbsent(pair, p -> new OrderBookHandler());
    }

    public OrderBookHandler getBook(TradePair tradePair) {
        if(tradePair == null){
            throw new InvalidTradPairException();
        }

        OrderBookHandler orderBookHandler = books.get(tradePair);
        if (orderBookHandler == null) {
            throw new NotFoundOrderBookHandlerException();
        }
        return orderBookHandler;
    }
}
