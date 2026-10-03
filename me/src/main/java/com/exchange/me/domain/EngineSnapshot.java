package com.exchange.me.domain;

import com.exchange.me.sbe.MarketType;
import com.exchange.me.sbe.OrderType;
import com.exchange.me.sbe.TradePair;

import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public record EngineSnapshot(List<Order> orders, Map<TradePair, Map<Long, Deque<Order>>> bids, Map<TradePair, Map<Long, Deque<Order>>> asks) {}

