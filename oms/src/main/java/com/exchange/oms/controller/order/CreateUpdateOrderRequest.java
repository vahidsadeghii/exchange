package com.exchange.oms.controller.order;

import com.exchange.oms.domain.*;

import java.math.BigDecimal;

public record CreateUpdateOrderRequest(
        Long oldOrderId,
        AssetType assetType,
        TradePair tradePair,
        OrderType orderType,
        TradeSide tradeSide,
        MarketType marketType,
        BigDecimal quantity,
        BigDecimal price) {
}
