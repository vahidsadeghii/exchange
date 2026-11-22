package com.exchange.coresdk.domain;

import com.exchange.me.sbe.TradeSide;

public record MatchInfoResponse(
        String id,
        long executionId,
        long timestamp,
        TradeSide makerSide,
        long takerOrderId,
        long makerOrderId,
        long takerUserId,
        long makerUserId,
        double quantity,
        double price,
        double takerOriginalQuantity,
        double takerRemain,
        double makerOriginalQuantity,
        double makerRemain
) {
}