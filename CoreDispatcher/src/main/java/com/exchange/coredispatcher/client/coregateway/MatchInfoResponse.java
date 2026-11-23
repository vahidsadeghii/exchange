package com.exchange.coredispatcher.client.coregateway;

public record MatchInfoResponse(
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
