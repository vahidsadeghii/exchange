package com.exchange.me.controller.neworder;

import com.exchange.me.domain.MatchEventStatus;

public record MatchEngineResponse(long orderId, long userId, MatchEventStatus status){
}
