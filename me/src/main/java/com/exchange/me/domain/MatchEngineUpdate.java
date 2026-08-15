package com.exchange.me.domain;

import com.exchange.core.sbe.MatchStatus;

public record MatchEngineUpdate(Long orderId, Long userId, MatchStatus matchStatus){
}
