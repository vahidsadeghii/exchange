package com.exchange.oms.client.matchingengine;


import com.exchange.oms.domain.MarketType;
import com.exchange.oms.domain.OrderType;
import com.exchange.oms.domain.TradePair;
import com.exchange.oms.domain.TradeSide;

public record CreateUpdateOrderRequestClient(Long oldOrderId, long orderId, long userId,
                                             TradePair tradePair, OrderType orderType,
                                             TradeSide tradeSide, MarketType marketType,
                                             double quantity, double price) {
}
