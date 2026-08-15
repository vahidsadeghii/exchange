package com.exchange.me.handler;

import com.exchange.me.domain.MatchInfo;
import com.exchange.me.domain.Order;
import com.exchange.me.domain.OrderType;
import com.exchange.me.matching.MatchingContext;

import java.util.List;

public interface OrderHandler {
      OrderType supports();

       List<MatchInfo> execute(
            long timestamp,
            Order order,
            MatchingContext context
    );
}
