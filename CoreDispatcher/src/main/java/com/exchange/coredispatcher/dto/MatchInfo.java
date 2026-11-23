package com.exchange.coredispatcher.dto;

public record MatchInfo(
        String id,
        long executionId,
        long timestamp,
        String makerSide,
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
