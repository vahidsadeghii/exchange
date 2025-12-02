package com.exchange.me.service.impl;

import com.exchange.me.domain.Order;
import com.exchange.me.handler.OrderBookHandler;
import com.exchange.me.service.EngineService;

public class EngineServiceImpl implements EngineService {
    private final OrderBookHandler orderBookHandler;

    public EngineServiceImpl(OrderBookHandler orderBookHandler) {
        this.orderBookHandler = orderBookHandler;
    }


    @Override
    public void processOrder(long orderId, long userId, Order.TradePair tradePair, Order.OrderType orderType,
            boolean isBuyOrder, double quantity,
            double price) {
        orderBookHandler.matchOrder(System.currentTimeMillis(),
                new Order(orderId, System.currentTimeMillis(), userId,
                        isBuyOrder ? Order.OrderSide.BUY : Order.OrderSide.SELL, orderType, tradePair, quantity,
                        price));
    }
}
