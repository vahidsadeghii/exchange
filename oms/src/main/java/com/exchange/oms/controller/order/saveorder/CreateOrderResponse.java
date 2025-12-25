package com.exchange.oms.controller.order.saveorder;

import java.time.LocalDateTime;

public record CreateOrderResponse(
        long id,
        String tradePair,
        String tradeSide,
        String orderType,
        String orderStatus,
        double quantity,
        double price,
        LocalDateTime createDate) {
}

