package com.exchange.me.service;

import com.exchange.me.domain.MatchEvent;
import com.exchange.me.domain.OrderType;
import com.exchange.me.domain.TradePair;

public interface MatchEventService {

    void processOrder(long orderId, long userId,
                      TradePair tradePair, OrderType orderType,
                      boolean isBuyOrder, double quantity, double price);

}

