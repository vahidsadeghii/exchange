package com.exchange.oms.controller.order.saveorder;

import com.exchange.oms.domain.AssetType;
import com.exchange.oms.domain.OrderType;
import com.exchange.oms.domain.TradePair;
import com.exchange.oms.domain.TradeSide;

import java.math.BigDecimal;

public record CreateOrderRequest(
        AssetType assetType,
        TradePair tradePair,
        OrderType orderType,
        TradeSide tradeSide,
        BigDecimal quantity,
        BigDecimal price) {
}
