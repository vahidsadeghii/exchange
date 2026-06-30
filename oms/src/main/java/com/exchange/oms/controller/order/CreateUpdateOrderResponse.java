package com.exchange.oms.controller.order;

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
        LocalDateTime createDate) {
}

