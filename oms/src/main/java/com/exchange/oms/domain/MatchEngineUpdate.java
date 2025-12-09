package com.exchange.oms.domain;

public record MatchEngineUpdate(long orderId, long userId,  MatchEventStatus status){
}
