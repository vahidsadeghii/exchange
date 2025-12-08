package com.exchange.me.controller.neworder;

import com.exchange.me.domain.OrderType;
import com.exchange.me.domain.TradePair;
import com.exchange.me.domain.TradeSide;

public record NewOrderRequest(long orderId, long userId,
                              TradePair tradePair, OrderType orderType,
                              TradeSide tradeSide,
                              double quantity, double price) {
}
