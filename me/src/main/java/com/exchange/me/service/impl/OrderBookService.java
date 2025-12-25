package com.exchange.me.service.impl;

import com.exchange.me.domain.TradePair;
import com.exchange.me.exception.InvalidTradPairException;
import com.exchange.me.exception.NotFoundOrderBookHandlerException;
import com.exchange.me.handler.OrderBookHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Service
@RequiredArgsConstructor
public class OrderBookService {

    private final Map<TradePair, OrderBookHandler> books = new ConcurrentHashMap<>();

    public OrderBookHandler getOrCreateBook(TradePair pair) {
        return books.computeIfAbsent(pair, p -> new OrderBookHandler(p));
    }

    public OrderBookHandler getBook(TradePair pair) {
        if (pair == null) {
            throw new InvalidTradPairException();
        }
        OrderBookHandler handler = books.get(pair);
        if (handler == null) {
            throw new NotFoundOrderBookHandlerException();
        }
        return handler;
    }
}
