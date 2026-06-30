package com.exchange.oms.service;

import com.exchange.oms.domain.*;

import java.math.BigDecimal;

public interface OrderService {

     Order createUpdateOrder(Long oldOrderId, Long onlineUser, AssetType assetType, TradePair tradePair,
                             TradeSide tradeSide, MarketType marketType,
                             OrderType orderType, BigDecimal quantity, BigDecimal price);

     Order getOrder(long orderId);
}
