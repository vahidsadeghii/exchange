package com.exchange.me.controller.neworder;

import com.exchange.me.domain.MarketType;
import com.exchange.me.domain.OrderType;
import com.exchange.me.domain.TradePair;
import com.exchange.me.domain.TradeSide;

public record CreateUpdateOrderRequest(Long oldOrderId, long orderId, long userId,
                                       TradePair tradePair, OrderType orderType,
                                       TradeSide tradeSide, MarketType marketType,
                                       double quantity, double price) {
}
