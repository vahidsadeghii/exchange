package com.exchange.me.controller.updatematchinfo;

import com.exchange.me.domain.OrderType;
import com.exchange.me.domain.TradePair;

public record saveOrderRequest(long orderId,  long userId,
                               TradePair tradePair, OrderType orderType,
                               boolean isBuyOrder,
                               double quantity, double price) {
}
