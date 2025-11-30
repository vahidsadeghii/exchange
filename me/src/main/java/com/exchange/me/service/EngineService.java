package com.exchange.me.service;

import com.exchange.me.domain.Order;

public interface  EngineService {
    public void processOrder(long orderId, long userId, Order.TradePair tradePair, Order.OrderType orderType, boolean isBuyOrder, double quantity, double price);
}
