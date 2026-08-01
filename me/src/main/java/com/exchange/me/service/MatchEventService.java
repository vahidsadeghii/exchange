package com.exchange.me.service;

import com.exchange.me.domain.MatchEngine;
import com.exchange.me.domain.Order;

public interface MatchEventService {

    MatchEngine saveMatchEvent(Order order) ;
}

