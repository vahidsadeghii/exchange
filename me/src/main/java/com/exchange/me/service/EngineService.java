package com.exchange.me.service;


import com.exchange.me.domain.OrderType;
import com.exchange.me.domain.TradePair;
import com.exchange.me.domain.TradeSide;

public interface  EngineService {
    void processOrder(long orderId, long userId,
                      TradePair tradePair, OrderType orderType,
                      TradeSide tradeSide, double quantity, double price);
}
