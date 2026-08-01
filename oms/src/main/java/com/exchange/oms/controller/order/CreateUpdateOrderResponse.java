package com.exchange.oms.controller.order;

import com.exchange.oms.domain.MarketType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateUpdateOrderResponse(
        long id,
        String tradePair,
        String tradeSide,
        String orderType,
        String orderStatus,
        BigDecimal quantity,
        BigDecimal price,
        Long expireDays,
        MarketType marketType,
        LocalDateTime createDate) {
}

