package com.exchange.me.domain;

public record MatchEngine(long orderId, long userId, MatchEventStatus status){
}
