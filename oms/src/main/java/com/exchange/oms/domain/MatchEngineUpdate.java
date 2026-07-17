package com.exchange.oms.domain;

public record MatchEngineUpdate (Long orderId, Long userId, MatchEventStatus status){
}
