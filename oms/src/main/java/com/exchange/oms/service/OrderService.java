package com.exchange.oms.service;

import com.exchange.oms.domain.*;

import java.math.BigDecimal;
import java.util.Optional;

public interface OrderService {

    Order createUpdateOrder(
            Long oldOrderId, Long onlineUser, AssetType assetType,
            TradePair tradePair, TradeSide tradeSide, MarketType marketType,
            OrderType orderType, BigDecimal quantity, BigDecimal price, Long expireDays);

    Order getOrder(long orderId);

    void matchEngineStatus(long orderId, long userId, MatchEventStatus matchEngineStatus);

    OrderBookDepth getOrderBookDepth(TradePair pair, int depth);

    Optional<Order> findByOrderIdAndTradePair(long orderId, TradePair pair);

    void cancelOrder(long orderId, TradePair pair);
}
