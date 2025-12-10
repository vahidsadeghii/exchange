package com.exchange.oms.controller.order.saveorder;

import com.exchange.oms.domain.OrderType;
import com.exchange.oms.domain.TradePair;

public record CreateOrderRequest(
        TradePair tradePair,
        OrderType orderType,
        double quantity,
        double price) {
}
