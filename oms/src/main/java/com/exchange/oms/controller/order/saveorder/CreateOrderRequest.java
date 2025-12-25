package com.exchange.oms.controller.order.saveorder;

import com.exchange.oms.domain.OrderType;
import com.exchange.oms.domain.TradePair;
import com.exchange.oms.domain.TradeSide;

public record CreateOrderRequest(
        TradePair tradePair,
        OrderType orderType,
        TradeSide tradeSide,
        double quantity,
        double price) {
}
