package com.exchange.oms.client.matchingengine;


import com.exchange.oms.domain.OrderType;
import com.exchange.oms.domain.TradePair;
import com.exchange.oms.domain.TradeSide;

public record createOrderRequest(long orderId, long userId,
                                 TradePair tradePair, OrderType orderType,
                                 TradeSide tradeSide,
                                 double quantity, double price) {
}
