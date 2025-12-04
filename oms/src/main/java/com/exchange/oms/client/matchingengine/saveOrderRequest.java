package com.exchange.oms.client.matchingengine;


import com.exchange.oms.domain.OrderType;
import com.exchange.oms.domain.TradePair;

public record saveOrderRequest(long orderId,  long userId,
                               TradePair tradePair, OrderType orderType,
                               boolean isBuyOrder,
                               double quantity, double price) {
}
