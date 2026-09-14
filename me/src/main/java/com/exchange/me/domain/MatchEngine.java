package com.exchange.me.domain;

import com.exchange.me.sbe.MatchStatus;

public record MatchEngine(long orderId, long userId, MatchStatus matchStatus){
}
