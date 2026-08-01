package com.exchange.me.domain;

public record MatchEngineUpdate(Long orderId, Long userId, MatchEventStatus status){
}
