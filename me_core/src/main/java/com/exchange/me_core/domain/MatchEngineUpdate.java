package com.exchange.me_core.domain;

import com.exchange.core.sbe.MatchStatus;

public record MatchEngineUpdate(Long orderId, Long userId, MatchStatus matchStatus){
}
