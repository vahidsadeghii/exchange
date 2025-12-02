package com.exchange.oms.controller.order.saveorder;

import java.time.LocalDateTime;

public record CreateOrderResponse(
        long id,
        String tradePair,
        String orderType,
        String orderStatus,
        double quantity,
        double price,
        LocalDateTime createDate) {
}

