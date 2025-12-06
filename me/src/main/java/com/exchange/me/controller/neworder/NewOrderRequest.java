package com.exchange.me.controller.neworder;

import com.exchange.me.domain.OrderType;
import com.exchange.me.domain.TradePair;

public record NewOrderRequest(long orderId, long userId,
                              TradePair tradePair, OrderType orderType,
                              boolean isBuyOrder,
                              double quantity, double price) {
}
