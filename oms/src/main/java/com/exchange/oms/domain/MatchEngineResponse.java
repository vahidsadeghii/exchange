package com.exchange.oms.domain;

public record MatchEngineResponse(long orderId, long userId, MatchEventStatus status){
}
