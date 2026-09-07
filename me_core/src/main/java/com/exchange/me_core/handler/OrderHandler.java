package com.exchange.me_core.handler;

import com.exchange.core.sbe.OrderType;
import com.exchange.me_core.domain.MatchInfo;
import com.exchange.me_core.domain.Order;
import com.exchange.me_core.matching.MatchingContext;

import java.util.List;

public interface OrderHandler {
      OrderType supports();

       List<MatchInfo> execute(
            long timestamp,
            Order order,
            MatchingContext context
    );
}
